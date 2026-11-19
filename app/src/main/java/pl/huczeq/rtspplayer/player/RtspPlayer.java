package pl.huczeq.rtspplayer.player;

import android.graphics.SurfaceTexture;
import android.net.Uri;

import androidx.collection.ArraySet;

import java.util.Set;

import lombok.Getter;
import pl.huczeq.rtspplayer.util.UrlUtils;
import pl.huczeq.rtspplayer.ui.player.view.PlayerView;

public abstract class RtspPlayer {

    protected Set<PlayerEventListener> eventListeners = new ArraySet<>();
    private PlayerView attachedPlayerView;
    private VideoInfoChangeListener videoInfoChangeListener;

    private final PlayerView.SurfaceLifecycleListener surfaceLifecycleListener = new PlayerView.SurfaceLifecycleListener() {
        @Override
        public void onSurfaceCreated(SurfaceTexture surfaceTexture) {
            if(isSurfaceAttached())
                detachSurface();
            attachSurface(surfaceTexture);
        }
    };

    public void registerEventListener(PlayerEventListener eventListener) {
        eventListeners.add(eventListener);
    }

    public void unregisterEventListener(PlayerEventListener eventListener) {
        this.eventListeners.remove(eventListener);
    }

    public abstract void loadMedia(RtspMedia rtspMedia);
    public final boolean isViewAttached() {
        return attachedPlayerView != null;
    }
    public final void attachView(PlayerView view) {
        if(isViewAttached())
            detachView();
        this.attachedPlayerView = view;
        registerEventListener(view.getPlayerEventListener());
        view.registerSurfaceLifecycleListener(this.surfaceLifecycleListener, true);
        this.videoInfoChangeListener = view.getVideoInfoChangeListener();
        if(this.videoInfoChangeListener != null) {
            this.notifyVideoInfoIfAvailable();
        }
    }
    public final void detachView() {
        if(isSurfaceAttached())
            this.detachSurface();
        unregisterEventListener(attachedPlayerView.getPlayerEventListener());
        this.attachedPlayerView.unregisterSurfaceLifecycleListener(this.surfaceLifecycleListener);
        this.attachedPlayerView = null;
        this.videoInfoChangeListener = null;
    }
    protected final void onNewNewVideoLayout(VideoLayout videoLayout) {
        if(!isViewAttached() || this.videoInfoChangeListener == null)
            return;
        this.videoInfoChangeListener.onVideoLayoutChanged(videoLayout);
    }
    protected void notifyVideoInfoIfAvailable() {}
    protected abstract boolean isSurfaceAttached();
    protected abstract void attachSurface(SurfaceTexture surfaceTexture);
    protected abstract void detachSurface();
    public abstract void play();
    public abstract void pause();
    public abstract void stop();
    public abstract void release();
    public abstract boolean isPlaying();
    public abstract boolean isMute();
    public abstract void setMute(boolean isMuted);

    @Getter
    public static class RtspMedia {
        private final Uri uri;
        private final boolean forceTcpEnabled;

        public RtspMedia(Uri uri, boolean forceTcpEnabled) {
            this.uri = UrlUtils.encodedUri(uri.toString());
            this.forceTcpEnabled = forceTcpEnabled;
        }
    }
}
