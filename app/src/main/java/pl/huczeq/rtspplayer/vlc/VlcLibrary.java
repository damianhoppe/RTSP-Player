package pl.huczeq.rtspplayer.vlc;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.util.Log;
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

    private static final String TAG = "VlcLibrary";
    private Context context;
    private LibVLC libVLC;
    private MediaPlayer mediaPlayer;
    private IVLCVout vlcVout;

    private SurfaceTexture outputSurface;
    private TextureView outputView;
    private OutputType outputType;

    private Uri uri;

    private Callback callbackListener;

    private boolean initialized;
    private boolean prepared;
    private ArrayList<String> args;

    private Settings settings;

    private Thread stopPlayerThread;
    private Thread releaseLibThread;

    public VlcLibrary(Context context) {
        this.context = context;
        this.initialized = false;
        this.prepared = false;
        this.settings = Settings.getInstance(context);
        int caching = settings.getCachingBufferSize();
        this.args = new ArrayList<String>(Arrays.asList("--vout=android-display", "--file-caching="+caching, "--network-caching="+caching, "--live-caching="+caching, "-vvv"));//TODO CHANGED
    }

    public void init() {
        if(initialized) return;
        this.libVLC = new LibVLC(context, args);
        this.mediaPlayer = new MediaPlayer(libVLC);
        this.vlcVout = this.mediaPlayer.getVLCVout();
        this.mediaPlayer.setEventListener(new MediaPlayer.EventListener() {
            @Override
            public void onEvent(MediaPlayer.Event event) {
                switch (event.type) {
                    case MediaPlayer.Event.Buffering:
                        Log.d(TAG, "Buffering");
                        if(callbackListener != null)
                            callbackListener.onVideoBuffering(event.getBuffering());
                        break;
                    case MediaPlayer.Event.EncounteredError:
                        Log.d(TAG, "EncounteredError");
                        if(callbackListener != null) {
                            callbackListener.onVideError();
                        }
                        break;
                    case MediaPlayer.Event.EndReached:
                        Log.d(TAG, "EndReached");
                        break;
                    case MediaPlayer.Event.ESAdded:
                        Log.d(TAG, "ESAdded");
                        break;
                    case MediaPlayer.Event.ESDeleted:
                        Log.d(TAG, "ESDeleted");
                        break;
                    case MediaPlayer.Event.ESSelected:
                        Log.d(TAG, "ESSelected");
                        break;
                    case MediaPlayer.Event.LengthChanged:
                        Log.d(TAG, "LengthChanged");
                        break;
                    case MediaPlayer.Event.MediaChanged:
                        Log.d(TAG, "MediaChanged");
                        break;
                    case MediaPlayer.Event.Opening:
                        Log.d(TAG, "Opening");
                        break;
                    case MediaPlayer.Event.PausableChanged:
                        Log.d(TAG, "PausableChanged");
                        break;
                    case MediaPlayer.Event.Paused:
                        Log.d(TAG, "Paused");
                        break;
                    case MediaPlayer.Event.Playing:
                        Log.d(TAG, "Playing");
                        break;
                    case MediaPlayer.Event.PositionChanged:
                        Log.d(TAG, "PositionChanged");
                        //Log.d(TAG, String.valueOf(event.getPositionChanged()));
                        break;
                    case MediaPlayer.Event.RecordChanged:
                        Log.d(TAG, "RecordChanged");
                        break;
                    case MediaPlayer.Event.SeekableChanged:
                        Log.d(TAG, "SeekableChanged");
                        break;
                    case MediaPlayer.Event.Stopped:
                        Log.d(TAG, "Stopped");
                        if(callbackListener != null) {
                            callbackListener.onVideStop();
                        }
                        break;
                    case MediaPlayer.Event.TimeChanged:
                        Log.d(TAG, "TimeChanged");
                        break;
                    case MediaPlayer.Event.Vout:
                        Log.d(TAG, "Vout");
                        break;
                }
            }
        });
        this.initialized = true;
    }

    public void release() {
        if(!initialized) return;
        if(vlcVout.areViewsAttached()) vlcVout.detachViews();
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
            }
        }).start();
    }

    public void prepare(SurfaceTexture outputSurface) {
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
       /* if(!this.initialized) throw new IllegalStateException("VlcLibrary is not initialized!");
        if(!this.prepared) throw new IllegalStateException("VlcLibrary is not prepared!");
        if(this.uri == null) throw new IllegalStateException("No data to play!");*/
        if(this.uri == null || mediaPlayer == null) return;
        if (stopPlayerThread != null && stopPlayerThread.isAlive()) {
            try {
                stopPlayerThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        mediaPlayer.play();
    }

    public void pause() {/*
        if(!this.initialized) throw new IllegalStateException("VlcLibrary is not initialized!");
        if(!this.prepared) throw new IllegalStateException("VlcLibrary is not prepared!");*/
        if(mediaPlayer == null) return;
        mediaPlayer.pause();
    }

    public void stop() {
        Log.d(TAG, "onStop");
        /*
        if(!this.initialized) throw new IllegalStateException("VlcLibrary is not initialized!");
        if(!this.prepared) throw new IllegalStateException("VlcLibrary is not prepared!");*/
        if(mediaPlayer == null) return;
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
    }

    public void loadData(Uri uri) {
        if(!this.initialized) throw new IllegalStateException("VlcLibrary is not initialized!");
        if(!this.prepared) throw new IllegalStateException("VlcLibrary is not prepared!");

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
    }

    public void setCallbackListener(Callback callbackListener) {
        this.callbackListener = callbackListener;
    }

    @Override
    public void onNewVideoLayout(IVLCVout vlcVout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
        if(callbackListener != null)
            callbackListener.onVideoStart(width, height);
    }

    public boolean canPlay() {
        return this.initialized && this.prepared;
    }

    public enum OutputType {
        SURFACE_TEXTURE, TEXTURE_VIEW;
    }

    public interface Callback{
        void onVideoStart(int width, int height);
        void onVideError();
        void onVideStop();
        void onVideoBuffering(float buffering);
    }
}
