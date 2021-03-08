package pl.huczeq.rtspplayer.ui.views;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.interfaces.IVLCVout;

import java.util.ArrayList;
import java.util.Arrays;

import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.data.Settings;
import pl.huczeq.rtspplayer.ui.renderers.SurfaceTextureRenderer;
import pl.huczeq.rtspplayer.ui.views.player.MyGLTextureView;

public class OldVideoView extends MyGLTextureView implements IVLCVout.OnNewVideoLayoutListener, IVLCVout.Callback {

    public interface Callback {
        void onVideoStart();
        void onVideError();
        void onVideStop();
        void onVideoBuffering(float buffering);
    }

    private final String TAG = "VideoView";

    private int caching = Settings.getInstance(getContext()).getCachingBufferSize();
    private ArrayList<String> args = new ArrayList<String>(Arrays.asList("--vout=android-display", "--file-caching="+caching, "--network-caching="+caching, "--live-caching="+caching, "-vvv"));//TODO CHANGED
    private LibVLC lib;

    private MediaPlayer player;
    private Uri uri;
    private Media media;
    private boolean urlSetted;

    private Callback callback;
    private boolean isSurfaceCreated = false;

    int width = 1;
    int height = 1;

    SurfaceTextureRenderer renderer;

    public OldVideoView(Context context) {
        super(context);
        onCreate(context);
    }

    public OldVideoView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        onCreate(context);
    }
/*
    public OldVideoView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onCreate(context);
    }

    public OldVideoView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        onCreate(context);
    }*/

    public void onCreate(Context context) {
        Log.d(TAG, "Caching buffer size: " + caching);
        lib = new LibVLC(context, args);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void setData(String url) {
        this.setData(Uri.parse(url));
    }

    public void setData(Uri u) {
        this.uri = u;
        if(this.uri == null) {
            Toast.makeText(getContext(), getResources().getString(R.string.incorrect_camera_url), Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, this.uri.toString());
        player = new MediaPlayer(lib);

        IVLCVout vOut = player.getVLCVout();

        vOut.setVideoView(this);
        vOut.addCallback(this);
        if(vOut.areViewsAttached()) {
            vOut.detachViews();
        }
        vOut.attachViews(this);
        if(isSurfaceCreated) {
            setMedia();
        }

        ViewGroup.LayoutParams params = getLayoutParams();
        Log.d(TAG, params.width + " : " + params.height);

        vOut.setWindowSize(params.width, params.height);
        urlSetted = true;

        player.setEventListener(new MediaPlayer.EventListener() {
            @Override
            public void onEvent(MediaPlayer.Event event) {
                switch (event.type) {
                    case MediaPlayer.Event.Buffering:
                        Log.d(TAG, "Buffering");
                        if(callback != null)
                            callback.onVideoBuffering(event.getBuffering());
                        break;
                    case MediaPlayer.Event.EncounteredError:
                        Log.d(TAG, "EncounteredError");
                        if(callback != null) {
                            callback.onVideError();
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
                        if(callback != null) {
                            callback.onVideStop();
                        }
                        break;
                    case MediaPlayer.Event.TimeChanged:
                        Log.d(TAG, "TimeChanged");
                        break;
                    case MediaPlayer.Event.Vout:
                        Log.d(TAG, "Vout");
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                OldVideoView.this.canTakePicture = true;
                            }
                        }).start();
                        break;
                }
            }
        });
        //Matrix matrix = new Matrix();
        //matrix.setTranslate(-1,-1);
        //matrix.postScale(2,2);
        //this.setTransform(matrix);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateOutputSize(newConfig);
    }
    @Override
    public void onSurfacesCreated(IVLCVout vlcVout) {
        Log.d(TAG, "onSurfacesCreated");
        setMedia();
    }

    @Override
    public void onSurfacesDestroyed(IVLCVout vlcVout) {
        Log.d(TAG, "onSurfacesDestroyed");
    }

    public void setMedia() {
        this.isSurfaceCreated = true;
        media = new Media(lib, uri);
        if (args != null) {
            for (String s : args) {
                media.addOption(s);
            }
        }
        //media.addOption(":network-caching=150");
        media.addOption("--clock-jitter=0");
        media.addOption("--clock-synchro=0");
        media.addOption("--no-drop-late-frames");
        media.addOption("--fps-fps=60");
        if(Settings.getInstance(getContext()).isEnabledAVCodes()) {
            media.addOption("--avcodes-fast");
            media.addOption("--avcodes-threads=1");
        }
        media.setHWDecoderEnabled(Settings.getInstance(getContext()).isEnabledHardwareAcceleration(), false);
        player.setMedia(media);
        media.release();
    }

    public void updateOutputSize() {
        updateOutputSize(this.getResources().getConfiguration());
    }

    public void updateOutputSize(Configuration newConfig) {
        DisplayMetrics dMetrics = getContext().getResources().getDisplayMetrics();

        View v = (View)getParent();
        v.getWidth();
        int pWidth = dMetrics.widthPixels;
        int pHeight = dMetrics.heightPixels;

        ViewGroup.LayoutParams params = getLayoutParams();
        Log.d(TAG, "onConfigurationChanged1: " + params.width + " x " + params.height);
        if(player == null || player.getVLCVout() == null)
            return;
        //player.getVLCVout().setWindowSize(params.width, params.height);
        params.width = pWidth;
        params.height = pHeight;
        this.setLayoutParams(params);

        float mVideoWidth = width;
        float mVideoHeight = height;
        final float viewHeight = params.height;
        final float viewWidth = params.width;
        if(mVideoWidth == 0 || mVideoHeight == 0 || viewHeight == 0 || viewWidth == 0) return;
        float scaleX = 1, scaleY = 1;
        /*if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

            params.height = pHeight;
            params.width = params.height * this.width/this.height;
        } else{
            params.width = pWidth;
            params.height = params.width * this.height/this.width;
        }*/
        if(viewHeight < mVideoHeight) {
            scaleX = (viewHeight / mVideoHeight) / (viewWidth / mVideoWidth);
        }else if(viewWidth < mVideoWidth){
            scaleY = (viewWidth / mVideoWidth) / (viewHeight / mVideoHeight);
        }

        Matrix matrix = new Matrix();
        matrix.postScale(scaleX,scaleY,viewWidth/2,viewHeight/2);
        setTransform(matrix);
        this.customMatrix = matrix;
        //setDisplayMetrics(width, height);
        //player.getVLCVout().setWindowSize(width, height);
        //this.invalidate();
    }

    public void onConfigurationChanged(int degress) {
        DisplayMetrics dMetrics = getContext().getResources().getDisplayMetrics();
        ViewGroup.LayoutParams params = getLayoutParams();
        Log.d(TAG, "onConfigurationChanged2: " + params.width + " x " + params.height);
        if(degress == 90 || degress == 270) {
            params.width = dMetrics.widthPixels*this.width/this.height;
            params.height = dMetrics.heightPixels;//TODO ASPECT RATION calulate
            //setupMatrix(params.width, params.height, degress);
            //setupMatrix(dMetrics.widthPixels, dMetrics.heightPixels, degress);
        }else {
            params.width = dMetrics.widthPixels;
            params.height = (int)((float)params.width*((float)this.height/(float)this.width));
            //setupMatrix(dMetrics.widthPixels, dMetrics.heightPixels, degress);
        }
        setupMatrix(params.width, params.height, degress);
        Log.d(TAG, params.width + " x " + params.height);
        player.getVLCVout().setWindowSize(params.width, params.height);
        this.setLayoutParams(params);
    }

    private void setupMatrix(int width, int height, int degrees) {
        Log.d(TAG, "setupMatrix for " + degrees + " degrees");
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees, width/2, height/2);

        this.setTransform(new Matrix());
        this.setTransform(matrix);
    }

    private boolean canTakePicture = false;
    public boolean canTakePicture() {
        return canTakePicture;
    }

    public void play() {
        Log.d(TAG, "play");
        if(!urlSetted || this.uri == null) {
            return;
        }
        player.play();
        //setKeepScreenOn(true);
    }

    public void pause() {
        Log.d(TAG, "Pause 1. " + player.isPlaying());
        if(player != null && player.isPlaying()) player.pause();
        Log.d(TAG, "Pause 2. " + player.isPlaying());
    }
    Thread thread;
    public void stop() {
        Log.d(TAG, "stop");
        player.stop();
        //setKeepScreenOn(false);
    }

    public void release() {
        this.setCallback(null);
        player.setEventListener(null);
        player.getVLCVout().removeCallback(this);
        player.getVLCVout().detachViews();
        lib.release();
        new Thread(new Runnable() {
            @Override
            public void run() {
                player.release();
            }
        }).start();
    }

    public ArrayList<String> getArgs() {
        return this.args;
    }

    public void setArgs(ArrayList<String> args) {
        this.args = args;
    }

    public MediaPlayer getPlayer() {
        return this.player;
    }

    @Override
    public void onNewVideoLayout(IVLCVout vlcVout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
        this.setVisibility(VISIBLE);
        Log.d(TAG, String.valueOf(visibleWidth) + "x" + String.valueOf(visibleHeight));
        DisplayMetrics dMetrics = getContext().getResources().getDisplayMetrics();
        ViewGroup.LayoutParams params = getLayoutParams();
        if(visibleHeight == 0 || visibleWidth == 0) return;
        if(params.height < visibleHeight) {
            params.height = dMetrics.heightPixels;
            params.width = (int) (params.height * ((float) visibleWidth/(float)visibleHeight));
            /*params.width = dMetrics.widthPixels;
            params.height = (int) ((float) params.width * ((float) visibleHeight / (float) visibleWidth));*/
        }else {
            params.width = dMetrics.widthPixels;
            params.height = (int) ((float) params.width * ((float) visibleHeight / (float) visibleWidth));
        }

        Log.d(TAG, "Aspect ration: " + ((float)visibleHeight/(float)visibleWidth));

        this.width = width;
        this.height = height;

        updateOutputSize(this.getResources().getConfiguration());
        //getSurfaceTexture().setDefaultBufferSize(width, height);

        if(this.callback != null)
            callback.onVideoStart();
    }

    public Bitmap getMyBitmap() {
        Matrix matrix = new Matrix();
        getTransform(matrix);
        setTransform(new Matrix());
        Bitmap bitmap = getBitmap(width, height);
        setTransform(matrix);
        return bitmap;
    }
}
