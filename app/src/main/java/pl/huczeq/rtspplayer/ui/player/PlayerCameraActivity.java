package pl.huczeq.rtspplayer.ui.player;

import static pl.huczeq.rtspplayer.ui.addeditcamera.BaseCameraFormActivity.EXTRA_CAMERA_INSTANCE_ID;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.app.RemoteAction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Icon;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.util.Rational;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.core.view.HapticFeedbackConstantsCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;

import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import pl.huczeq.rtspplayer.BuildConfig;
import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.Settings;
import pl.huczeq.rtspplayer.RtspPlayerProvider;
import pl.huczeq.rtspplayer.data.model.Camera;
import pl.huczeq.rtspplayer.databinding.ActivityPreviewCameraSurfaceviewGlBinding;
import pl.huczeq.rtspplayer.ui.BaseActivity;
import pl.huczeq.rtspplayer.ui.player.view.PlayerSurfaceView;
import pl.huczeq.rtspplayer.ui.player.view.PlayerView;
import pl.huczeq.rtspplayer.ui.player.view.renderer.OnFrameCaptureListener;
import pl.huczeq.rtspplayer.util.Timer;
import pl.huczeq.rtspplayer.player.PlayerEventListener;
import pl.huczeq.rtspplayer.player.RtspPlayer;

@AndroidEntryPoint
public class PlayerCameraActivity extends BaseActivity implements PlayerHandler, OnFrameCaptureListener, PlayerView.OnVideoBoundsChangeListener {

    private static final String TAG = PlayerCameraActivity.class.getSimpleName();

    public static final String EXTRA_URL = "streamUrl";
    public static final String EXTRA_PAR_FORCE_TCP = "forceTcp";

    private boolean mute = false;

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
    private boolean switchAnimating = false;
    private Runnable pendingSlideIn;

    private final Rational MIN_PIP_ASPECT_RATION = new Rational(100,239);
    private final Rational MAX_PIP_ASPECT_RATION = new Rational(239,100);
    private final Rational DEFAULT_PIP_ASPECT_RATION = new Rational(16,9);

    private final Timer playerControlVisibilityTimer = new Timer(3000, new Timer.Callback() {
        @Override
        public void onStart() {
            if(!isPlayerControlVisible()) {
                setPlayerControlVisibility(true);
            }
        }

        @Override
        public void onFinished() {
            setPlayerControlVisibility(false);
        }
    });

    private static final String PIP_ACTION_PREVIOUS = "PIP_ACTION_PREVIOUS";
    private static final String PIP_ACTION_NEXT = "PIP_ACTION_NEXT";

    private final BroadcastReceiver pipActionsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent == null || intent.getAction() == null)
                return;

            switch(intent.getAction()) {
                case PIP_ACTION_PREVIOUS:
                    loadAdjacentCamera(-1);
                    break;
                case PIP_ACTION_NEXT:
                    loadAdjacentCamera(1);
                    break;
            }
        }
    };


    private final OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(false) {
        @Override
        public void handleOnBackPressed() {
            setEnabled(false);

            if(!navToLauncherTask(getApplicationContext())) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.N)
    private boolean isPipModeAvailable() {
        AppOpsManager appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        if(appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_PICTURE_IN_PICTURE, Process.myUid(), BuildConfig.APPLICATION_ID) != AppOpsManager.MODE_ALLOWED)
            return false;
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE);
    }

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

        PlayerCameraViewModel.Params params = buildParams(getIntent());
        if(params == null) {
            Toast.makeText(this, R.string.error_stream_url_not_passed, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        this.viewModel.setStreamParams(params);

        this.binding.playerView.setOnSwipeListener(this::loadAdjacentCamera);

        playerControlVisibilityTimer.start();
        lastScreenOrientation = getResources().getConfiguration().orientation;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isPipModeAvailable()) {
            pictureInPictureParamsBuilder = new PictureInPictureParams.Builder();
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                pictureInPictureParamsBuilder.setSeamlessResizeEnabled(false);
                pictureInPictureParamsBuilder.setAutoEnterEnabled(settings.autoEnterPipModeEnabled());
            }
            setDefaultPIPRectAndAspectRatio(pictureInPictureParamsBuilder);
            setPictureInPictureParams(pictureInPictureParamsBuilder.build());
        }

        this.binding.playerView.getPlayerSurfaceView().setOnFirstFrameRenderedListener(this::runPendingSlideIn);

        this.player = this.playerProvider.build();
        this.mute = this.settings.isMuteAudioDefaultEnabled();
        this.player.setMute(mute);
        updateVolumeButtonIcon();

        player.attachView(this.binding.playerView);

        this.player.registerEventListener(new PlayerEventListener() {
            @Override
            public void onStartRendering() {
                onPlayingStarted();
            }

            @Override
            public void onPlaying() {
                if(mute) {
                    player.setMute(true);
                }
                updateVolumeButtonIcon();
            }

            @Override
            public void onEndReached() {
                onPlayingStopped();
            }

            @Override
            public void onEncounteredError() {
                onPlayingStopped();
            }
        });

        pendingSlideIn = () -> binding.playerView.getPlayerSurfaceView().animate()
                .translationX(0f)
                .alpha(1f)
                .setDuration(150)
                .start();
        binding.playerView.getPlayerSurfaceView().setAlpha(0f);
        loadMedia();
        startPlaying();

        this.binding.playerView.registerOnFrameCaptureListener(this);

        this.binding.playerView.registerOnVideoBoundsChangeListener(this);

        this.binding.playerControl.buttonEnterPictureInPictureMode
                .setVisibility(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
                    getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)?
                        View.VISIBLE : View.INVISIBLE
                );

        this.viewModel.getAdjacentCamera().observe(this, new Observer<Pair<Camera, Camera>>() {
            @Override
            public void onChanged(Pair<Camera, Camera> cameraPair) {
                if(cameraPair != null) {
                    boolean isSwipeAvailable = cameraPair.first != null && cameraPair.second != null;
                    binding.playerView.setSwipeGestureAvailable(isSwipeAvailable);
                    viewModel.getAdjacentCamera().removeObserver(this);
                    updatePIPActions(isSwipeAvailable);
                }
            }
        });

        this.viewModel.getReconnectTimer().observe(this, timer -> {
            if(timer == null) {
                binding.tvReconnectTimer.setVisibility(View.GONE);
                return;
            }
            if(timer > 0) {
                binding.tvReconnectTimer.setText(getResources().getQuantityString(R.plurals.message_reconnect, timer, timer));
                binding.llReconnect.setVisibility(View.VISIBLE);
                binding.tvReconnectTimer.setVisibility(View.VISIBLE);
                return;
            }
            if(timer == 0) {
                reconnect();
                viewModel.stopReconnectTimer();
            }
        });

        getOnBackPressedDispatcher().addCallback(this.onBackPressedCallback);
    }

    @Override
    protected void onPause() {
        super.onPause();
        binding.playerView.cancelFrameCapture();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setDefaultPIPRectAndAspectRatio(PictureInPictureParams.Builder pictureInPictureParamsBuilder) {
        int displayWidth, displayHeight;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Rect windowRect = getWindowManager().getCurrentWindowMetrics().getBounds();
            displayWidth = windowRect.width();
            displayHeight = windowRect.height();
        }else {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            //noinspection deprecation
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            displayWidth = displayMetrics.widthPixels;
            displayHeight = displayMetrics.heightPixels;
        }
        int min;
        boolean vertically;
        if(displayWidth < displayHeight) {
            min = displayWidth;
            vertically = false;
        }else {
            min = displayHeight;
            vertically = true;
        }
        Rational aspectRatio = new Rational(16,9);
        Rect rect = new Rect();
        if(vertically) {
            rect.top = 0;
            rect.bottom = min;
            int width = (int) (aspectRatio.floatValue() * min);
            rect.left = (displayWidth - width)/2;
            rect.right = rect.left + width;
        }else {
            int height = (int) (min/aspectRatio.floatValue());
            rect.top = (displayHeight - height)/2;
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
        switchStream(params);
        onBackPressedCallback.setEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(!player.isViewAttached())
            player.attachView(binding.playerView);

        if(player.isPlaying())
            return;

        if(!viewModel.isReconnectTimerRunning()) {
            startPlaying();
            return;
        }

        if(!isNetworkAvailable()) {
            reconnectOnNetworkAvailable();
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
        binding.playerView.animate().cancel();
        binding.playerView.setTranslationX(0f);
        binding.playerView.setAlpha(1f);
        switchAnimating = false;
        pendingSlideIn = null;
        player.stop();
        if(orientationListener != null)
            orientationListener.disable();
        disableNetworkCallback();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pictureInPictureParamsBuilder = null;
        player.detachView();
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
        if(isInPictureInPictureMode())
            return;
        lastScreenOrientation = newConfig.orientation;
        binding.playerView.getTransformController().reset();
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
        if(isPlayerControlVisible())
            playerControlVisibilityTimer.stop();
        else
            playerControlVisibilityTimer.start();
    }

    @Override
    public void reconnect() {
        binding.llReconnect.setVisibility(View.GONE);
        loadMedia();
        startPlaying();
    }

    @Override
    public void switchVolume() {
        player.setMute(!player.isMute());
        this.mute = player.isMute();
        updateVolumeButtonIcon();
        playerControlVisibilityTimer.start();
    }

    @Override
    public void enterPlayerIntoPictureInPictureMode() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
            return;
        if(!isPipModeAvailable())
            return;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && pictureInPictureParamsBuilder != null) {
            enterPictureInPictureMode(pictureInPictureParamsBuilder.build());
        }else {
            enterPictureInPictureModeDeprecated();
        }
        binding.playerView.getTransformController().resetScaleWithAnimation();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressWarnings("deprecation")
    private void enterPictureInPictureModeDeprecated() {
        enterPictureInPictureMode();
    }

    @Override
    public void back() {
        getOnBackPressedDispatcher().onBackPressed();
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

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        binding.playerView.getTransformController().resetScaleWithAnimation();
        if (player.isPlaying() && settings.autoEnterPipModeEnabled() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            enterPlayerIntoPictureInPictureMode();
        }
    }

    private void loadAdjacentCamera(int direction) {
        if(switchAnimating)
            return;
        viewModel.loadAdjacentCamera(direction, camera -> {
            if(isDestroyed() || switchAnimating)
                return;
            if(camera == null) {
                binding.playerView.getTransformController().resetTranslationWithAnimation();
                ViewCompat.performHapticFeedback(binding.playerView, HapticFeedbackConstantsCompat.REJECT);
                return;
            }
            PlayerCameraViewModel.Params newParams = PlayerCameraViewModel.Params.builder()
                    .url(camera.getCameraInstance().getUrl())
                    .forceTcpEnabled(settings.isPreferUseRtspTcpEnabled())
                    .cameraId(camera.getCameraInstance().getId())
                    .build();
            if(newParams.equals(viewModel.getStreamParams()))
                return;
            animateSwitch(direction, () -> switchStream(newParams));
        });
    }

    /**
     * Shows the loading spinner and slides the current stream out.
     * The previous camera preview is cleared using view.clearSurface() and remains black until rendering starts.
     * If the slide-in animation is enabled, the new preview slides in once rendering starts;
     * otherwise, the preview appears with a fade-in animation as playback starts.
     */
    private void animateSwitch(int direction, Runnable switchAction) {
        final PlayerSurfaceView view = binding.playerView.getPlayerSurfaceView();
        final float distance = view.getWidth() * direction;
        switchAnimating = true;
        binding.llReconnect.setVisibility(View.GONE);
        binding.pBLoading.setVisibility(View.VISIBLE);
        view.animate()
                .translationX(-distance)
                .alpha(0f)
                .setDuration(150)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationCancel(Animator animation) {
                        switchAnimating = false;
                    }
                })
                .withEndAction(() -> {
                    view.setAlpha(0f);
                    view.setTranslationX(settings.isCameraSlideInAnimationEnabled()? distance : 0f);
                    view.clearSurface();
                    pendingSlideIn = () -> view.animate()
                            .translationX(0f)
                            .alpha(1f)
                            .setDuration(150)
                            .withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    switchAnimating = false;
                                }
                            })
                            .start();
                    switchAction.run();
                    switchAnimating = false;
                })
                .start();
    }

    private void runPendingSlideIn() {
        Runnable slideIn = pendingSlideIn;
        pendingSlideIn = null;
        if(slideIn != null)
            slideIn.run();
    }

    private void switchStream(PlayerCameraViewModel.Params params) {
        binding.playerView.getTransformController().reset();

        if(!params.equals(viewModel.getStreamParams()) || !player.isPlaying()) {
            this.viewModel.setStreamParams(params);
            loadMedia();
        }
        startPlaying();
    }

    private void updateVolumeButtonIcon() {
        this.binding.playerControl.buttonVolumeChange.setIconResource(player.isMute()? R.drawable.ic_volume_off : R.drawable.ic_volume_on);
    }

    private void loadMedia() {
        PlayerCameraViewModel.Params params = this.viewModel.getStreamParams();
        if(Strings.isNullOrEmpty(params.getUrl())) {
            finish();
            return;
        }
        RtspPlayer.RtspMedia media = new RtspPlayer.RtspMedia(Uri.parse(params.getUrl()), params.isForceTcpEnabled());
        player.loadMedia(media);
        binding.pBLoading.setVisibility(View.VISIBLE);
        binding.playerView.cancelFrameCapture();
    }

    private void startPlaying() {
        binding.llReconnect.setVisibility(View.GONE);
        binding.pBLoading.setVisibility(View.VISIBLE);
        viewModel.stopReconnectTimer();
        player.play();
    }

    private void onPlayingStarted() {
        binding.pBLoading.setVisibility(View.INVISIBLE);
        updateVolumeButtonIcon();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isPipModeAvailable()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                pictureInPictureParamsBuilder.setAutoEnterEnabled(settings.autoEnterPipModeEnabled());
            }
            setPictureInPictureParams(pictureInPictureParamsBuilder.build());
        }
        captureFrame();
    }

    private void onPlayingStopped() {
        this.binding.playerView.cancelFrameCapture();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && isPipModeAvailable() && pictureInPictureParamsBuilder != null) {
            pictureInPictureParamsBuilder.setAutoEnterEnabled(false);
            setPictureInPictureParams(pictureInPictureParamsBuilder.build());
        }
        binding.pBLoading.setVisibility(View.INVISIBLE);
        binding.llReconnect.setVisibility(View.VISIBLE);
        if(settings.isReconnectTimerEnabled()) {
            viewModel.startReconnectTimer();
        }
        if(isNetworkAvailable()) {
            return;
        }
        reconnectOnNetworkAvailable();
    }

    private boolean isPlayerControlVisible() {
        return binding.playerControl.rootPlayerControl.getVisibility() == View.VISIBLE;
    }

    private void setPlayerControlVisibility(boolean visible) {
        binding.playerControl.rootPlayerControl.setVisibility(
                visible?
                    View.VISIBLE
                :
                    View.INVISIBLE
        );
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
            return isNetworkAvailableDeprecated(connectivityManager);
        }
    }


    @SuppressWarnings("deprecation")
    private boolean isNetworkAvailableDeprecated(ConnectivityManager connectivityManager) {
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public void reconnectOnNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();

        this.networkCallback = new ConnectivityManager.NetworkCallback(){
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                runOnUiThread(() -> {
                    if(networkCallback == null)
                        return;
                    disableNetworkCallback();
                    if(getLifecycle().getCurrentState() == Lifecycle.State.RESUMED)
                        reconnect();
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
        }catch (IllegalArgumentException ignored) {
        }finally {
            this.networkCallback = null;
        }
    }

    private boolean lastPIPMode = false;

    private void onPIPModeChanged(boolean isInPictureInPictureMode) {
        if(isInPictureInPictureMode == lastPIPMode)
            return;
        lastPIPMode = isInPictureInPictureMode;
        LayoutTransition lt = new LayoutTransition();
        if(isInPictureInPictureMode) {
            onBackPressedCallback.setEnabled(true);
            lt.disableTransitionType(LayoutTransition.DISAPPEARING);
            binding.uiContainer.setLayoutTransition(lt);

            playerControlVisibilityTimer.stop();
            binding.playerControl.rootPlayerControl.setVisibility(View.GONE);

            IntentFilter pipActionsFilter = new IntentFilter();
            pipActionsFilter.addAction(PIP_ACTION_PREVIOUS);
            pipActionsFilter.addAction(PIP_ACTION_NEXT);

            Log.d("DEBUG", "registerReceiver");
            ContextCompat.registerReceiver(
                    this,
                    pipActionsReceiver,
                    pipActionsFilter,
                    ContextCompat.RECEIVER_NOT_EXPORTED
                );
        }else {
            binding.uiContainer.setLayoutTransition(lt);
            unregisterReceiver(pipActionsReceiver);
        }
    }

    private void updatePIPActions(boolean isSwipeAvailable) {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return;
        if(pictureInPictureParamsBuilder == null)
            return;

        List<RemoteAction> actions = new ArrayList<>();

        if(isSwipeAvailable) {
            actions.add(new RemoteAction(
                            Icon.createWithResource(this, R.drawable.ic_chevrons_left),
                            "Previous",
                            "Previous",
                            PendingIntent.getBroadcast(
                                    this,
                                    1,
                                    new Intent(PIP_ACTION_PREVIOUS).setPackage(getPackageName()),
                                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE)
                    )
            );
            actions.add(new RemoteAction(
                            Icon.createWithResource(this, R.drawable.ic_chevrons_right),
                            "Next",
                            "Next",
                            PendingIntent.getBroadcast(
                                    this,
                                    2,
                                    new Intent(PIP_ACTION_NEXT).setPackage(getPackageName()),
                                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE)
                    )
            );
        }

        pictureInPictureParamsBuilder.setActions(actions);
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

    private void captureFrame() {
        if(viewModel.getStreamParams().getCameraId() > 0)
            binding.playerView.captureFrame();
        binding.playerView.captureFrame();
    }

    @Override
    public void onFrameCapture(Bitmap bitmap) {
        viewModel.saveCameraThumbnail(bitmap);
    }

    protected void hideUI() {
        WindowInsetsControllerCompat insetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        insetsController.hide(WindowInsetsCompat.Type.systemBars());
        insetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }

    private Rational clampRational(Rational rational) {
        if(rational.compareTo(MIN_PIP_ASPECT_RATION) < 0)
            return MIN_PIP_ASPECT_RATION;

        if(rational.compareTo(MAX_PIP_ASPECT_RATION) > 0)
            return MAX_PIP_ASPECT_RATION;

        return rational;
    }

    private Rect calculateVideoBoundsInWindow(Rect videoBoundsInPlayerView) {
        int[] location = new int[2];
        binding.playerView.getLocationInWindow(location);

        Rect result = new Rect(videoBoundsInPlayerView);
        result.offset(location[0], location[1]);

        return result;
    }

    @Override
    public void onVideoBoundsChanged(Rect videoBounds) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return;

        if(!isPipModeAvailable())
            return;

        if(pictureInPictureParamsBuilder == null)
            return;

        Rational videoAspectRatio;

        if(videoBounds == null) {
            videoAspectRatio = DEFAULT_PIP_ASPECT_RATION;
            videoBounds = new Rect(0,0,0,0);
        }else {
            videoAspectRatio = new Rational(videoBounds.width(), videoBounds.height());
            videoBounds = calculateVideoBoundsInWindow(videoBounds);
        }

        videoAspectRatio = clampRational(videoAspectRatio);

        pictureInPictureParamsBuilder.setAspectRatio(videoAspectRatio);
        pictureInPictureParamsBuilder.setSourceRectHint(videoBounds);
        setPictureInPictureParams(pictureInPictureParamsBuilder.build());
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