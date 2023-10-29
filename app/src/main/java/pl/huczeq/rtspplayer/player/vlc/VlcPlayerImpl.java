package pl.huczeq.rtspplayer.player.vlc;

import android.content.Context;
import android.util.Log;

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
import pl.huczeq.rtspplayer.player.RtspPlayer;
import pl.huczeq.rtspplayer.player.VideoLayout;
import pl.huczeq.rtspplayer.ui.player.view.PlayerSurfaceView;

public class VlcPlayerImpl extends RtspPlayer implements IVLCVout.OnNewVideoLayoutListener {

    private static final String TAG = "VlcRtspPlayer";

    private ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat(TAG + "-THREAD").build());
    private Settings settings;

    private LibVLC libVLC;
    private MediaPlayer mediaPlayer;
    private IVLCVout vlcOut;

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
                if(eventListener == null)
                    return;

                switch (event.type) {
                    case MediaPlayer.Event.Vout:
                        eventListener.onStartRendering();
                        break;
                    case MediaPlayer.Event.Playing:
                        eventListener.onPlaying();
                        break;
                    case MediaPlayer.Event.EndReached:
                        eventListener.onEndReached();
                        break;
                    case MediaPlayer.Event.EncounteredError:
                        eventListener.onEncounteredError();
                        break;
                }
            }
        });
    }

    @Override
    public void loadMedia(RtspMedia rtspMedia) {
        Media vlcMedia = new Media(libVLC, rtspMedia.getUri());
        initMedia(vlcMedia, rtspMedia);
        executor.execute(() -> {
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
    public boolean isViewAttached() {
        return vlcOut.areViewsAttached();
    }

    @Override
    public void attachView(PlayerSurfaceView view) {
        if(isViewAttached()) {
           throw new RuntimeException("View is attached.");
        }
        surfaceSizeChangedListener = view;
        vlcOut.setVideoSurface(view.getSurfaceTexture());
        vlcOut.attachViews(this);
    }

    @Override
    public void detachView() {
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

    @Override
    public void onNewVideoLayout(IVLCVout vlcVout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
        if(surfaceSizeChangedListener == null)
            return;
        surfaceSizeChangedListener.onVideoLayoutChanged(new VideoLayout(width, height, visibleWidth, visibleHeight));
    }

    @AssistedFactory
    public interface Factory {
        VlcPlayerImpl build(String ...args);
    }
}
