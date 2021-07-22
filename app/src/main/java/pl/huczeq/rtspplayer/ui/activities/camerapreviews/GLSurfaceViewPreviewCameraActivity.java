package pl.huczeq.rtspplayer.ui.activities.camerapreviews;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;

import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.ui.renderers.OnTakeImageCallback;
import pl.huczeq.rtspplayer.ui.renderers.base.RendererCallback;
import pl.huczeq.rtspplayer.ui.views.surfaces.MyGLSurfaceView;

public class GLSurfaceViewPreviewCameraActivity extends BasePreviewCameraActivity implements OnTakeImageCallback {

    private final static String TAG = "SurfaceViewPCA";

    private MyGLSurfaceView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_camera_surfaceview_gl);

        setViewsWidgets();
        if(this.url != null) {
            videoView.initRenderer(new RendererCallback() {
                @Override
                public void onReady(SurfaceTexture surfaceTexture) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(!vlcLibrary.isPrepared())
                                prepareSurface();
                            viewModel.getPreviewUrl().observe(GLSurfaceViewPreviewCameraActivity.this, new Observer<String>() {
                                @Override
                                public void onChanged(String newUrl) {
                                    Log.d(TAG, "New url:" + newUrl);
                                    if(newUrl != null) {
                                        camera = viewModel.getCamera();
                                        url = newUrl;
                                        loadVideo();
                                        viewModel.getPreviewUrl().removeObserver(this);
                                    }
                                }
                            });
                        }
                    });
                }
            }, this);
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
    protected void onPause() {
        super.onPause();
        vlcLibrary.pause();
    }

    @Override
    public void onVideStop() {
        super.onVideStop();
        videoView.getSurfaceRenderer().resetTextureRendered();
    }

    @Override
    public void onVideoStart(int width, int height, int videoWidth, int videoHeight) {
        super.onVideoStart(width, height, videoWidth, videoHeight);
        videoView.onNewVideoSize(width, height, videoWidth, videoHeight);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        videoView.onConfigurationChanged();
    }

    @Override
    protected boolean canTakePicture() {
        return false;
    }

    @Override
    protected void prepareSurface() {
        super.prepareSurface();
        vlcLibrary.prepare(videoView.getSurfaceRenderer().getSurfaceTexture());
    }

    @Override
    public void onVideError() {
        super.onVideError();
    }

    @Override
    protected void destroyVlcLibraryObject() {
        super.destroyVlcLibraryObject();
        videoView.getSurfaceRenderer().recreateSurface();
    }

    @Override
    public void onSaveImg(final Bitmap bitmap) {
        Log.d(TAG, "onSaveImage" + this.camera.getCameraInstance().getName());
        dataManager.saveCameraPreviewImg(this.camera.getCameraInstance(), bitmap);
    }

    @Override
    protected void loadVideo() {
        super.loadVideo();
        if(camera != null) {
            if (settings.isEnabledGenerateCameraTumbnailOnce()) {
                if (camera.getCameraInstance().getPreviewImg() == null || camera.getCameraInstance().getPreviewImg().trim().isEmpty()) {
                    videoView.takePicture();
                }
            } else {
                videoView.takePicture();
            }
        }
    }
}
