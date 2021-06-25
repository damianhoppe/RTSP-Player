package pl.huczeq.rtspplayer.ui.renderers.base;

import android.graphics.SurfaceTexture;

public interface RendererCallback {
    void onReady(SurfaceTexture surfaceTexture);
}