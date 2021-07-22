package pl.huczeq.rtspplayer.player.exo;

import android.content.Context;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.rtsp.RtspMediaSource;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.video.VideoSize;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.hilt.android.qualifiers.ApplicationContext;
import pl.huczeq.rtspplayer.Settings;
import pl.huczeq.rtspplayer.player.RtspPlayer;
import pl.huczeq.rtspplayer.ui.player.view.PlayerSurfaceView;
import pl.huczeq.rtspplayer.player.VideoLayout;

public class ExoPlayerImpl extends RtspPlayer {

    private static final String TAG = "ExoPlayer";

    private ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat(TAG + "-THREAD").build());
    private Context context;
    private Settings settings;

    private ExoPlayer player;

    @AssistedInject
    public ExoPlayerImpl(@ApplicationContext Context context, Settings settings, @Assisted String ...args) {
        this.context = context;
        this.settings = settings;
        this.player = new ExoPlayer.Builder(context).build();
        this.player.setRepeatMode(Player.REPEAT_MODE_OFF);
        this.player.addListener(new Player.Listener() {

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                Player.Listener.super.onIsPlayingChanged(isPlaying);
                if(eventListener == null)
                    return;
                if(isPlaying) {
                    eventListener.onStartRendering();
                    eventListener.onPlaying();
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                Player.Listener.super.onPlayerError(error);
                if(eventListener == null)
                    return;
                eventListener.onEncounteredError();
            }

            @Override
            public void onPlayerErrorChanged(@Nullable PlaybackException error) {
                Player.Listener.super.onPlayerErrorChanged(error);
                Player.Listener.super.onPlayerError(error);
                if(eventListener == null)
                    return;
                eventListener.onEncounteredError();
            }

            @Override
            public void onVideoSizeChanged(VideoSize videoSize) {
                Player.Listener.super.onVideoSizeChanged(videoSize);
                if(surfaceSizeChangedListener == null)
                    return;
                surfaceSizeChangedListener.onVideoLayoutChanged(new VideoLayout(videoSize.width, videoSize.height));
            }
        });
    }

    @Override
    public void loadMedia(RtspMedia rtspMedia) {
        if(player.isPlaying())
            player.stop();
        MediaSource mediaSource;
        if(rtspMedia.getUri().toString().startsWith("http://") || rtspMedia.getUri().toString().startsWith("https://")) {
            mediaSource = new DefaultMediaSourceFactory(context)
                    .createMediaSource(MediaItem.fromUri(rtspMedia.getUri()));
        }else {
            mediaSource = new RtspMediaSource.Factory()
                    .setForceUseRtpTcp(rtspMedia.isForceTcpEnabled())
                    .createMediaSource(MediaItem.fromUri(rtspMedia.getUri()));
        }
        player.setMediaSource(mediaSource);
        player.prepare();
    }

    private boolean areViewsAttached = false;

    @Override
    public boolean isViewAttached() {
        return areViewsAttached;
    }

    @Override
    public void attachView(PlayerSurfaceView view) {
        if(isViewAttached()) {
           throw new RuntimeException("View is attached.");
        }
        Surface surface = new Surface(view.getSurfaceTexture());
        player.setVideoSurface(surface);
        areViewsAttached = true;
        surfaceSizeChangedListener = view;
        if(player.getVideoFormat() == null)
            return;
        if(surfaceSizeChangedListener != null)
            surfaceSizeChangedListener.onVideoLayoutChanged(new VideoLayout(player.getVideoFormat().width, player.getVideoFormat().height));
    }

    @Override
    public void detachView() {
        player.clearVideoSurface();
    }

    @Override
    public void play() {
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
