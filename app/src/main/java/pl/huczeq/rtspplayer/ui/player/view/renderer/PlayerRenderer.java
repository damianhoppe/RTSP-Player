package pl.huczeq.rtspplayer.ui.player.view.renderer;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import pl.huczeq.rtspplayer.player.OnVideoLayoutChanged;
import pl.huczeq.rtspplayer.player.VideoLayout;

public class PlayerRenderer implements GLSurfaceView.Renderer, OnVideoLayoutChanged {

    private static final String TAG = PlayerRenderer.class.getSimpleName();

    private boolean textureAvailable;
    private int texturesAvailableCounter;
    public int surfaceWidth;
    public int surfaceHeight;
    public int surfaceTextureWidth = 0;
    public int surfaceTextureHeight = 0;
    private long takePictureId;
    private float[] surfaceTextureTransformationMatrix = new float[16];
    private float[] mvpMatrix = new float[16];
    private float[] mvpBitmapMatrix = new float[16];

    private Frame frame;
    private FrameShader frameShader;
    private SurfaceTexture surfaceTexture;

    private PlayerRendererCallback rendererCallback;
    private OnImageCapturedListener onImageCapturedListener;
    private SurfaceTexture.OnFrameAvailableListener onFrameAvailableListener;

    public boolean viewSizeMeasured = false;

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
                textureAvailable = true;
                texturesAvailableCounter++;
                if(onFrameAvailableListener != null)
                    onFrameAvailableListener.onFrameAvailable(surfaceTexture);
            }
        });

        if(this.rendererCallback != null)
            this.rendererCallback.onSurfaceCreated();

        Matrix.setIdentityM(mvpMatrix, 0);
        Matrix.setIdentityM(mvpBitmapMatrix, 0);
        Matrix.rotateM(mvpBitmapMatrix, 0, 180, 1, 0, 0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        boolean textureUpdated = false;
        if(texturesAvailableCounter > 0) {
            texturesAvailableCounter = 0;
            surfaceTexture.updateTexImage();
            surfaceTexture.getTransformMatrix(surfaceTextureTransformationMatrix);
            textureUpdated = true;
        }

        if(textureAvailable && viewSizeMeasured && surfaceTextureHeight*surfaceTextureWidth != 0) {
            if(takePictureId > 0 && onImageCapturedListener != null && textureUpdated) {
                Bitmap bitmap = drawFrameToBitmap();
                long pictureId = takePictureId;
                AndroidSchedulers.mainThread().scheduleDirect(new Runnable() {
                    @Override
                    public void run() {
                        onImageCapturedListener.onImageCaptured(bitmap, pictureId);
                    }
                });
                takePictureId = 0;
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            }
            drawFrame();
        }

        GLES20.glFlush();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        this.surfaceWidth = width;
        this.surfaceHeight = height;
    }

    private void drawFrame() {
        GLES20.glUseProgram(this.frameShader.program);
        this.frameShader.loadMVPMatrix(mvpMatrix);
        this.frameShader.loadSTMatrix(surfaceTextureTransformationMatrix);
        this.frame.draw(this.frameShader);
    }

    private Bitmap drawFrameToBitmap() {
        int oldSurfaceTextureWidth = surfaceTextureWidth;
        int oldSurfaceTextureHeight = surfaceTextureHeight;

        surfaceTextureHeight = 520;
        surfaceTextureWidth = surfaceTextureHeight * oldSurfaceTextureWidth / oldSurfaceTextureHeight;

        GLES20.glViewport(0,0,surfaceTextureWidth,surfaceTextureHeight);

        int[] frame = new int[1];
        GLES20.glGenFramebuffers(1, frame, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frame[0]);

        int[] texture = new int[1];
        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, surfaceTextureWidth, surfaceTextureHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, texture[0], 0);

        GLES20.glUseProgram(this.frameShader.program);
        this.frameShader.loadMVPMatrix(mvpBitmapMatrix);
        this.frameShader.loadSTMatrix(surfaceTextureTransformationMatrix);
        this.frame.draw(this.frameShader);

        ByteBuffer buffer = ByteBuffer.allocateDirect(surfaceTextureWidth * surfaceTextureHeight * 4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        GLES20.glReadPixels(0, 0, surfaceTextureWidth, surfaceTextureHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);
        buffer.rewind();
        Bitmap bitmap = Bitmap.createBitmap(surfaceTextureWidth, surfaceTextureHeight, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        GLES20.glDeleteFramebuffers(1, frame, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glDeleteTextures(1, texture, 0);

        surfaceTextureWidth = oldSurfaceTextureWidth;
        surfaceTextureHeight = oldSurfaceTextureHeight;

        GLES20.glViewport(0,0,surfaceWidth,surfaceHeight);
        return bitmap;
    }


    @Override
    public void onVideoLayoutChanged(VideoLayout videoLayout) {
        this.surfaceTextureWidth = videoLayout.getVisibleWidth();
        this.surfaceTextureHeight = videoLayout.getVisibleHeight();;
        this.frame.setTextureScale((float) videoLayout.getVisibleWidth()/videoLayout.getWidth(), (float) videoLayout.getVisibleHeight()/videoLayout.getHeight());
        if(rendererCallback != null)
            rendererCallback.onVideoLayoutChanged(surfaceTextureWidth, surfaceTextureHeight);
    }

    public void setRendererCallback(PlayerRendererCallback rendererCallback) {
        this.rendererCallback = rendererCallback;
    }

    public void setOnImageCapturedListener(OnImageCapturedListener onImageCapturedListener) {
        this.onImageCapturedListener = onImageCapturedListener;
    }

    public void setOnFrameAvailableListener(SurfaceTexture.OnFrameAvailableListener onFrameAvailableListener) {
        this.onFrameAvailableListener = onFrameAvailableListener;
    }

    public SurfaceTexture getSurfaceTexture() {
        return this.surfaceTexture;
    }

    public void clearSurface() {
        this.textureAvailable = false;
    }

    public void takePicture(long id) {
        this.takePictureId = id;
    }

    public Rect getFrameVisibleRect() {
        Rect rect = new Rect();
        rect.top = 0;
        rect.bottom = surfaceHeight;
        rect.left = 0;
        rect.right = surfaceWidth;

        float scaleX = 1;
        float scaleY = 1;

        float videoAspectRatio = (float)surfaceTextureHeight / surfaceTextureWidth;
        float surfaceAspectRatio = (float)surfaceHeight / surfaceWidth;
        if(videoAspectRatio < surfaceAspectRatio) {
            scaleY = ((float)surfaceWidth / surfaceTextureWidth) / ((float)surfaceHeight / surfaceTextureHeight);
        }else {
            scaleX = ((float)surfaceHeight / surfaceTextureHeight) / ((float)surfaceWidth / surfaceTextureWidth);
        }

        int targetWidth = (int) (scaleX * rect.width());
        int targetHeight = (int) (scaleY * rect.height());
        int leftOffset = (rect.width() - targetWidth) / 2;
        int topOffset = (rect.height() - targetHeight) / 2;
        rect.left = leftOffset;
        rect.right = leftOffset + targetWidth;
        rect.top = topOffset;
        rect.bottom = topOffset + targetHeight;

        return rect;
    }
}
