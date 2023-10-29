package pl.huczeq.rtspplayer.ui.player;

import static pl.huczeq.rtspplayer.ui.addeditcamera.BaseCameraFormActivity.EXTRA_CAMERA_INSTANCE_ID;

import android.animation.LayoutTransition;
import android.app.ActivityManager;
import android.app.PictureInPictureParams;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Rational;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.databinding.DataBindingUtil;

import com.google.common.base.Strings;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.Settings;
import pl.huczeq.rtspplayer.RtspPlayerProvider;
import pl.huczeq.rtspplayer.databinding.ActivityPreviewCameraSurfaceviewGlBinding;
import pl.huczeq.rtspplayer.ui.BaseActivity;
import pl.huczeq.rtspplayer.ui.player.view.renderer.OnImageCapturedListener;
import pl.huczeq.rtspplayer.ui.player.view.renderer.PlayerRendererCallback;
import pl.huczeq.rtspplayer.util.Timer;
import pl.huczeq.rtspplayer.player.PlayerEventListener;
import pl.huczeq.rtspplayer.player.RtspPlayer;

@AndroidEntryPoint
public class PlayerCameraActivity extends BaseActivity implements PlayerHandler, OnImageCapturedListener {

    private static final String TAG = PlayerCameraActivity.class.getSimpleName();

    public static final String EXTRA_URL = "streamUrl";
    public static final String EXTRA_PAR_FORCE_TCP = "forceTcp";

    private boolean probablyBackstackLost = false;

    public static void putCameraIntoIntent(Intent intent, String url, boolean forceTcp, long cameraInstanceId) {
        intent.putExtra(EXTRA_CAMERA_INSTANCE_ID, cameraInstanceId);
        intent.putExtra(EXTRA_URL, url);
        intent.putExtra(EXTRA_PAR_FORCE_TCP, forceTcp);
    }

    @Inject
    public Settings settings;
    @Inject
    public RtspPlayerProvider playerProvider;
    @Inject
    public PlayerCameraViewModel viewModel;

    private ActivityPreviewCameraSurfaceviewGlBinding binding;

    public RtspPlayer player;
    private OrientationListener orientationListener;
    private PictureInPictureParams.Builder pictureInPictureParamsBuilder;
    private ConnectivityManager.NetworkCallback networkCallback;
    private int lastScreenOrientation;
    private boolean unknownPlayerRect = true;

    private Timer playerControlVisibilityTimer = new Timer(3000, new Timer.Callback() {
        @Override
        public void onStart() {
            if(!isPlayerControlVisibile()) {
                setPlayerControlVisibile(true);
            }
        }

        @Override
        public void onFinished() {
            setPlayerControlVisibile(false);
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        @Settings.OrientationMode int orientationMode = settings.getOrientationMode();
        if(settings.getInitialOrientation() == Settings.InitialOrientation.HORIZONTAL) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            if(orientationMode != Settings.OrientationMode.LOCKED) {
                orientationListener = new OrientationListener(this);
            }
        }else {
            switch(orientationMode) {
                case Settings.OrientationMode.FOLLOW_SYSTEM:
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_USER);
                    break;
                case Settings.OrientationMode.FOLLOW_SENSOR:
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
                    break;
                case Settings.OrientationMode.LOCKED:
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    break;
            }
        }

        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_preview_camera_surfaceview_gl);
        this.binding.setHandler(this);
        this.binding.playerControl.setHandler(this);
        this.binding.surfaceViewContainer.setPlayerView(binding.surfaceView);

        PlayerCameraViewModel.Params params = buildParams(getIntent());
        if(params == null) {
            Toast.makeText(this, R.string.error_stream_url_not_passed, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        this.viewModel.setStreamParams(params);

        playerControlVisibilityTimer.start();
        lastScreenOrientation = getResources().getConfiguration().orientation;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
            pictureInPictureParamsBuilder = new PictureInPictureParams.Builder();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                pictureInPictureParamsBuilder.setSeamlessResizeEnabled(true);
                pictureInPictureParamsBuilder.setAutoEnterEnabled(settings.autoEnterPipModeEnabled());
            }
            setDefaultPIPRectAndAspectRatio(pictureInPictureParamsBuilder);
            setPictureInPictureParams(pictureInPictureParamsBuilder.build());
        }

        this.player = this.playerProvider.build();
        this.player.setMute(this.settings.isMuteAudioDefaultEnabled());
        updateVolumeButtonIcon();

        this.binding.surfaceView.setRendererCallback(new PlayerRendererCallback() {
            @Override
            public void onSurfaceCreated() {
                new Handler(Looper.getMainLooper()).post(() -> {
                    if(!player.isViewAttached()) {
                        player.attachView(binding.surfaceView);
                    }
                });
            }

            @Override
            public void onVideoLayoutChanged(int width, int height) {
            }
        });

        this.player.setEventListener(new PlayerEventListener() {
            @Override
            public void onStartRendering() {
                onPlayingStarted();
            }

            @Override
            public void onPlaying() {}

            @Override
            public void onEndReached() {
                onPlayingStopped();
            }

            @Override
            public void onEncounteredError() {
                onPlayingStopped();
            }
        });

        loadMedia();
        startPlaying();
        this.binding.surfaceView.setOnImageCapturedListener(this);
        takePicture();

        this.binding.surfaceView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                    return;

                if(left == oldLeft
                    && top == oldTop
                    &&right == oldRight
                    && bottom == oldBottom)
                    return;

                Rect oldRect = new Rect();
                oldRect.top = oldTop;
                oldRect.right = oldRight;
                oldRect.bottom = oldBottom;
                oldRect.left = oldLeft;
                Rect rect = new Rect();
                rect.top = top;
                rect.right = right;
                rect.bottom = bottom;
                rect.left = left;

                if(rect.width() <= 1 || rect.height() <= 1)
                    return;
                if(rect.width() == oldRect.width() && rect.height() == oldRect.height())
                    return;
                int maxDiff = 1;
                if(Math.abs(rect.width() - oldRect.width()) <= maxDiff && Math.abs(rect.height() - oldRect.height()) <= maxDiff)
                    return;
                if (binding == null)
                    return;
                if(isDestroyed())
                    return;
                if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE))
                    return;

                Rational rational = clamp(new Rational(rect.width(), rect.height()));
                unknownPlayerRect = false;

                pictureInPictureParamsBuilder.setAspectRatio(rational);
                pictureInPictureParamsBuilder.setSourceRectHint(rect);
                setPictureInPictureParams(pictureInPictureParamsBuilder.build());
            }
        });

        this.binding.playerControl.buttonEnterPictureInPictureMode
                .setVisibility(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
                getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)?
                    View.VISIBLE : View.INVISIBLE);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setDefaultPIPRectAndAspectRatio(PictureInPictureParams.Builder pictureInPictureParamsBuilder) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int min;
        boolean vertically;
        if(displayMetrics.widthPixels < displayMetrics.heightPixels) {
            min = displayMetrics.widthPixels;
            vertically = false;
        }else {
            min = displayMetrics.heightPixels;
            vertically = true;
        }
        Rational aspectRatio = new Rational(16,9);
        Rect rect = new Rect();
        if(vertically) {
            rect.top = 0;
            rect.bottom = min;
            int width = (int) (aspectRatio.floatValue() * min);
            rect.left = (displayMetrics.widthPixels - width)/2;
            rect.right = rect.left + width;
        }else {
            int height = (int) (min/aspectRatio.floatValue());
            rect.top = (displayMetrics.heightPixels - height)/2;
            rect.bottom = rect.top + height;
            rect.left = 0;
            rect.right = min;
        }
        pictureInPictureParamsBuilder.setSourceRectHint(rect);
        pictureInPictureParamsBuilder.setAspectRatio(aspectRatio);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        PlayerCameraViewModel.Params params = buildParams(intent);
        if(params == null) {
            Toast.makeText(this, R.string.error_stream_url_not_passed, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.surfaceViewContainer.resetTransformation();
        binding.surfaceView.resetTransformations();
        binding.surfaceView.clearSurface();

        if(!params.equals(viewModel.getStreamParams()) || !player.isPlaying()) {
            this.viewModel.setStreamParams(params);
            loadMedia();
        }
        startPlaying();
        takePicture();
        probablyBackstackLost = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!player.isViewAttached() && binding.surfaceView.getSurfaceTexture() != null) {
            player.attachView(binding.surfaceView);
        }
        if(player.isPlaying())
            return;
        startPlaying();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                pictureInPictureParamsBuilder.setAutoEnterEnabled(settings.autoEnterPipModeEnabled());
            }
            setPictureInPictureParams(pictureInPictureParamsBuilder.build());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(orientationListener != null)
            orientationListener.enable();
    }

    @Override
    protected void onStop() {
        super.onStop();
        player.stop();
        if(orientationListener != null)
            orientationListener.disable();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(player != null)
            player.release();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if(isInPictureInPictureMode())
                return;
        }
        playerControlVisibilityTimer.start();
        if(hasFocus)
            hideUI();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(lastScreenOrientation == newConfig.orientation)
            return;
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
            return;
        if(unknownPlayerRect && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setDefaultPIPRectAndAspectRatio(pictureInPictureParamsBuilder);
            setPictureInPictureParams(pictureInPictureParamsBuilder.build());
        }
        if(isInPictureInPictureMode())
            return;
        lastScreenOrientation = newConfig.orientation;

        binding.surfaceView.resetTransformations();
        binding.surfaceViewContainer.resetTransformation();
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode);
        onPIPModeChanged(isInPictureInPictureMode);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, @NonNull Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        onPIPModeChanged(isInPictureInPictureMode);
    }

    @Override
    public void togglePlayerControlVisibility() {
        if(isPlayerControlVisibile())
            playerControlVisibilityTimer.stop();
        else
            playerControlVisibilityTimer.start();
    }

    @Override
    public void reconnect() {
        binding.bReconnect.setVisibility(View.INVISIBLE);
        loadMedia();
        startPlaying();
    }

    @Override
    public void switchVolume() {
        player.setMute(!player.isMute());
        updateVolumeButtonIcon();
        playerControlVisibilityTimer.start();
    }

    @Override
    public void enterPlayerIntoPictureInPictureMode() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
            return;
        PackageManager packageManager = getApplicationContext().getPackageManager();
        if(!packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE))
            return;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            enterPictureInPictureMode(pictureInPictureParamsBuilder.build());
        }else {
            enterPictureInPictureMode();
        }
    }

    @Override
    public void back() {
        if(probablyBackstackLost && navToLauncherTask(this)) {
            probablyBackstackLost = false;
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onBackPressed() {
        this.back();
    }

    /*
        Source: https://medium.com/mindorks/android-picture-in-picture-mode-and-backstack-management-for-multiple-activities-8427fe3f8102
        Added returning a boolean value
         */
    public static boolean navToLauncherTask(Context appContext) {
        ActivityManager activityManager = (ActivityManager) appContext.getSystemService(Context.ACTIVITY_SERVICE);
        assert activityManager != null;
        final List<ActivityManager.AppTask> appTasks = activityManager.getAppTasks();
        for (ActivityManager.AppTask task : appTasks) {
            final Intent baseIntent = task.getTaskInfo().baseIntent;
            final Set<String> categories = baseIntent.getCategories();
            if (categories != null && categories.contains(Intent.CATEGORY_LAUNCHER)) {
                task.moveToFront();
                return true;
            }
        }
        return false;
    }

    private Rational clamp(Rational rational) {
        float max = 2.39f;
        float min = 1f/max;
        if(rational.floatValue() < min)
            return new Rational(100,239);
        if(rational.floatValue() > max)
            return new Rational(239,100);
        return rational;
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        binding.surfaceViewContainer.resetTransformationWithAnimation();
        if (player.isPlaying() && settings.autoEnterPipModeEnabled()
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            enterPlayerIntoPictureInPictureMode();
        }
    }

    private void onPIPModeChanged(boolean isInPictureInPictureMode) {
        probablyBackstackLost = true;
        if(isInPictureInPictureMode) {
            LayoutTransition lt = new LayoutTransition();
            lt.disableTransitionType(LayoutTransition.DISAPPEARING);
            binding.uiContainer.setLayoutTransition(lt);

            playerControlVisibilityTimer.stop();
            binding.playerControl.rootPlayerControl.setVisibility(View.GONE);
        }else {
            LayoutTransition lt = new LayoutTransition();
            binding.uiContainer.setLayoutTransition(lt);
        }
    }

    private void updateVolumeButtonIcon() {
        this.binding.playerControl.buttonVolumeChange.setIconResource(player.isMute()? R.drawable.ic_volume_off : R.drawable.ic_volume_on);
    }

    private void onNewVideo() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                pictureInPictureParamsBuilder.setAutoEnterEnabled(settings.autoEnterPipModeEnabled());
            }
            setPictureInPictureParams(pictureInPictureParamsBuilder.build());
        }
        binding.pBLoading.setVisibility(View.INVISIBLE);
    }

    private void loadMedia() {
        onNewVideo();
        PlayerCameraViewModel.Params params = this.viewModel.getStreamParams();
        if(Strings.isNullOrEmpty(params.getUrl())) {
            finish();
            return;
        }
        RtspPlayer.RtspMedia media = new RtspPlayer.RtspMedia(Uri.parse(params.getUrl()), params.isForceTcpEnabled());
        player.loadMedia(media);
    }

    private void startPlaying() {
        binding.pBLoading.setVisibility(View.VISIBLE);
        binding.bReconnect.setVisibility(View.INVISIBLE);
        player.play();
    }

    private void onPlayingStarted() {
        binding.pBLoading.setVisibility(View.INVISIBLE);
        updateVolumeButtonIcon();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                pictureInPictureParamsBuilder.setAutoEnterEnabled(settings.autoEnterPipModeEnabled());
            }
            setPictureInPictureParams(pictureInPictureParamsBuilder.build());
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Network network = connectivityManager.getActiveNetwork();
            if(network == null)
                return false;
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
            return networkCapabilities != null && (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        }else {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
    }

    private void onPlayingStopped() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                pictureInPictureParamsBuilder.setAutoEnterEnabled(false);
            }
            setPictureInPictureParams(pictureInPictureParamsBuilder.build());
        }
        binding.pBLoading.setVisibility(View.INVISIBLE);
        if(isNetworkAvailable()) {
            binding.bReconnect.setVisibility(View.VISIBLE);
            return;
        }
        binding.bReconnect.setVisibility(View.GONE);
        reconnectOnNetworkAvailable();
    }

    private boolean isPlayerControlVisibile() {
        return binding.playerControl.rootPlayerControl.getVisibility() == View.VISIBLE;
    }

    private void setPlayerControlVisibile(boolean visible) {
        if(visible) {
            binding.playerControl.rootPlayerControl.setVisibility(View.VISIBLE);
        }else {
            binding.playerControl.rootPlayerControl.setVisibility(View.INVISIBLE);
        }
    }

    public void reconnectOnNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();

        this.networkCallback = new ConnectivityManager.NetworkCallback(){
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                disableNetworkCallback();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        reconnect();
                    }
                });
            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
            }
        };

        connectivityManager.registerNetworkCallback(builder.build(), this.networkCallback);
    }

    public void disableNetworkCallback() {
        if(this.networkCallback == null)
            return;
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            connectivityManager.unregisterNetworkCallback(this.networkCallback);
            this.networkCallback = null;
        }catch (IllegalArgumentException e) {}
    }

    private PlayerCameraViewModel.Params buildParams(Intent intent) {
        PlayerCameraViewModel.Params.ParamsBuilder paramsBuilder = PlayerCameraViewModel.Params.builder();

        String url;
        if((url = intent.getStringExtra(EXTRA_URL)) == null) {
            return null;
        }
        return paramsBuilder.url(url)
                .forceTcpEnabled(intent.getBooleanExtra(EXTRA_PAR_FORCE_TCP, false))
                .cameraId(intent.getLongExtra(EXTRA_CAMERA_INSTANCE_ID, 0))
                .build();
    }

    private void takePicture() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if(isDestroyed())
                return;
            if(viewModel.getStreamParams().getCameraId() > 0)
                binding.surfaceView.takePicture(viewModel.getStreamParams().getCameraId());
        }, 500);
    }

    @Override
    public void onImageCaptured(Bitmap bitmap, long id) {
        if(id != viewModel.getStreamParams().getCameraId())
            return;
        viewModel.saveCameraThumbnail(bitmap);
    }

    protected void hideUI() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();
            if(controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        }else {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }

    private class OrientationListener extends OrientationEventListener {
        public OrientationListener(Context context) { super(context); }

        @Override
        public void onOrientationChanged(int orientation) {
            if(orientation > 85 && orientation < 95 || orientation > 265 && orientation < 275) {
                if(!isSystemOrientationLocked(PlayerCameraActivity.this)) {
                    this.disable();
                    PlayerCameraActivity.this.orientationListener = null;
                    new Handler(getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(settings.getOrientationMode() == Settings.OrientationMode.FOLLOW_SENSOR) {
                                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
                            }else if(settings.getOrientationMode() == Settings.OrientationMode.FOLLOW_SYSTEM) {
                                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_USER);
                            }
                        }
                    }, 200);
                }
            }
        }

        public boolean isSystemOrientationLocked(Context context) {
            try {
                return android.provider.Settings.System.getInt(context.getContentResolver(), android.provider.Settings.System.ACCELEROMETER_ROTATION) != 1;
            } catch (android.provider.Settings.SettingNotFoundException e) {
                return true;
            }
        }
    }
}