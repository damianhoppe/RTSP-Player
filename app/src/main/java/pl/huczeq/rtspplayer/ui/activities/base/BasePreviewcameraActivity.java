package pl.huczeq.rtspplayer.ui.activities.base;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

public class BasePreviewcameraActivity extends BaseActivity implements VlcLibrary.Callback{

    private final static String TAG = "BasePreviewcameraActiv";

    public static String EXTRA_CAMERA_NAME = "cameraName";
    public static String EXTRA_URL = "cameraUrl";

    protected ConstraintLayout rootLayout;
    protected ProgressBar pBLoading;

    protected Camera camera;
    protected String url;
    protected VlcLibrary vlcLibrary;

    private OrientationListener orientationListener;

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

        camera = dataManager.getCamera(getIntent().getStringExtra(EXTRA_CAMERA_NAME));
        if(camera == null) {
            url = getIntent().getStringExtra(EXTRA_URL);
        }else {
            url = camera.getUrl();
        }
        if(url == null || url.trim().isEmpty()) {
            finish();
            return;
        }
        vlcLibrary = new VlcLibrary(this);
        vlcLibrary.init();
        vlcLibrary.setCallbackListener(this);
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
        vlcLibrary.play();
        if(orientationListener != null)
            orientationListener.enable();
    }

    @Override
    protected void onStop() {
        super.onStop();
        vlcLibrary.setCallbackListener(null);
        vlcLibrary.stop();
        if(orientationListener != null)
            orientationListener.disable();
    }

    @Override
    protected void onResume() {
        super.onResume();
        vlcLibrary.play();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(this.canTakePicture()) {
            dataManager.savePreviewImg(this.camera, this.takePicture());
        }
        vlcLibrary.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        vlcLibrary.release();
    }

    @Override
    public void onVideoStart(int width, int height) {
        pBLoading.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onVideError() {
        pBLoading.setVisibility(View.INVISIBLE);
        Toast.makeText(getApplicationContext(), getString(R.string.video_error), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onVideStop() {
        pBLoading.setVisibility(View.VISIBLE);
    }

    @Override
    public void onVideoBuffering(float buffering) {
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

    protected boolean canTakePicture() {
        return false;
    }

    protected Bitmap takePicture() {
        return null;
    }

    private class OrientationListener extends OrientationEventListener {
        public OrientationListener(Context context) { super(context); }

        @Override public void onOrientationChanged(int orientation) {/*
            int localRotation = 0;
            if( (orientation < 35 || orientation > 325) && rotation!= ROTATION_O){
                rotation = ROTATION_O;
            }
            else if( orientation > 145 && orientation < 215 && rotation!=ROTATION_180){
                rotation = ROTATION_180;
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
            }
            else if(orientation > 55 && orientation < 125 && rotation!=ROTATION_270){
                rotation = ROTATION_270;
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
            }
            else if(orientation > 235 && orientation < 305 && rotation!=ROTATION_90){
                rotation = ROTATION_90;
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }*/
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
