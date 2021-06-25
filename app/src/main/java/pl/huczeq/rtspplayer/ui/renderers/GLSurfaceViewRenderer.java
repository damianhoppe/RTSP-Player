package pl.huczeq.rtspplayer.ui.renderers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import pl.huczeq.rtspplayer.ui.renderers.base.RendererCallback;
import pl.huczeq.rtspplayer.ui.renderers.base.SurfaceRenderer;
import pl.huczeq.rtspplayer.utils.GLUtils;

import static android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES;

public class GLSurfaceViewRenderer implements GLSurfaceView.Renderer, SurfaceRenderer {


    private static final String TAG = "GLSurfaceViewRenderer";

    public float scale = 1;
    public float videoScaleX = 1;
    public float videoScaleY = 1;
    public float dx = 0, dy = 0;

    private float[] vtmp = { 1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f};
    //private float[] ttmp = { 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f };
    private float[] ttmp = {0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f};
    private FloatBuffer vertices;
    private FloatBuffer verticesTxt;
    private int shaderProgram;
    private int[] txtIds = new int[1];

    private boolean textureIsAvailable = false;
    private boolean textureRendered = false;
    private boolean takePicture = false;
    private int width = 0;
    private int height = 0;
    private int surfaceWidth = 0;
    private int surfaceHeight = 0;
    private final float[] vPMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private final float[] defaultProjectionMatrix = new float[16];
    private float[] scaleMatrix = new float[16];

    private Context context;
    private OnTakeImageCallback takeImageCallback;
    private SurfaceTexture surfaceTexture;
    private RendererCallback callback;


    public static final String vss =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "uniform mat4 uMVPMatrix ;\n" +
                    "attribute vec2 vPosition;\n" +
                    "attribute vec2 vTexCoord;\n" +
                    "varying vec2 texCoord;\n" +
                    "void main() {\n" +
                    "  texCoord = vTexCoord;\n" +
                    "  gl_Position = uMVPMatrix * vec4 ( vPosition.x, vPosition.y, 0, 1.0 );\n" +
                    "}";

    public static final String fss =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "varying vec2 texCoord;\n" +
                    "void main() {\n" +
                    "  gl_FragColor = texture2D(sTexture,texCoord);\n" +
                    "}";

    public GLSurfaceViewRenderer(Context context) {
        this.context = context;

        vertices = ByteBuffer.allocateDirect(8*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertices.put(vtmp);
        vertices.position(0);

        verticesTxt = ByteBuffer.allocateDirect(8*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        verticesTxt.put(ttmp);
        verticesTxt.position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig eglConfig) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        shaderProgram = GLUtils.createShaderProgram(vss, fss);

        gl.glGenTextures(1, txtIds, 0);
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, txtIds[0]);
        GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);


        surfaceTexture = new SurfaceTexture(getTxtId());
        surfaceTexture.setDefaultBufferSize(1080,1920);
        surfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                textureIsAvailable = true;
            }
        });
        if(callback != null)
            callback.onReady(surfaceTexture);

        scaleMatrix[0] = scale;
        scaleMatrix[5] = scale;
        scaleMatrix[9] = 1;
        scaleMatrix[15] = 1;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        float[] localMatrix;
        gl.glClear(gl.GL_COLOR_BUFFER_BIT);
        if (textureIsAvailable) {
            surfaceTexture.updateTexImage();
            textureIsAvailable = false;
            textureRendered = true;
        }

        if(this.takePicture && textureRendered && takeImageCallback != null && width > 0 && height > 0) {

            Matrix.orthoM(defaultProjectionMatrix, 0,-1,1, -1, 1, 3, 7);
            Matrix.setLookAtM(viewMatrix, 0, 0, 0, -3f, 0, 0, 0f, 0f, 1.0f, 0.0f);
            Matrix.setIdentityM(vPMatrix, 0);
            Matrix.multiplyMM(vPMatrix, 0, defaultProjectionMatrix, 0, viewMatrix, 0);
            GLES20.glViewport(0,0,width,height);

            localMatrix = vPMatrix;

            Bitmap bitmap = null;
            int[] frame = new int[1];
            GLES20.glGenFramebuffers(1, frame, 0);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frame[0]);

            int[] texture = new int[1];
            GLES20.glGenTextures(1, texture, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);

            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, texture[0], 0);

            GLES20.glUseProgram(shaderProgram);
            int vPMatrixHandle = GLES20.glGetUniformLocation(shaderProgram, "uMVPMatrix");
            int ph = GLES20.glGetAttribLocation(shaderProgram, "vPosition");
            int tch = GLES20.glGetAttribLocation(shaderProgram, "vTexCoord");
            int th = GLES20.glGetUniformLocation(shaderProgram, "sTexture");

            GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, localMatrix, 0);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, getTxtId());
            GLES20.glUniform1i(th, 0);

            GLES20.glEnableVertexAttribArray(ph);
            GLES20.glEnableVertexAttribArray(tch);

            GLES20.glVertexAttribPointer(ph, 2, GLES20.GL_FLOAT, false, 4 * 2, vertices);
            GLES20.glVertexAttribPointer(tch, 2, GLES20.GL_FLOAT, false, 4 * 2, verticesTxt);

            GLES20.glEnableVertexAttribArray(ph);
            GLES20.glEnableVertexAttribArray(tch);

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

            ByteBuffer buffer = ByteBuffer.allocate(width * height * 4);
            GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(buffer);
            if(bitmap != null) {
                final Bitmap finalBitmap = bitmap;
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        GLSurfaceViewRenderer.this.takeImageCallback.onSaveImg(finalBitmap);
                    }
                });
            }
            GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, 0);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
            this.takePicture = false;

            GLES20.glDeleteFramebuffers(1, frame, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
            GLES20.glDeleteTextures(1, texture, 0);

            GLES20.glViewport(0,0,surfaceWidth,surfaceHeight);
            updateScaleValues();
            Matrix.setLookAtM(viewMatrix, 0, 0, 0, 3f, 0, 0, 0f, 0f, -1.0f, 0.0f);
        }

        scaleMatrix[0] = scale;
        scaleMatrix[5] = scale;
        localMatrix = new float[16];

        Matrix.setIdentityM(vPMatrix, 0);

        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        Matrix.multiplyMM(localMatrix,0,vPMatrix,0,scaleMatrix,0);
        Matrix.translateM(localMatrix, 0, dx, dy, 0);

        GLES20.glUseProgram(shaderProgram);
        int vPMatrixHandle = GLES20.glGetUniformLocation(shaderProgram, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, localMatrix, 0);

        int ph = GLES20.glGetAttribLocation(shaderProgram, "vPosition");
        int tch = GLES20.glGetAttribLocation(shaderProgram, "vTexCoord");
        int th = GLES20.glGetUniformLocation(shaderProgram, "sTexture");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, getTxtId());
        GLES20.glUniform1i(th, 0);

        GLES20.glEnableVertexAttribArray(ph);
        GLES20.glEnableVertexAttribArray(tch);

        GLES20.glVertexAttribPointer(ph, 2, GLES20.GL_FLOAT, false, 4 * 2, vertices);
        GLES20.glVertexAttribPointer(tch, 2, GLES20.GL_FLOAT, false, 4 * 2, verticesTxt);

        GLES20.glEnableVertexAttribArray(ph);
        GLES20.glEnableVertexAttribArray(tch);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, 0);
        gl.glFinish();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(TAG, "onSurfaceChanged: " + width + " x " + height);
        GLES20.glViewport(0, 0, width, height);
        this.surfaceWidth = width;
        this.surfaceHeight = height;
        textureRendered = false;
        Matrix.setLookAtM(viewMatrix, 0, 0, 0, 3f, 0, 0, 0f, 0f, -1.0f, 0.0f);
        updateScaleValues();
    }

    @Override
    public void takePicture() {
        Log.d(TAG, "TAKE PICTURE");
        this.takePicture = true;
    }

    @Override
    public void onConfigurationChanged() {
        this.scale = 1;
        this.dx = 0;
        this.dy = 0;
        updateScaleValues();
    }

    @Override
    public void onNewVideoSize(int width, int height, int videoWidth, int videoHeight) {
        this.width = width;
        this.height = height;
        this.getSurfaceTexture().setDefaultBufferSize(width, height);
        this.updateScaleValues();
    }

    @Override
    public void init(RendererCallback rendererCallback, OnTakeImageCallback takeImageCallback) {
        this.callback = rendererCallback;
        this.takeImageCallback = takeImageCallback;
    }

    @Override
    public void resetTextureRendered() {
        this.textureRendered = false;
    }

    private void updateScaleValues() {
        DisplayMetrics dMetrics = this.context.getResources().getDisplayMetrics();
        int pWidth = surfaceWidth;
        int pHeight = surfaceHeight;
        Log.d(TAG, "updateScaleValues: " + pWidth + " x " + pHeight);

        float mVideoWidth = width;
        float mVideoHeight = height;
        float screenAspectRatio = (float)pHeight/pWidth;
        float videoAspectRatio = mVideoHeight/mVideoWidth;

        if(screenAspectRatio > videoAspectRatio) {
            videoScaleY = (pWidth / mVideoWidth) / (pHeight / mVideoHeight);
            videoScaleX = 1;

            float ratio = videoScaleX/videoScaleY;
            Matrix.orthoM(projectionMatrix, 0,-1,1, -ratio, ratio, 3, 7);
        }else {
            videoScaleX = (pHeight / mVideoHeight) / (pWidth / mVideoWidth);
            videoScaleY = 1;

            float ratio = videoScaleY/videoScaleX;
            Matrix.orthoM(projectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
        }
    }

    public SurfaceTexture getSurfaceTexture() {
        return this.surfaceTexture;
    }

    public int getTxtId() {
        return this.txtIds[0];
    }

    public void recreateSurface() {
        surfaceTexture.release();
        surfaceTexture = new SurfaceTexture(getTxtId());
        surfaceTexture.setDefaultBufferSize(width,height);
        surfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                textureIsAvailable = true;
            }
        });
    }
}
