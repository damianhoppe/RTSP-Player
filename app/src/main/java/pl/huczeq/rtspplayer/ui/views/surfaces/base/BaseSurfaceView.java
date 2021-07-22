package pl.huczeq.rtspplayer.ui.views.surfaces.base;

import pl.huczeq.rtspplayer.ui.renderers.OnTakeImageCallback;
import pl.huczeq.rtspplayer.ui.renderers.base.RendererCallback;
import pl.huczeq.rtspplayer.ui.renderers.base.SurfaceRenderer;

public interface BaseSurfaceView {
    void initRenderer(RendererCallback rendererCallback, OnTakeImageCallback takeImageCallback);
    void takePicture();
    void onConfigurationChanged();
    void onNewVideoSize(int width, int height, int videoWidth, int videoHeight);
    SurfaceRenderer getSurfaceRenderer();
}
