package pl.huczeq.rtspplayer.ui.player.view.renderer;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import pl.huczeq.rtspplayer.player.VideoInfoChangeListener;
import pl.huczeq.rtspplayer.player.VideoLayout;

public class PlayerRenderer implements GLSurfaceView.Renderer, VideoInfoChangeListener {

    private static final String TAG = PlayerRenderer.class.getSimpleName();

    public boolean isRenderingBlockedUntilSizeMeasured;
    private boolean firstSurfaceReceived = false;
    private final AtomicBoolean surfaceTextureFrameAvailable = new AtomicBoolean(false);
    private final float[] mvpMatrix = new float[16];
    private final float[] videoSurfaceTransformationMatrix = new float[16];
    private int viewportWidth = 0;
    private int viewportHeight = 0;
    private int videoWidth = 0;
    private int videoHeight = 0;

    private int captureFrameIndex;
    private final AtomicBoolean shouldCaptureFrame = new AtomicBoolean();

    private final Frame frame;
    private FrameShader frameShader;
    public SurfaceTexture surfaceTexture;

    private PlayerRendererCallback rendererCallback;
    private InternalOnFrameCaptureListener onFrameCaptureListener;
    private SurfaceTexture.OnFrameAvailableListener onFrameAvailableListener;

    private long renderFramesFromTimestamp = 0;

    public PlayerRenderer() {
        this.frame = new Frame();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig eglConfig) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        this.frame.init();
        this.frameShader = new FrameShader();

        this.surfaceTexture = new SurfaceTexture(this.frame.getTextureId());
        this.surfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                firstSurfaceReceived = true;
                surfaceTextureFrameAvailable.set(true);
                if(onFrameAvailableListener != null)
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            onFrameAvailableListener.onFrameAvailable(surfaceTexture);
                        }
                    });
            }
        });

        Matrix.setIdentityM(mvpMatrix, 0);
        Matrix.setIdentityM(videoSurfaceTransformationMatrix, 0);

        if(this.rendererCallback != null)
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    rendererCallback.onSurfaceCreated(surfaceTexture);
                }
            });
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        if(surfaceTextureFrameAvailable.getAndSet(false)) {
            surfaceTexture.updateTexImage();
            surfaceTexture.getTransformMatrix(videoSurfaceTransformationMatrix);
        }

        if(isRenderingBlockedUntilSizeMeasured ||
                !firstSurfaceReceived ||
                videoWidth*videoHeight==0 ||
                renderFramesFromTimestamp > surfaceTexture.getTimestamp()) {
            GLES20.glFlush();
            return;
        }

        if(firstSurfaceReceived && onFrameCaptureListener != null && shouldCaptureFrame.compareAndSet(true,false)) {
            Bitmap bitmap = drawFrameToBitmap(videoWidth, videoHeight);
            if(bitmap != null) {
                AndroidSchedulers.mainThread().scheduleDirect(new Runnable() {
                    @Override
                    public void run() {
                        onFrameCaptureListener.onFrameCapture(bitmap, captureFrameIndex);
                    }
                });
            }
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        }
        drawFrame();

        GLES20.glFlush();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        this.viewportWidth = width;
        this.viewportHeight = height;
    }

    private void drawFrame() {
        GLES20.glUseProgram(this.frameShader.program);
        this.frameShader.loadMVPMatrix(mvpMatrix);
        this.frameShader.loadSTMatrix(videoSurfaceTransformationMatrix);
        this.frame.draw(this.frameShader);
    }

    private Bitmap drawFrameToBitmap(int frameWidth, int frameHeight) {
        float[] mvpBitmapMatrix = new float[16];
        Matrix.setIdentityM(mvpBitmapMatrix, 0);
        Matrix.rotateM(mvpBitmapMatrix, 0, 180, 1, 0, 0);

        GLES20.glViewport(0,0, frameWidth, frameHeight);

        int[] frame = new int[1];
        GLES20.glGenFramebuffers(1, frame, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frame[0]);

        int[] texture = new int[1];
        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, frameWidth, frameHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, texture[0], 0);

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(this.frameShader.program);
        this.frameShader.loadMVPMatrix(mvpBitmapMatrix);
        this.frameShader.loadSTMatrix(videoSurfaceTransformationMatrix);
        this.frame.draw(this.frameShader);

        ByteBuffer buffer = ByteBuffer.allocateDirect(frameWidth * frameHeight * 4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        GLES20.glReadPixels(0, 0, frameWidth, frameHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);
        buffer.rewind();
        Bitmap bitmap = Bitmap.createBitmap(frameWidth, frameHeight, Bitmap.Config.ARGB_8888);
        int glError = GLES20.glGetError();

        if(glError != GLES20.GL_NO_ERROR) {
            Log.e("GL_CAPTURE", "glReadPixels error: " + glError);
            return null;
        }

        bitmap.copyPixelsFromBuffer(buffer);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        GLES20.glDeleteFramebuffers(1, frame, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glDeleteTextures(1, texture, 0);

        GLES20.glViewport(0,0, viewportWidth, viewportHeight);

        return bitmap;
    }

    @Override
    public void onVideoLayoutChanged(VideoLayout videoLayout) {
        this.frame.setTextureScale((float) videoLayout.getVisibleWidth()/videoLayout.getWidth(), (float) videoLayout.getVisibleHeight()/videoLayout.getHeight());
        videoWidth = videoLayout.getVisibleWidth();
        videoHeight = videoLayout.getVisibleHeight();
    }

    public void setRendererCallback(PlayerRendererCallback rendererCallback) {
        this.rendererCallback = rendererCallback;
    }

    public void setOnFrameCaptureListener(InternalOnFrameCaptureListener onFrameCaptureListener) {
        this.onFrameCaptureListener = onFrameCaptureListener;
    }

    public SurfaceTexture getSurfaceTexture() {
        return this.surfaceTexture;
    }

    public void clearSurface() {
        this.firstSurfaceReceived = false;
        renderFramesFromTimestamp = Long.MAX_VALUE;
    }
    public void drawOnlyNewFrames() {
        renderFramesFromTimestamp = System.nanoTime();
    }

    public void captureFrame(int captureFrameIndex) {
        this.captureFrameIndex = captureFrameIndex;
        this.shouldCaptureFrame.set(true);
    }

    public void setOnFrameAvailableListener(SurfaceTexture.OnFrameAvailableListener onFrameAvailableListener) {
        this.onFrameAvailableListener = onFrameAvailableListener;
    }

    public void cancelFrameCapture() {
        shouldCaptureFrame.set(false);
    }

    public interface InternalOnFrameCaptureListener {
        void onFrameCapture(Bitmap bitmap, int captureIndex);
    }
}
