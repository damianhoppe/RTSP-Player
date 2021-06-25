package pl.huczeq.rtspplayer.ui.activities.camerapreviews;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;

import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.ui.renderers.OnTakeImageCallback;
import pl.huczeq.rtspplayer.ui.renderers.base.RendererCallback;
import pl.huczeq.rtspplayer.ui.views.surfaces.MyGLTextureView;
import pl.huczeq.rtspplayer.ui.activities.base.BasePreviewCameraActivity;

public class GLTextureViewPreviewCameraActivity extends BasePreviewCameraActivity implements OnTakeImageCallback{

    private final static String TAG = "GLPreviewCameraActivity";

    private MyGLTextureView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_camera_textureview_gl);

        setViewsWidgets();
        if(this.url != null) {
            if(this.camera != null) {
                if (settings.isEnabledGenerateCameraTumbnailOnce()) {
                    if (this.camera.getPreviewImg() == null || this.camera.getPreviewImg().trim().isEmpty()) {
                        videoView.takePicture();
                    }
                } else {
                    videoView.takePicture();
                }
            }
            videoView.initRenderer(new RendererCallback() {
                @Override
                public void onReady(SurfaceTexture surfaceTexture) {
                    prepareSurface();
                    loadVideo();
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
    protected Bitmap takePicture() {
        videoView.setVisibility(View.INVISIBLE);
        Bitmap bitmap = this.videoView.getMyBitmap();
        videoView.setVisibility(View.VISIBLE);
        return bitmap;
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
    public void onSaveImg(Bitmap bitmap) {
        dataManager.savePreviewImg(this.camera, bitmap);
    }
}
