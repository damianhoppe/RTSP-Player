package pl.huczeq.rtspplayer.ui.views.player;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;

import pl.huczeq.rtspplayer.ui.renderers.SurfaceTextureRenderer;
import pl.huczeq.rtspplayer.ui.renderers.SurfaceTextureRenderer.Callback;
import pl.huczeq.rtspplayer.ui.views.player.GLTextureView;

public class MyGLTextureView extends GLTextureView {

    protected Matrix customMatrix;

    private SurfaceTextureRenderer renderer;

    public MyGLTextureView(Context context) {
        super(context);
        init(context);
    }

    public MyGLTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        customMatrix = new Matrix();
    }

    public void initRenderer(Callback callback) {
        renderer = new SurfaceTextureRenderer();
        renderer.callback = callback;
        setRenderer(renderer);
    }

    public SurfaceTexture getMySurfaceTexture() {
        return this.renderer.getSurfaceTexture();
    }

    public SurfaceTextureRenderer getMyRenderer() {
        return this.renderer;
    }
}