package pl.huczeq.rtspplayer.ui.activities;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.ui.activities.base.BaseActivity;
import pl.huczeq.rtspplayer.data.objects.Camera;
import pl.huczeq.rtspplayer.ui.views.OldVideoView;

public class PreviewCameraActivity extends BaseActivity implements OldVideoView.Callback {

    private static String TAG = "PreviewCameraActivity";

    public static String EXTRA_CAMERA_NAME = "cameraName";
    public static String EXTRA_URL = "cameraUrl";

    private ConstraintLayout rootLayout;
    private OldVideoView videoView;
    private ProgressBar pBLoading;

    private Camera camera;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_camera);

        setViewsWidgets();

        camera = dataManager.getCamera(getIntent().getStringExtra(EXTRA_CAMERA_NAME));
        if(camera == null) {
            url = getIntent().getStringExtra(EXTRA_URL);
        }else {
            url = camera.getUrl();
        }
        if(url == null || url.trim().isEmpty()) {
            finish();
            return;
        }else {
            videoView.setData(url);
        }

        videoView.setCallback(this);
        hideUI();
        videoView.updateOutputSize();
    }

    @Override
    public void onVideoStart() {
        pBLoading.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onVideError() {
        pBLoading.setVisibility(View.INVISIBLE);
        Toast.makeText(getApplicationContext(), getString(R.string.video_error), Toast.LENGTH_SHORT).show();
    }

    private void hideUI() {
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
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoView.play();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(videoView.canTakePicture() && camera != null)
            dataManager.savePreviewImg(this.camera, videoView.getBitmap());
        videoView.pause();
    }

    @Override
    protected void onDestroy() {
        videoView.setCallback(null);
        super.onDestroy();
        //videoView.pause();
        new Thread(new Runnable() {
            @Override
            public void run() {
                videoView.stop();
                videoView.release();
            }
        }).start();
    }

    @Override
    protected void setViewsWidgets() {
        super.setViewsWidgets();

        videoView = findViewById(R.id.cameraPreview);
        pBLoading = findViewById(R.id.pBLoading);
        rootLayout = findViewById(R.id.rootLayout);
    }
}
