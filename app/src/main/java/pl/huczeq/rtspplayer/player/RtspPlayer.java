package pl.huczeq.rtspplayer.player;

import android.net.Uri;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.huczeq.rtspplayer.ui.player.view.PlayerSurfaceView;

public abstract class RtspPlayer {

    protected PlayerEventListener eventListener;
    protected OnVideoLayoutChanged surfaceSizeChangedListener;

    public void setEventListener(PlayerEventListener eventListener) {
        this.eventListener = eventListener;
    }
    protected void setOnSurfaceSizeChangedListener(OnVideoLayoutChanged surfaceSizeChangedListener) {
        this.surfaceSizeChangedListener = surfaceSizeChangedListener;
    }

    public abstract void loadMedia(RtspMedia rtspMedia);
    public abstract boolean isViewAttached();
    public abstract void attachView(PlayerSurfaceView view);
    public abstract void detachView();
    public abstract void play();
    public abstract void pause();
    public abstract void stop();
    public abstract void release();
    public abstract boolean isPlaying();
    public abstract boolean isMute();
    public abstract void setMute(boolean isMuted);

    @AllArgsConstructor
    @Getter
    public static class RtspMedia {
        private Uri uri;
        private boolean forceTcpEnabled;
    }
}
