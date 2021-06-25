package pl.huczeq.rtspplayer.ui.views.surfaces;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import pl.huczeq.rtspplayer.ui.renderers.GLTextureViewRenderer;
import pl.huczeq.rtspplayer.ui.renderers.OnTakeImageCallback;
import pl.huczeq.rtspplayer.ui.renderers.base.RendererCallback;
import pl.huczeq.rtspplayer.ui.renderers.base.SurfaceRenderer;
import pl.huczeq.rtspplayer.ui.views.surfaces.base.BaseSurfaceView;
import pl.huczeq.rtspplayer.ui.views.surfaces.base.GLTextureView;

public class MyGLTextureView extends GLTextureView implements BaseSurfaceView {

    private GLTextureViewRenderer renderer;

    public MyGLTextureView(Context context) {
        super(context);
        init();
    }

    public MyGLTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        this.renderer = new GLTextureViewRenderer();
    }

    public void initRenderer(RendererCallback rendererCallback, OnTakeImageCallback takeImageCallback) {
        renderer.init(rendererCallback, takeImageCallback);
        setRenderer(this.renderer);
    }


    public void takePicture() {
        this.renderer.takePicture();
    }

    @Override
    public void onNewVideoSize(int width, int height, int videoWidth, int videoHeight) {
        super.onNewVideoSize(width, height, videoWidth, videoHeight);
        renderer.onNewVideoSize(width, height, videoWidth, videoHeight);
    }


    public SurfaceRenderer getSurfaceRenderer() {
        return this.renderer;
    }
}