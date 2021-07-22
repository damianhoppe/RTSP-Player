package pl.huczeq.rtspplayer.ui.player.view;

import static android.opengl.EGL14.EGL_CONTEXT_CLIENT_VERSION;
import static android.opengl.EGL14.EGL_TRUE;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import java.util.Objects;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

import pl.huczeq.rtspplayer.ui.player.view.renderer.OnImageCapturedListener;
import pl.huczeq.rtspplayer.ui.player.view.renderer.PlayerRenderer;
import pl.huczeq.rtspplayer.ui.player.view.renderer.PlayerRendererCallback;
import pl.huczeq.rtspplayer.player.OnVideoLayoutChanged;
import pl.huczeq.rtspplayer.player.VideoLayout;

public class PlayerSurfaceView extends GLSurfaceView implements OnVideoLayoutChanged {


    private VideoLayout videoLayout;

    private PlayerRenderer renderer;

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
        this.renderer.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                requestRender();
            }
        });

        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        setBackgroundColor(Color.argb(0x00,0,0,0));
        getHolder().setFormat(PixelFormat.TRANSPARENT);

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(VideoLayout.isEmptyOrNull(videoLayout) || getMeasuredWidth() * getMeasuredHeight() == 0) {
                    renderer.viewSizeMeasured = false;
                    return;
                }
                if(Math.abs(videoLayout.getWidthToHeightRatio() - (float)getMeasuredWidth()/getMeasuredHeight()) <= 0.1f)
                    renderer.viewSizeMeasured = true;
            }
        });
    }

    public void setOnImageCapturedListener(OnImageCapturedListener onImageCapturedListener) {
        this.renderer.setOnImageCapturedListener(onImageCapturedListener);
    }

    @Override
    public void onVideoLayoutChanged(VideoLayout newVideoLayout) {
        if(Objects.equals(this.videoLayout, newVideoLayout))
            return;
        this.videoLayout = newVideoLayout;
        renderer.viewSizeMeasured = false;
        requestLayout();
        renderer.onVideoLayoutChanged(videoLayout);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        renderer.viewSizeMeasured = true;
        if(VideoLayout.isEmptyOrNull(videoLayout)) {
            super.onMeasure(MeasureSpec.makeMeasureSpec(1, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(1, MeasureSpec.EXACTLY));
            return;
        }

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
//        int width = View.MeasureSpec.getSize(widthMeasureSpec);
//        int height = View.MeasureSpec.getSize(heightMeasureSpec);

        float videoAspectRatio = videoLayout.getHeightToWidthRatio();
        float surfaceAspectRatio = (float)height / width;

        float aspectDeformation = videoAspectRatio / surfaceAspectRatio - 1;
        if(aspectDeformation < 0) {
            height = (int) (width * videoAspectRatio);
        }else {
            width = (int) (height / videoAspectRatio);
        }
        super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }

    public void takePicture(long id) {
        this.renderer.takePicture(id);
    }

    public void resetTransformations() {
        invalidate();
    }

    public void clearSurface() {
        renderer.clearSurface();
        requestRender();
    }

    public SurfaceTexture getSurfaceTexture() {
        return this.renderer.getSurfaceTexture();
    }

    public void setRendererCallback(PlayerRendererCallback callback) {
        this.renderer.setRendererCallback(callback);
    }

    public PlayerRenderer getRenderer() {
        return this.renderer;
    }
}
