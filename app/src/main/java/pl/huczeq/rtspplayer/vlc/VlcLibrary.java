package pl.huczeq.rtspplayer.vlc;

import android.content.Context;
import android.net.Uri;
import android.view.SurfaceView;
import android.view.TextureView;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.interfaces.IVLCVout;
import org.w3c.dom.Text;

public class VlcLibrary {

    Context context;
    LibVLC libVLC;
    MediaPlayer mediaPlayer;
    IVLCVout vlcVout;

    TextureView outputTextureView;
    SurfaceView outputSurfaceView;

    private static final int OUTPUT_TYPE_TEXTURE_VIEW = 1;
    private static final int OUTPUT_TYPE_SURFACE_VIEW = 2;

    int outputViewType = 0;

    Uri uri;

    boolean initialized;

    public VlcLibrary(Context context) {
        this.context = context;
        this.initialized = false;
    }

    public void init() {
        if(initialized) release();
        this.libVLC = new LibVLC(context);
        this.mediaPlayer = new MediaPlayer(libVLC);
        this.vlcVout = this.mediaPlayer.getVLCVout();
        this.initialized = true;
    }

    public void release() {
        if(!initialized) return;
        if(vlcVout.areViewsAttached()) vlcVout.detachViews();
        mediaPlayer.release();
        libVLC.release();

        mediaPlayer = null;
        libVLC = null;
    }

    public void setOutputView(TextureView view) {
        if(view == null) return;
        this.outputTextureView = view;
        this.outputViewType = OUTPUT_TYPE_TEXTURE_VIEW;
        if(vlcVout.areViewsAttached()) {
            vlcVout.detachViews();
            //TODO init vlcout?
        }
    }

    public void setOutputView(SurfaceView view) {
        if(view == null) return;
        this.outputSurfaceView = view;
        this.outputViewType = OUTPUT_TYPE_SURFACE_VIEW;
        if(vlcVout.areViewsAttached()) {
            vlcVout.detachViews();
            //TODO init vlcout?
        }
    }

    public void play(Uri uri) {
        if(!initialized) init();

        switch(outputViewType) {
            case OUTPUT_TYPE_TEXTURE_VIEW:
                if(outputTextureView == null)
                    return;
                vlcVout.setVideoView(outputTextureView);
                break;
            case OUTPUT_TYPE_SURFACE_VIEW:
                if(outputSurfaceView == null)
                    return;
                vlcVout.setVideoView(outputSurfaceView);
                break;
            default:
                return;
        }
        vlcVout.addCallback(new IVLCVout.Callback() {
            @Override
            public void onSurfacesCreated(IVLCVout vlcVout) {

            }

            @Override
            public void onSurfacesDestroyed(IVLCVout vlcVout) {

            }
        });
    }
}
