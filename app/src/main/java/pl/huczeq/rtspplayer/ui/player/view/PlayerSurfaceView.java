package pl.huczeq.rtspplayer.ui.player.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import androidx.collection.ArraySet;

import java.util.Objects;
import java.util.Set;

import pl.huczeq.rtspplayer.ui.player.view.renderer.OnFrameCaptureListener;
import pl.huczeq.rtspplayer.ui.player.view.renderer.PlayerRenderer;
import pl.huczeq.rtspplayer.ui.player.view.renderer.PlayerRendererCallback;
import pl.huczeq.rtspplayer.player.VideoInfoChangeListener;
import pl.huczeq.rtspplayer.player.VideoLayout;

public class PlayerSurfaceView extends GLSurfaceView implements VideoInfoChangeListener {


    private VideoLayout videoLayout;
    private PlayerRenderer renderer;
    private PlayerView.OnVideoBoundsChangeListener onVideoBoundsChangeListener;
    private OnFirstFrameRenderedListener onFirstFrameRenderedListener;

    private Rect videoBounds;

    public PlayerSurfaceView(Context context) {
        super(context);
        init();
    }

    public PlayerSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8,8,8,0,0,0);
//        setSecure(true);
//        setEGLContextFactory(new EGLContextFactory() {
//            @Override
//            public EGLContext createContext(EGL10 egl10, EGLDisplay eglDisplay, EGLConfig eglConfig) {
//                int[] attrib_list = {EGL_CONTEXT_CLIENT_VERSION, 2, 0x32C0, EGL_TRUE,
//                        EGL10.EGL_NONE};
//                return egl10.eglCreateContext(eglDisplay, eglConfig, EGL10.EGL_NO_CONTEXT, attrib_list);
//            }
//
//            @Override
//            public void destroyContext(EGL10 egl10, EGLDisplay eglDisplay, EGLContext eglContext) {
//                egl10.eglDestroyContext(eglDisplay, eglContext);
//            }
//        });
//
//        setEGLWindowSurfaceFactory(new EGLWindowSurfaceFactory() {
//            @Override
//            public EGLSurface createWindowSurface(EGL10 egl10, EGLDisplay eglDisplay, EGLConfig eglConfig, Object o) {
//                int[] surface_attribs = {
//                        0x32C0, EGL_TRUE,
//                        EGL10.EGL_NONE
//                };
//                return egl10.eglCreateWindowSurface(eglDisplay, eglConfig, o, surface_attribs);
//            }
//
//            @Override
//            public void destroySurface(EGL10 egl10, EGLDisplay eglDisplay, EGLSurface eglSurface) {
//                egl10.eglDestroySurface(eglDisplay, eglSurface);
//            }
//        });
        this.renderer = new PlayerRenderer();
        this.renderer.setStatesListener(new PlayerRenderer.StatesListener() {

            @Override
            public void onFrameAvailable() {
                requestRender();
            }

            @Override
            public void onFirstFrameRendered() {
                if(onFirstFrameRenderedListener != null)
                    onFirstFrameRenderedListener.onFirstFrameRendered();
            }
        });
        this.renderer.setOnFrameCaptureListener(new PlayerRenderer.InternalOnFrameCaptureListener() {
            @Override
            public void onFrameCapture(Bitmap bitmap, int capturedFrameIndex) {
                if(captureFrameCancelled)
                    return;
                if(captureFrameIndex != capturedFrameIndex)
                    return;
                for(OnFrameCaptureListener listener : onFrameCaptureListeners) {
                    listener.onFrameCapture(bitmap);
                }
            }
        });

        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        setBackgroundColor(Color.argb(0x00,0,0,0));
        getHolder().setFormat(PixelFormat.OPAQUE);
    }

    @Override
    public void onVideoLayoutChanged(VideoLayout newVideoLayout) {
        if(Objects.equals(this.videoLayout, newVideoLayout))
            return;
        this.videoLayout = newVideoLayout;
        queueEvent(() -> this.renderer.onVideoLayoutChanged(videoLayout));
        if(newVideoLayout == null || newVideoLayout.getVisibleWidth() == 0 || newVideoLayout.getVisibleHeight() == 0) {
            return;
        }
        getHolder().setFixedSize(newVideoLayout.getVisibleWidth(), newVideoLayout.getVisibleHeight());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        requestRender();
        Rect videoBounds = new Rect();
        this.getGlobalVisibleRect(videoBounds);
        if(Objects.equals(this.videoBounds, videoBounds)) {
            return;
        }
        this.videoBounds = videoBounds;
        onVideoBoundsChangeListener.onVideoBoundsChanged(videoBounds);
    }

    private int captureFrameIndex = 0;
    private boolean captureFrameCancelled = false;
    private final Set<OnFrameCaptureListener> onFrameCaptureListeners = new ArraySet<>();

    public void cancelFrameCapture() {
        captureFrameCancelled = true;
        queueEvent(new Runnable() {
            @Override
            public void run() {
                renderer.cancelFrameCapture();
            }
        });
    }

    public void captureFrame() {
        captureFrameCancelled = false;
        captureFrameIndex++;
        queueEvent(new Runnable() {
            @Override
            public void run() {
                renderer.captureFrame(captureFrameIndex);
            }
        });
        requestRender();
    }

    public void registerOnFrameCaptureListener(OnFrameCaptureListener onFrameCaptureListener) {
        this.onFrameCaptureListeners.add(onFrameCaptureListener);
    }

    public void clearSurface() {
        renderer.clearSurface();
        cancelFrameCapture();
        requestRender();
    }

    public SurfaceTexture getSurfaceTexture() {
        return this.renderer.getSurfaceTexture();
    }

    public void setOnSurfaceCreatedListener(PlayerRendererCallback callback) {
        this.renderer.setRendererCallback(callback);
    }

    public void setOnVideoBoundsChanged(PlayerView.OnVideoBoundsChangeListener onVideoBoundsChangeListener) {
        this.onVideoBoundsChangeListener = onVideoBoundsChangeListener;
    }

    public void drawOnlyNewFrames() {
        this.renderer.drawOnlyNewFrames();
    }

    public void setOnFirstFrameRenderedListener(OnFirstFrameRenderedListener onFirstFrameRenderedListener) {
        this.onFirstFrameRenderedListener = onFirstFrameRenderedListener;
    }

    public interface OnFirstFrameRenderedListener {
        void onFirstFrameRendered();
    }
}
