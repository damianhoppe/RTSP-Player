package pl.huczeq.rtspplayer.ui.activities;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.ui.views.player.MyGLTextureView;
import pl.huczeq.rtspplayer.ui.activities.base.BasePreviewcameraActivity;
import pl.huczeq.rtspplayer.ui.renderers.SurfaceTextureRenderer;

public class GLPreviewCameraActivity extends BasePreviewcameraActivity {

    private final static String TAG = "GLPreviewCameraActivity";

    private MyGLTextureView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_camera_gl);

        setViewsWidgets();
        if(this.url != null) {
            videoView.initRenderer(new SurfaceTextureRenderer.Callback() {
                @Override
                public void onReady(SurfaceTexture surfaceTexture) {
                    prepareSurface();
                    loadVideo();
                }
            });
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
        videoView.getMyRenderer().resetTextureRendered();
    }

    @Override
    public void onVideoStart(int width, int height) {
        super.onVideoStart(width, height);

        videoView.onNewVideoSize(width, height);
        videoView.getMyRenderer().getSurfaceTexture().setDefaultBufferSize(width, height);
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
        return videoView.getMyRenderer().isTextureRendered();
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
        vlcLibrary.prepare(videoView.getMySurfaceTexture());
    }

    @Override
    public void onVideError() {
        super.onVideError();
    }

    @Override
    protected void destroyVlcLibraryObject() {
        super.destroyVlcLibraryObject();
        videoView.getMyRenderer().recreateSurface();
    }
}
