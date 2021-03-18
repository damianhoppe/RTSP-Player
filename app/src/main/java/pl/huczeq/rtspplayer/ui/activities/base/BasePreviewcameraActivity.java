package pl.huczeq.rtspplayer.ui.activities.base;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.data.Settings;
import pl.huczeq.rtspplayer.data.objects.Camera;
import pl.huczeq.rtspplayer.utils.Utils;
import pl.huczeq.rtspplayer.vlc.VlcLibrary;

public class BasePreviewcameraActivity extends BaseActivity implements VlcLibrary.Callback {

    private final static String TAG = "BasePreviewcameraActiv";

    public static String EXTRA_CAMERA_NAME = "cameraName";
    public static String EXTRA_URL = "cameraUrl";

    protected ConstraintLayout rootLayout;
    protected ProgressBar pBLoading;

    protected Camera camera;
    protected String cameraName;
    protected String url;
    protected VlcLibrary vlcLibrary;

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
        if(connectionChanged && connectionAvailable && connectionError && active) {
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

        this.cameraName = getIntent().getStringExtra(EXTRA_CAMERA_NAME);
        this.url = getIntent().getStringExtra(EXTRA_URL);
        if(url == null || url.trim().isEmpty())
        {
            this.url = null;
            finish();
        }
        vlcLibrary = new VlcLibrary(this);
        vlcLibrary.init();
        vlcLibrary.setCallbackListener(this);
        if(this.dataManager.isDataLoaded())
            this.onDataChangedWAA();

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        connectivityManager.registerNetworkCallback(builder.build(), this.networkCallback);
    }

    @Override
    protected void onDataChangedWAA() {
        super.onDataChangedWAA();
        if(this.cameraName != null && !this.cameraName.trim().isEmpty())
            camera = dataManager.getCamera(this.cameraName);
    }

    @Override
    protected void setViewsWidgets() {
        super.setViewsWidgets();

        pBLoading = findViewById(R.id.pBLoading);
        rootLayout = findViewById(R.id.rootLayout);
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
        if(this.canTakePicture() && this.camera != null) {
            dataManager.savePreviewImg(this.camera, this.takePicture());
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
    public void onVideoStart(int width, int height) {
        pBLoading.setVisibility(View.INVISIBLE);
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
        Log.d(TAG, "LOADVIDEO");
        pBLoading.setVisibility(View.VISIBLE);
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
        Log.d(TAG, "PREPARE SURFACE");}

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

    protected boolean canTakePicture() {
        return vlcLibrary.isPlaying();
    }

    protected Bitmap takePicture() {
        return null;
    }

    private class OrientationListener extends OrientationEventListener {
        public OrientationListener(Context context) { super(context); }

        @Override public void onOrientationChanged(int orientation) {
            Log.d(TAG, "Orientation:" + orientation);
            if(orientation > 75 && orientation < 105 || orientation > 255 && orientation < 285) {
                if(!Utils.isSystemOrientationLocked(BasePreviewcameraActivity.this)) {
                    this.disable();
                    BasePreviewcameraActivity.this.orientationListener = null;
                    if(settings.getOrientationMode() == Settings.ORIENTATION_MODE.AUTO_SENSOR) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
                    }else if(settings.getOrientationMode() == Settings.ORIENTATION_MODE.AUTO_SYS) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_USER);
                    }
                }
            }
        }
    }
}
