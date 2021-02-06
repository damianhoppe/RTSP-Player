package pl.huczeq.rtspplayer.ui.views;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.TextureView;
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

public class OldVideoView extends TextureView implements IVLCVout.OnNewVideoLayoutListener {

    public interface Callback {
        void onVideoStart();
        void onVideError();
    }

    private final String TAG = "VideoView";

    ArrayList<String> args = new ArrayList<String>(Arrays.asList("--vout=android-display", "--file-caching=150", "-vvv"));//TODO CHANGED
    LibVLC lib;

    public MediaPlayer player;
    Uri uri;
    Media media;
    boolean urlSetted;

    Callback callback;
    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public OldVideoView(Context context) {
        super(context);
        onCreate(context);
    }

    public OldVideoView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        onCreate(context);
    }

    public OldVideoView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onCreate(context);
    }

    public OldVideoView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        onCreate(context);
    }

    public void onCreate(Context context) {
        lib = new LibVLC(context, args);
    }

    public void setData(String url) {
        this.uri = Uri.parse(url);
        if(this.uri.getPath() == null) {
            Toast.makeText(getContext(), getResources().getString(R.string.incorrect_camera_url), Toast.LENGTH_SHORT).show();
            return;
        }
        if(player != null && !player.isReleased()) player.release();
        player = new MediaPlayer(lib);

        IVLCVout vOut = player.getVLCVout();
        vOut.setVideoView(this);
        vOut.attachViews(this);

        vOut.addCallback(new IVLCVout.Callback() {
            @Override
            public void onSurfacesCreated(IVLCVout vlcVout) {
                media = new Media(lib, uri);
                if (args != null) {
                    for (String s : args) {
                        media.addOption(s);
                    }
                }
                media.addOption(":network-caching=150");
                media.addOption(":clock-jitter=0");
                media.addOption(":clock-synchro=0");
                media.setHWDecoderEnabled(true, false);
                if(player != null) {
                    player.setMedia(media);
                }
                Log.d(TAG, "0");
            }

            @Override
            public void onSurfacesDestroyed(IVLCVout vlcVout) {

            }
        });

        ViewGroup.LayoutParams params = getLayoutParams();
        Log.d(TAG, params.width + " : " + params.height);
        DisplayMetrics dMetrics = getContext().getResources().getDisplayMetrics();

        vOut.setWindowSize(params.width, params.height);
        player.setEventListener(new MediaPlayer.EventListener() {
            @Override
            public void onEvent(MediaPlayer.Event event) {
                switch (event.type) {
                    case MediaPlayer.Event.Buffering:
                        Log.d(TAG, "Buffering");
                        break;
                    case MediaPlayer.Event.EncounteredError:
                        Log.d(TAG, "EncounteredError");
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
                        Log.d(TAG, String.valueOf(event.getPositionChanged()));
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
                            callback.onVideError();
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
        Log.d(TAG, "2");
        urlSetted = true;
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateOutputSize(newConfig);
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
        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if(this.height == 0) return;
            params.height = pHeight;
            params.width = params.height * this.width/this.height;
        } else if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if(this.width == 0) return;
            params.width = pWidth;
            params.height = params.width * this.height/this.width;
        }
        if(player == null || player.getVLCVout() == null)
            return;
        player.getVLCVout().setWindowSize(params.width, params.height);
        this.setLayoutParams(params);

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
        if(!urlSetted || this.uri == null) {
            return;
        }
        Log.d(TAG, "3");
        Log.d(TAG, String.valueOf(player.isReleased()));
        if(player != null) {
            if(!player.getVLCVout().areViewsAttached())
                player.getVLCVout().attachViews(this);
            if(player.isReleased()) {
                this.setData(this.uri.getPath());
                player.play();
            }else {
                player.play();
            }
        }

        Media.VideoTrack vtrack = player.getCurrentVideoTrack();
        if (vtrack == null) {
            return;
        }

    }

    public void pause() {
        if(player != null) player.pause();
    }

    public void stop() {
        if(player != null) {
            if(player.isPlaying()) player.stop();
            player.getVLCVout().detachViews();
        }
    }

    public void release() {
        if(player != null) {
            player.release();
            if (media != null)
                media.release();
        }
        lib.release();
        lib = null;
        player = null;
        setKeepScreenOn(false);
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

    int width = 1;
    int height = 1;

    @Override
    public void onNewVideoLayout(IVLCVout vlcVout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
        this.setVisibility(VISIBLE);
        Log.d(TAG, String.valueOf(visibleWidth) + "x" + String.valueOf(visibleHeight));
        DisplayMetrics dMetrics = getContext().getResources().getDisplayMetrics();
        ViewGroup.LayoutParams params = getLayoutParams();
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

        if(this.callback != null)
            callback.onVideoStart();
    }
}
