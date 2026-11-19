package pl.huczeq.rtspplayer.player.vlc;

import android.content.Context;
import android.graphics.SurfaceTexture;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.interfaces.IVLCVout;

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

public class VlcPlayerImpl extends RtspPlayer {

    private static final String TAG = "VlcRtspPlayer";

    private ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat(TAG + "-THREAD").build());
    private Settings settings;

    private LibVLC libVLC;
    private MediaPlayer mediaPlayer;
    private IVLCVout vlcOut;
    private final IVLCVout.OnNewVideoLayoutListener onNewVideoLayout = new IVLCVout.OnNewVideoLayoutListener() {
        @Override
        public void onNewVideoLayout(IVLCVout vlcVout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
            onNewNewVideoLayout(new VideoLayout(width, height, visibleWidth, visibleHeight));
        }
    };

    @AssistedInject
    public VlcPlayerImpl(@ApplicationContext Context context, Settings settings, @Assisted String ...args) {
        this.settings = settings;

        this.libVLC = VlcFactory.buildLibVLC(context, settings, args);
        this.mediaPlayer = new MediaPlayer(libVLC);
        this.vlcOut = mediaPlayer.getVLCVout();

        mediaPlayer.setVolume(100);
        mediaPlayer.setEventListener(new MediaPlayer.EventListener() {
            @Override
            public void onEvent(MediaPlayer.Event event) {
                if(eventListeners.isEmpty())
                    return;

                switch (event.type) {
                    case MediaPlayer.Event.Vout:
                        if(event.getVoutCount() > 0)
                            for(PlayerEventListener listener : eventListeners) {
                                listener.onStartRendering();
                            }
                        break;
                    case MediaPlayer.Event.Playing:
                        for(PlayerEventListener listener : eventListeners) {
                            listener.onPlaying();
                        }
                        break;
                    case MediaPlayer.Event.EndReached:
                        for(PlayerEventListener listener : eventListeners) {
                            listener.onEndReached();
                        }
                        break;
                    case MediaPlayer.Event.EncounteredError:
                        for(PlayerEventListener listener : eventListeners) {
                            listener.onEncounteredError();
                        }
                        break;
                }
            }
        });
    }

    @Override
    public void loadMedia(RtspMedia rtspMedia) {
        executor.execute(() -> {
            if(libVLC.isReleased())
                return;
            Media vlcMedia = new Media(libVLC, rtspMedia.getUri());
            initMedia(vlcMedia, rtspMedia);
            if(mediaPlayer.isPlaying())
                mediaPlayer.stop();
            mediaPlayer.setMedia(vlcMedia);
            vlcMedia.release();
        });
    }

    private void initMedia(Media media, RtspMedia rtspMedia) {
        if(rtspMedia.isForceTcpEnabled())
            media.addOption("--rtsp-tcp");

        int cachingBufferSize = settings.getCachingBufferSize();
        media.addOption("--live-caching=" + cachingBufferSize);
        media.addOption("--network-caching=" + cachingBufferSize);
        media.addOption("--file-caching=" + cachingBufferSize);

        media.setHWDecoderEnabled(settings.isEnabledHardwareAcceleration(), settings.isEnabledHardwareAcceleration());
    }

    @Override
    public boolean isSurfaceAttached() {
        return vlcOut.areViewsAttached();
    }

    @Override
    public void attachSurface(SurfaceTexture surfaceTexture) {
        if(isSurfaceAttached()) {
           throw new RuntimeException("View is attached.");
        }
        vlcOut.setVideoSurface(surfaceTexture);
        vlcOut.attachViews(this.onNewVideoLayout);
    }

    @Override
    public void detachSurface() {
        mediaPlayer.detachViews();
    }

    @Override
    public void play() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                mediaPlayer.play();
            }
        });
    }

    @Override
    public void pause() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                mediaPlayer.pause();
            }
        });
    }

    @Override
    public void stop() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                mediaPlayer.stop();
            }
        });
    }

    @Override
    public void release() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if(!mediaPlayer.isReleased())
                    mediaPlayer.release();
                if(!libVLC.isReleased())
                    libVLC.release();
            }
        });
    }

    @Override
    public boolean isPlaying() {
        return this.mediaPlayer.isPlaying();
    }

    @Override
    public boolean isMute() {
        return this.mediaPlayer.getVolume() == 0;
    }

    @Override
    public void setMute(boolean isMuted) {
        this.mediaPlayer.setVolume(isMuted? 0 : 100);
    }

    @AssistedFactory
    public interface Factory {
        VlcPlayerImpl build(String ...args);
    }
}
