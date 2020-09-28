package pl.huczeq.rtspplayer.activities;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.PixelCopy;
import android.view.SurfaceView;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.activities.main.BaseActivity;
import pl.huczeq.rtspplayer.utils.data.Camera;
import pl.huczeq.rtspplayer.utils.data.Data;
import pl.huczeq.rtspplayer.views.VideoView;

public class PreviewCameraActivity extends BaseActivity {

    private static String TAG = "PreviewCameraActivity";

    public static String EXTRA_CAMERA_NAME = "cameraName";
    public static String EXTRA_URL = "cameraUrl";

    private VideoView videoView;

    private Camera camera;
    private String url;

    OrientationListener orientationListener;

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

        orientationListener = new OrientationListener(this);
    }

    @Override
    protected void onStart() {
        orientationListener.enable();
        super.onStart();
    }

    @Override
    protected void onStop() {
        orientationListener.disable();
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
    }

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
