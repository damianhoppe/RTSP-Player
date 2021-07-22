package pl.huczeq.rtspplayer.ui.activities.camerapreviews;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;

import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.ui.views.surfaces.ZoomableTextureView;

public class TextureViewPreviewCameraActivity extends BasePreviewCameraActivity {

    private final static String TAG = "PreviewCameraActivity";

    private ZoomableTextureView videoView;
    private boolean canTakePicture = false;
    private Thread canTakePictureDelayThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_camera_textureview);

        setViewsWidgets();

        if(!vlcLibrary.isPrepared())
            prepareSurface();
        viewModel.getPreviewUrl().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String newUrl) {
                if(newUrl != null) {
                    camera = viewModel.getCamera();
                    url = newUrl;
                    loadVideo();
                    viewModel.getPreviewUrl().removeObserver(this);
                }
            }
        });
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
    public void onVideoStart(int width, int height, int videoWidth, int videoHeight) {
        super.onVideoStart(width, height, videoWidth, videoHeight);
        videoView.onNewVideoSize(width, height, videoWidth, videoHeight);
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
        if(settings.isEnabledGenerateCameraTumbnailOnce()) {
            if (this.camera.getCameraInstance().getPreviewImg() == null || this.camera.getCameraInstance().getPreviewImg().trim().isEmpty()) {
                return this.canTakePicture;
            }else {
                return false;
            }
        }
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
