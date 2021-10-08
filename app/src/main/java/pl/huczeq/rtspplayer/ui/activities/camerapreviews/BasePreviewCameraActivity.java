package pl.huczeq.rtspplayer.ui.activities.camerapreviews;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProviders;

import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.data.DataManager;
import pl.huczeq.rtspplayer.data.Settings;
import pl.huczeq.rtspplayer.data.objects.Camera;
import pl.huczeq.rtspplayer.ui.activities.MainActivity;
import pl.huczeq.rtspplayer.ui.activities.base.BaseActivity;
import pl.huczeq.rtspplayer.common.Utils;
import pl.huczeq.rtspplayer.viewmodels.CameraPreviewViewModel;
import pl.huczeq.rtspplayer.viewmodels.factories.CameraPreviewViewModelFactory;
import pl.huczeq.rtspplayer.vlc.VlcLibrary;

public class BasePreviewCameraActivity extends BaseActivity implements VlcLibrary.Callback {

    private final static String TAG = "BasePreviewCameraActiv";

    public static Intent getStartIntent(Context context, Camera camera, boolean showHomeButton) {
        Intent intent = getStartIntent(context, camera);
        intent.putExtra(BasePreviewCameraActivity.EXTRA_SHOW_HOME_BUTTON, showHomeButton);
        return intent;
    }

    public static Intent getStartIntent(Context context, Camera camera) {
        Intent intent = getStartIntent(context);
        intent.putExtra(BasePreviewCameraActivity.EXTRA_CAMERA_ID, camera.getCameraInstance().getId());
        intent.putExtra(BasePreviewCameraActivity.EXTRA_URL, camera.getCameraInstance().getUrl());
        return intent;
    }

    public static Intent getStartIntent(Context context, String url) {
        Intent intent = getStartIntent(context);
        intent.putExtra(BasePreviewCameraActivity.EXTRA_URL, url);
        return intent;
    }

    private static Intent getStartIntent(Context context) {
        Intent intent;
        switch(Settings.getInstance(context).getPlayerSurfaceEnum()) {
            case GLTEXTUREVIEW:
                intent = new Intent(context, GLTextureViewPreviewCameraActivity.class);
                break;
            case TEXTUREVIEW:
                intent = new Intent(context, TextureViewPreviewCameraActivity.class);
                break;
            default:
                intent = new Intent(context, GLSurfaceViewPreviewCameraActivity.class);
                break;
        }
        return intent;
    }

    public static String EXTRA_CAMERA_ID = "cameraId";
    public static String EXTRA_URL = "cameraUrl";
    public static String EXTRA_SHOW_HOME_BUTTON = "showHomeButton";

    protected ConstraintLayout rootLayout;
    protected ProgressBar pBLoading;
    protected ConstraintLayout rootPlayerControl;
    protected ImageView buttonVolumeChange;
    protected ImageView buttomHome;

    protected VlcLibrary vlcLibrary;

    private Thread threadPlayerControlHide;
    private Runnable runnablePlayerControlHide;

    protected CameraPreviewViewModel viewModel;
    protected Camera camera;
    protected String url;

    protected boolean connectionError = false;
    private boolean connectionChanged = false;
    private boolean connectionAvailable = false;
    private boolean active = false;

    private OrientationListener orientationListener;

    ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback(){
        @Override
        public void onAvailable(@NonNull Network network) {
            super.onAvailable(network);
            connectionAvailable = true;
            connectionChanged = true;
            if(active) {
                onStateChanged();
                Toast.makeText(getApplicationContext(), getString(R.string.online_status), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onLost(@NonNull Network network) {
            super.onLost(network);
            connectionAvailable = false;
            connectionChanged = true;
            if(active)
                Toast.makeText(getApplicationContext(), getString(R.string.offline_status), Toast.LENGTH_SHORT).show();
        }
    };

    private void onStateChanged() {
        Log.d(TAG, connectionChanged + " / " + connectionAvailable + " / " + connectionError + " / " + active);
        if(connectionChanged && connectionAvailable && connectionError && active && this.url != null) {
            if (!vlcLibrary.isInitialized()) {
                createVlcLibraryObject();
            }
            prepareSurface();
            loadVideo();
            connectionChanged = false;
            connectionError = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Settings.ORIENTATION_MODE orientationMode = settings.getOrientationMode();
        if(settings.getDefaultOrientation() == Settings.ORIENTATION.HORIZONTAL) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            if(orientationMode != Settings.ORIENTATION_MODE.LOCKED) {
                Log.d(TAG, "OrientationListener init");
                orientationListener = new OrientationListener(this);
            }
        }else {
            Log.d(TAG, "OrientationListener not");
            switch(orientationMode) {
                case AUTO_SYS:
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_USER);
                    break;
                case AUTO_SENSOR:
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
                    break;
                case LOCKED:
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    break;
            }
        }

        int cameraId = getIntent().getIntExtra(EXTRA_CAMERA_ID, -1);
        Log.d(TAG, "Camera id: " + cameraId);
        url = getIntent().getStringExtra(EXTRA_URL);
        if(url.trim().isEmpty())
            url = null;
        if(cameraId == -1 && url == null)
        {
            if(getIntent().getBooleanExtra(EXTRA_SHOW_HOME_BUTTON, false)) {
                startActivity(new Intent(this, MainActivity.class));
            }else {
                finish();
            }
        }
        vlcLibrary = new VlcLibrary(this);
        vlcLibrary.init();
        vlcLibrary.setCallbackListener(this);
        this.viewModel = ViewModelProviders.of(this, new CameraPreviewViewModelFactory(DataManager.getInstance(getApplicationContext()), cameraId, url)).get(CameraPreviewViewModel.class);

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        connectivityManager.registerNetworkCallback(builder.build(), this.networkCallback);

        this.runnablePlayerControlHide = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1800);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(rootPlayerControl.getVisibility() == View.VISIBLE)
                            rootPlayerControl.setVisibility(View.INVISIBLE);
                    }
                });
            }
        };
    }
    @Override
    protected void setViewsWidgets() {
        super.setViewsWidgets();

        pBLoading = findViewById(R.id.pBLoading);
        rootLayout = findViewById(R.id.rootLayout);

        rootPlayerControl = findViewById(R.id.rootPlayerControl);

        View cameraPreviewView = findViewById(R.id.cameraPreview);
        cameraPreviewView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(vlcLibrary.mediaPlayer == null || !vlcLibrary.mediaPlayer.hasMedia()) {
                    if(rootPlayerControl.getVisibility() == View.VISIBLE)
                        rootPlayerControl.setVisibility(View.INVISIBLE);
                    return;
                }
                if(rootPlayerControl.getVisibility() == View.VISIBLE) {
                    rootPlayerControl.setVisibility(View.INVISIBLE);
                    if(threadPlayerControlHide != null) {
                        if (threadPlayerControlHide.isAlive())
                            threadPlayerControlHide.interrupt();
                        threadPlayerControlHide = null;
                    }
                }else {
                    rootPlayerControl.setVisibility(View.VISIBLE);
                    startThreadPlayerControlHide();
                }
            }
        });

        buttonVolumeChange = findViewById(R.id.buttonVolumeChange);
        buttomHome = findViewById(R.id.buttonHome);
        vlcLibrary.mediaPlayer.setVolume(100);
        buttonVolumeChange.setImageResource(R.drawable.ic_volume_on);

        buttonVolumeChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(vlcLibrary.mediaPlayer == null)
                    return;
                if(vlcLibrary.mediaPlayer.getVolume() == 0) {
                    vlcLibrary.mediaPlayer.setVolume(100);
                    buttonVolumeChange.setImageResource(R.drawable.ic_volume_on);
                }else {
                    vlcLibrary.mediaPlayer.setVolume(0);
                    buttonVolumeChange.setImageResource(R.drawable.ic_volume_off);
                }
                startThreadPlayerControlHide();
            }
        });
        buttomHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
        this.buttomHome.setVisibility(getIntent().getBooleanExtra(EXTRA_SHOW_HOME_BUTTON, false)? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        pBLoading.setVisibility(View.VISIBLE);
        vlcLibrary.setCallbackListener(this);
        if(orientationListener != null)
            orientationListener.enable();
        active = true;
        onStateChanged();
    }

    @Override
    protected void onStop() {
        super.onStop();
        vlcLibrary.setCallbackListener(null);
        vlcLibrary.stop();
        if(orientationListener != null)
            orientationListener.disable();
        active = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        vlcLibrary.play();
    }

    @Override
    protected void onPause() {
        super.onPause();
        rootPlayerControl.setVisibility(View.GONE);
        if(threadPlayerControlHide != null) {
            if (threadPlayerControlHide.isAlive()) {
                threadPlayerControlHide.interrupt();
            }
            threadPlayerControlHide = null;
        }
        if(this.canTakePicture() && this.camera != null) {
            dataManager.saveCameraPreviewImg(this.camera.getCameraInstance(), this.takePicture());
        }
        vlcLibrary.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        vlcLibrary.release();
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        connectivityManager.unregisterNetworkCallback(this.networkCallback);
    }

    @Override
    public void onVideoStart(int width, int height, int videoWidth, int videoHeight) {
        //pBLoading.setVisibility(View.GONE);
    }

    @Override
    public void onVideError() {
        Log.d(TAG, "ONVIDEOERROR");
        pBLoading.setVisibility(View.VISIBLE);
        if(active && !connectionChanged)
            Toast.makeText(getApplicationContext(), getString(R.string.video_error), Toast.LENGTH_SHORT).show();
        destroyVlcLibraryObject();
        connectionError = true;
        onStateChanged();
    }

    @Override
    public void onVideStop() {
        pBLoading.setVisibility(View.VISIBLE);
    }

    @Override
    public void onVideoBuffering(float buffering) {
    }

    @Override
    public void onEndReached() {
        onVideError();
    }

    @Override
    public void onVideoPlaying() {
        this.pBLoading.setVisibility(View.GONE);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus) hideUI();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "onConfigurationChanged");
    }

    protected void loadVideo()
    {
        if(this.url == null) return;
        Log.d(TAG, "LOADVIDEO");
        pBLoading.setVisibility(View.VISIBLE);
        if(vlcLibrary.isInitialized() && vlcLibrary.isReleased())
            vlcLibrary.loadData(Uri.parse(url));
        vlcLibrary.play();
    }

    protected void destroyVlcLibraryObject() {
        Log.d(TAG, "DESTROY VLC");
        vlcLibrary.setCallbackListener(null);
        vlcLibrary.stop();
        vlcLibrary.release();
        vlcLibrary = new VlcLibrary(this);
    }

    protected void createVlcLibraryObject() {
        Log.d(TAG, "CREATE VLC");
        vlcLibrary.init();
        vlcLibrary.setCallbackListener(this);
    }

    protected void prepareSurface() {
        Log.d(TAG, "PREPARE SURFACE");
    }

    @SuppressWarnings({"deprecation"})
    private void hideUIDeprecated() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
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
            hideUIDeprecated();
        }
    }

    protected boolean canTakePicture() {
        return vlcLibrary.isPlaying() && this.camera != null;
    }

    protected Bitmap takePicture() {
        return null;
    }

    private void startThreadPlayerControlHide() {
        if (threadPlayerControlHide != null) {
            if (threadPlayerControlHide.isAlive()) {
                threadPlayerControlHide.interrupt();
            }
        }
        threadPlayerControlHide = new Thread(runnablePlayerControlHide);
        threadPlayerControlHide.start();
    }

    private class OrientationListener extends OrientationEventListener {
        public OrientationListener(Context context) { super(context); }

        @Override public void onOrientationChanged(int orientation) {
            Log.d(TAG, "Orientation:" + orientation);
            if(orientation > 85 && orientation < 95 || orientation > 265 && orientation < 275) {
                if(!Utils.isSystemOrientationLocked(BasePreviewCameraActivity.this)) {
                    this.disable();
                    BasePreviewCameraActivity.this.orientationListener = null;
                    new Handler(getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(settings.getOrientationMode() == Settings.ORIENTATION_MODE.AUTO_SENSOR) {
                                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
                            }else if(settings.getOrientationMode() == Settings.ORIENTATION_MODE.AUTO_SYS) {
                                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_USER);
                            }
                        }
                    }, 200);
                }
            }
        }
    }
}
