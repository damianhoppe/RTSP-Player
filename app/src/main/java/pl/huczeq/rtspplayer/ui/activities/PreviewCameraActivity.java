package pl.huczeq.rtspplayer.ui.activities;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.Toast;

import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.ui.activities.base.BaseActivity;
import pl.huczeq.rtspplayer.data.objects.Camera;
import pl.huczeq.rtspplayer.ui.views.OldVideoView;

public class PreviewCameraActivity extends BaseActivity {

    private static String TAG = "PreviewCameraActivity";

    public static String EXTRA_CAMERA_NAME = "cameraName";
    public static String EXTRA_URL = "cameraUrl";

    private OldVideoView videoView;
    private ProgressBar pBLoading;

    private Camera camera;
    private String url;

    //OrientationListener orientationListener;
    //TODO

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_preview_camera);

        setViewsWidgets();

        camera = dataManager.getCamera(getIntent().getStringExtra(EXTRA_CAMERA_NAME));
        if(camera == null) {
            url = getIntent().getStringExtra(EXTRA_URL);
            if(url == null) {
                finish();
                return;
            }
            videoView.setData(Uri.parse(url));
            Log.d(TAG, "Url: " + url);
        }else {
            videoView.setData(Uri.parse(camera.getUrl()));
            Log.d(TAG, "Camera: " + camera.getUrl());
        }

        videoView.setCallback(new OldVideoView.Callback() {
            @Override
            public void onVideoStart() {
                pBLoading.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onVideError() {
                pBLoading.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(), getString(R.string.video_error), Toast.LENGTH_SHORT).show();
            }
        });
        //orientationListener = new OrientationListener(this);

        videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showUI();
            }
        });
        showUI();
    }

    Thread thread;

    private void hideUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);

    }

    private void showUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        if(thread!=null) {
            thread.interrupt();
        }
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        hideUI();
                    }
                });
            }
        });
        thread.start();
    }

    @Override
    protected void onStart() {
        //orientationListener.enable();
        super.onStart();
    }

    @Override
    protected void onStop() {
        //orientationListener.disable();
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
        super.onDestroy();
        videoView.stop();
        new Thread(new Runnable() {
            @Override
            public void run() {
                videoView.release();
            }
        }).start();
    }

    @Override
    protected void setViewsWidgets() {
        super.setViewsWidgets();

        videoView = findViewById(R.id.cameraPreview);
        pBLoading = findViewById(R.id.pBLoading);
    }

    /*@Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        switch (newConfig.orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                Log.d(TAG, "ORIENTATION_PORTRAIT");
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                Log.d(TAG, "ORIENTATION_LANDSCAPE");
                break;
        }
        Log.d(TAG, String.valueOf(getWindowManager().getDefaultDisplay().getRotation()));
    }*/

    private class OrientationListener extends OrientationEventListener {
        final int ROTATION_O    = 1;
        final int ROTATION_90   = 2;
        final int ROTATION_180  = 3;
        final int ROTATION_270  = 4;

        private int rotation = 0;
        public OrientationListener(Context context) { super(context); }

        boolean request = true;
        @Override public void onOrientationChanged(int orientation) {
            if( (orientation < 35 || orientation > 325) && rotation!= ROTATION_O){ // PORTRAIT
                rotation = ROTATION_O;
                if(request)
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                else
                    videoView.onConfigurationChanged(0);
            }
            else if( orientation > 145 && orientation < 215 && rotation!=ROTATION_180){ // REVERSE PORTRAIT
                rotation = ROTATION_180;
                if(request)
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                else
                    videoView.onConfigurationChanged(90);
            }
            else if(orientation > 55 && orientation < 125 && rotation!=ROTATION_270){ // REVERSE LANDSCAPE
                rotation = ROTATION_270;
                if(request)
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                else
                    videoView.onConfigurationChanged(270);
            }
            else if(orientation > 235 && orientation < 305 && rotation!=ROTATION_90){ //LANDSCAPE
                rotation = ROTATION_90;
                if(request)
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                else
                    videoView.onConfigurationChanged(90);
            }
        }
    }
}
