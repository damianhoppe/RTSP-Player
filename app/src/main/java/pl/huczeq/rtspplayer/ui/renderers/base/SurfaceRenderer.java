package pl.huczeq.rtspplayer.ui.renderers.base;

import android.graphics.SurfaceTexture;

import pl.huczeq.rtspplayer.data.objects.Camera;
import pl.huczeq.rtspplayer.ui.renderers.OnTakeImageCallback;

public interface SurfaceRenderer {
    void takePicture();
    void onConfigurationChanged();
    void onNewVideoSize(int width, int height, int videoWidth, int videoHeight);
    void init(RendererCallback rendererCallback, OnTakeImageCallback takeImageCallback);
    void resetTextureRendered();
    SurfaceTexture getSurfaceTexture();
    void recreateSurface();
}
