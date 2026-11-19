package pl.huczeq.rtspplayer.player.exo;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.view.Surface;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.media3.common.C;
import androidx.media3.common.Format;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.Tracks;
import androidx.media3.common.VideoSize;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.rtsp.RtspMediaSource;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.util.EventLogger;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.hilt.android.qualifiers.ApplicationContext;
import pl.huczeq.rtspplayer.Settings;
import pl.huczeq.rtspplayer.player.PlayerEventListener;
import pl.huczeq.rtspplayer.player.RtspPlayer;
import pl.huczeq.rtspplayer.player.VideoLayout;

public class ExoPlayerImpl extends RtspPlayer {

    private static final String TAG = "ExoPlayer";
    private final Context context;
    private final ExoPlayer player;

    @OptIn(markerClass = UnstableApi.class) @AssistedInject
    public ExoPlayerImpl(@ApplicationContext Context context, @Assisted String ...args) {
        this.context = context;
        this.player = new ExoPlayer.Builder(context)
                .build();
        this.player.setRepeatMode(Player.REPEAT_MODE_OFF);
        this.player.addListener(new Player.Listener() {

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                Player.Listener.super.onIsPlayingChanged(isPlaying);
                if(eventListeners.isEmpty())
                    return;
                if(isPlaying) {
                    for(PlayerEventListener listener : eventListeners) {
                        listener.onStartRendering();
                    }
                    for(PlayerEventListener listener : eventListeners) {
                        listener.onPlaying();
                    }
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                Player.Listener.super.onPlayerError(error);
                if(eventListeners.isEmpty())
                    return;
                for(PlayerEventListener listener : eventListeners) {
                    listener.onEncounteredError();
                }
            }

            @Override
            public void onPlayerErrorChanged(@Nullable PlaybackException error) {
                Player.Listener.super.onPlayerErrorChanged(error);
                Player.Listener.super.onPlayerError(error);
                if(eventListeners.isEmpty())
                    return;
                for(PlayerEventListener listener : eventListeners) {
                    listener.onEncounteredError();
                }
            }

            @Override
            public void onVideoSizeChanged(VideoSize videoSize) {
                Player.Listener.super.onVideoSizeChanged(videoSize);
                ExoPlayerImpl.this.onNewNewVideoLayout(new VideoLayout(videoSize.width, videoSize.height));
            }
        });

        player.addAnalyticsListener(new EventLogger());
    }

    @Override
    public void loadMedia(RtspMedia rtspMedia) {
        if(player.isPlaying())
            player.stop();

        loadMediaSource(rtspMedia);
        //Alternative:
        /*
        MediaItem mediaItem = MediaItem.fromUri(rtspMedia.getUri());
        player.setMediaItem(mediaItem);
         */

        player.prepare();
    }

    @OptIn(markerClass = UnstableApi.class)
    private void loadMediaSource(RtspMedia rtspMedia) {
        MediaSource mediaSource;

        if("rtsp".equalsIgnoreCase(rtspMedia.getUri().getScheme())) {
            mediaSource =new RtspMediaSource.Factory()
                    .setForceUseRtpTcp(rtspMedia.isForceTcpEnabled())
                    .createMediaSource(MediaItem.fromUri(rtspMedia.getUri()));
        }else {
            mediaSource = new DefaultMediaSourceFactory(context)
                    .createMediaSource(MediaItem.fromUri(rtspMedia.getUri()));
        }

        player.setMediaSource(mediaSource);
    }

    private Format getActiveVideoFormat() {
        for (Tracks.Group trackGroup : player.getCurrentTracks().getGroups()) {
            if (trackGroup.isSelected() && trackGroup.getType() == C.TRACK_TYPE_VIDEO) {
                for (int i = 0; i < trackGroup.length; i++) {
                    if (trackGroup.isTrackSelected(i)) {
                        return trackGroup.getTrackFormat(i);
                    }
                }
            }
        }

        return null;
    }
    protected void notifyVideoInfoIfAvailable() {
        VideoSize videoSize = player.getVideoSize();
        if(videoSize.width <= 0 || videoSize.height <= 0)
            return;
        this.onNewNewVideoLayout(new VideoLayout(videoSize.width, videoSize.height));
    }

    private Surface surface = null;
    @Override
    public boolean isSurfaceAttached() {
        return surface != null;
    }

    @Override
    public void attachSurface(SurfaceTexture surfaceTexture) {
        if(isSurfaceAttached()) {
           detachSurface();
        }
        this.surface = new Surface(surfaceTexture);
        player.setVideoSurface(surface);
    }

    @Override
    public void detachSurface() {
        player.clearVideoSurface();
        this.surface.release();
        this.surface = null;
    }

    @Override
    public void play() {
        if(player.getPlaybackState() == Player.STATE_IDLE)
            player.prepare();
        player.play();
    }

    @Override
    public void pause() {
        player.pause();
    }

    @Override
    public void stop() {
        player.stop();
    }

    @Override
    public void release() {
        player.release();
    }

    @Override
    public boolean isPlaying() {
        return this.player.isPlaying();
    }

    @Override
    public boolean isMute() {
        return this.player.getVolume() == 0;
    }

    @Override
    public void setMute(boolean isMuted) {
        this.player.setVolume(isMuted? 0 : 1);
    }

    @AssistedFactory
    public interface Factory {
        ExoPlayerImpl build(String ...args);
    }
}
