package pl.huczeq.rtspplayer.ui.activities;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;

import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.ui.activities.base.BasePreviewcameraActivity;
import pl.huczeq.rtspplayer.ui.views.player.ZoomableTextureView;

public class PreviewCameraActivity extends BasePreviewcameraActivity {

    private final static String TAG = "PreviewCameraActivity";

    private ZoomableTextureView videoView;
    private boolean canTakePicture = false;
    private Thread canTakePictureDelayThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_camera_gl);

        setViewsWidgets();
        if(this.url != null) {
            prepareSurface();
            loadVideo();
        }
    }

    @Override
    protected void setViewsWidgets() {
        super.setViewsWidgets();
        videoView = findViewById(R.id.cameraPreview);
    }

    @Override
    protected void onStart() {
        super.onStart();
        videoView.setVisibility(View.VISIBLE);
        hideUI();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(canTakePictureDelayThread != null) {
            if(canTakePictureDelayThread.isAlive()) {
                canTakePictureDelayThread.interrupt();
            }
            canTakePictureDelayThread = null;
        }
        canTakePicture = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        vlcLibrary.pause();
    }

    @Override
    public void onVideoStart(int width, int height) {
        super.onVideoStart(width, height);
        videoView.onNewVideoSize(width, height);
        if(camera != null) {
            if (canTakePictureDelayThread != null) {
                canTakePictureDelayThread.interrupt();
                canTakePictureDelayThread = null;
            }
            canTakePictureDelayThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    canTakePicture = true;
                    canTakePictureDelayThread = null;
                }
            });
            canTakePictureDelayThread.start();
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        videoView.onConfigurationChanged();
    }

    @Override
    protected boolean canTakePicture() {
        if(!super.canTakePicture())
            return false;
        return this.canTakePicture;
    }

    @Override
    protected Bitmap takePicture() {
        videoView.setVisibility(View.INVISIBLE);
        Bitmap bitmap = this.videoView.getMyBitmap();
        videoView.setVisibility(View.VISIBLE);
        return bitmap;
    }

    @Override
    protected void prepareSurface() {
        vlcLibrary.prepare(videoView);
    }

    @Override
    protected void destroyVlcLibraryObject() {
        super.destroyVlcLibraryObject();
        finish();
    }
}
