package pl.huczeq.rtspplayer.ui.player.view.renderer;

public interface PlayerRendererCallback {

    void onSurfaceCreated();
    void onVideoLayoutChanged(int width, int height);
}
