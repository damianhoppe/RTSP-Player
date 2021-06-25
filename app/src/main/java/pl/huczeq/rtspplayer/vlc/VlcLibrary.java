package pl.huczeq.rtspplayer.vlc;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.interfaces.IVLCVout;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;

import pl.huczeq.rtspplayer.data.Settings;
import pl.huczeq.rtspplayer.ui.views.OldVideoView;

public class VlcLibrary implements IVLCVout.OnNewVideoLayoutListener {

    private static final boolean ASYNC = true;

    private static final String TAG = "VlcLibrary";
    private Context context;
    private LibVLC libVLC;
    public MediaPlayer mediaPlayer;
    private IVLCVout vlcVout;

    private SurfaceTexture outputSurface;
    private TextureView outputView;
    private SurfaceView outputSurfaceView;
    private OutputType outputType;

    private Uri uri;

    private Callback callbackListener;

    private boolean initialized;
    private boolean prepared;
    private boolean released;
    private ArrayList<String> args;

    private Settings settings;

    private Thread stopPlayerThread;

    public VlcLibrary(Context context) {
        this.context = context;
        this.initialized = false;
        this.prepared = false;
        this.released = true;
        this.settings = Settings.getInstance(context);
        int caching = settings.getCachingBufferSize();
        this.args = new ArrayList<String>(Arrays.asList("--vout=android-display", "--file-caching="+caching, "--network-caching="+caching, "--live-caching="+caching, "-vvv"));//TODO CHANGED
    }

    public void init() {
        Log.d(TAG, "init");
        if(initialized) return;
        this.libVLC = new LibVLC(context, args);
        this.mediaPlayer = new MediaPlayer(libVLC);
        this.vlcVout = this.mediaPlayer.getVLCVout();
        this.mediaPlayer.setEventListener(new MediaPlayer.EventListener() {
            @Override
            public void onEvent(MediaPlayer.Event event) {
                switch (event.type) {
                    case MediaPlayer.Event.Buffering:
                        if(callbackListener != null)
                            callbackListener.onVideoBuffering(event.getBuffering());
                        break;
                    case MediaPlayer.Event.EncounteredError:
                        if(callbackListener != null) {
                            callbackListener.onVideError();
                        }
                        break;
                    case MediaPlayer.Event.EndReached:
                        if(callbackListener != null)
                            callbackListener.onEndReached();
                        break;
                    case MediaPlayer.Event.Stopped:
                        Log.d(TAG, "Stopped");
                        if(callbackListener != null) {
                            callbackListener.onVideStop();
                        }
                        break;
                }
            }
        });
        this.initialized = true;
    }

    public void release() {
        if(!initialized) return;
        if(vlcVout.areViewsAttached()) vlcVout.detachViews();
        if(ASYNC) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (stopPlayerThread != null && stopPlayerThread.isAlive()) {
                        try {
                            stopPlayerThread.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    mediaPlayer.setEventListener(null);
                    mediaPlayer.release();
                    mediaPlayer = null;
                    libVLC.release();
                    libVLC = null;
                    initialized = false;
                    prepared = false;
                    released = true;
                    Log.d(TAG, "released in Thread");
                }
            }).start();
        }else {
            mediaPlayer.setEventListener(null);
            mediaPlayer.release();
            mediaPlayer = null;
            libVLC.release();
            libVLC = null;
            initialized = false;
            prepared = false;
            released = true;
        }
    }

    public void prepare(SurfaceView outputSurface) {
        Log.d(TAG, "prepare");
        if(!this.initialized) throw new IllegalStateException("VlcLibrary is not initialized!");

        this.outputType = OutputType.SURFACE_VIEW;
        this.outputSurfaceView = outputSurface;

        if(!vlcVout.areViewsAttached()) {
            vlcVout.setVideoView(this.outputSurfaceView);
            vlcVout.attachViews(this);
        }
        this.prepared = true;
    }

    public void prepare(SurfaceTexture outputSurface) {
        Log.d(TAG, "prepare");
        if(!this.initialized) throw new IllegalStateException("VlcLibrary is not initialized!");

        this.outputType = OutputType.SURFACE_TEXTURE;
        this.outputSurface = outputSurface;

        if(!vlcVout.areViewsAttached()) {
            vlcVout.setVideoSurface(this.outputSurface);
            vlcVout.attachViews(this);
        }
        this.prepared = true;
    }

    public void prepare(TextureView outputView) {
        Log.d(TAG, "prepare");
        if(!this.initialized) throw new IllegalStateException("VlcLibrary is not initialized!");

        this.outputType = OutputType.TEXTURE_VIEW;
        this.outputView = outputView;

        if(!vlcVout.areViewsAttached()) {
            vlcVout.setVideoView(this.outputView);
            vlcVout.attachViews(this);
        }
        this.prepared = true;
    }

    public void play() {
        Log.d(TAG, "play");
       /* if(!this.initialized) throw new IllegalStateException("VlcLibrary is not initialized!");
        if(!this.prepared) throw new IllegalStateException("VlcLibrary is not prepared!");
        if(this.uri == null) throw new IllegalStateException("No data to play!");*/
        if(this.uri == null || mediaPlayer == null) return;
        if(ASYNC) {
            if (stopPlayerThread != null && stopPlayerThread.isAlive()) {
                try {
                    stopPlayerThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        mediaPlayer.play();
    }

    public void pause() {
        Log.d(TAG, "pause");/*
        if(!this.initialized) throw new IllegalStateException("VlcLibrary is not initialized!");
        if(!this.prepared) throw new IllegalStateException("VlcLibrary is not prepared!");*/
        if(mediaPlayer == null) return;
        mediaPlayer.pause();
    }

    public void stop() {
        Log.d(TAG, "stop");
        /*
        if(!this.initialized) throw new IllegalStateException("VlcLibrary is not initialized!");
        if(!this.prepared) throw new IllegalStateException("VlcLibrary is not prepared!");*/
        if(mediaPlayer == null) return;
        if(ASYNC) {
            if (stopPlayerThread == null || stopPlayerThread.isAlive()) {
                stopPlayerThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "onStop in Thread");
                        mediaPlayer.stop();
                        Log.d(TAG, "onStop end in Thread");
                    }
                });
                stopPlayerThread.start();
            }
        }else {
            mediaPlayer.stop();
        }
    }

    public void loadData(Uri uri) {
        Log.d(TAG, "loadData");
        if(!this.initialized) throw new IllegalStateException("VlcLibrary is not initialized!");
        if(!this.prepared) throw new IllegalStateException("VlcLibrary is not prepared!");
        if(!this.released) throw new IllegalStateException("VlcLibrary is not released!");

        this.uri = uri;
        Media media = new Media(libVLC, uri);
        for (String s : args) {
                media.addOption(s);
        }
        media.addOption("--clock-jitter=0");
        media.addOption("--clock-synchro=0");
        media.addOption("--no-drop-late-frames");
        media.addOption("--fps-fps=60");
        if(settings.isEnabledAVCodes()) {
            media.addOption("--avcodes-fast");
            media.addOption("--avcodes-threads=1");
        }
        media.setHWDecoderEnabled(settings.isEnabledHardwareAcceleration(), false);
        mediaPlayer.setMedia(media);
        media.release();
        this.released = false;
    }

    public void setCallbackListener(Callback callbackListener) {
        this.callbackListener = callbackListener;
    }

    @Override
    public void onNewVideoLayout(IVLCVout vlcVout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
        //Log.d(TAG, "" + width + "x" + height + " / " + visibleWidth + "x" + visibleHeight + " / " + sarNum + " / " + sarDen);
        if(callbackListener != null)
            callbackListener.onVideoStart(width, height, visibleWidth, visibleHeight);
    }

    public void releaseMediaPlayer() {
        vlcVout.detachViews();
        mediaPlayer.detachViews();
        mediaPlayer.setEventListener(null);
        mediaPlayer.release();
        libVLC.release();
    }

    public boolean canPlay() {
        return this.initialized && this.prepared;
    }

    public boolean isReleased() {
        return this.released;
    }

    public boolean isInitialized() {
        return this.initialized;
    }

    public boolean isPlaying() {
        if(this.mediaPlayer == null) return false;
        return this.mediaPlayer.isPlaying();
    }

    public enum OutputType {
        SURFACE_TEXTURE, TEXTURE_VIEW, SURFACE_VIEW;
    }

    public interface Callback{
        void onVideoStart(int width, int height, int videoWidth, int videoHeight);
        void onVideError();
        void onVideStop();
        void onVideoBuffering(float buffering);
        void onEndReached();
    }
}
