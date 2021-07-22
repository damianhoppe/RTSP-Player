package pl.huczeq.rtspplayer.ui.renderers;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import pl.huczeq.rtspplayer.ui.renderers.base.RendererCallback;
import pl.huczeq.rtspplayer.ui.renderers.base.SurfaceRenderer;
import pl.huczeq.rtspplayer.ui.views.surfaces.base.GLTextureView;
import pl.huczeq.rtspplayer.common.GLUtils;

import static android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES;

public class GLTextureViewRenderer implements GLTextureView.Renderer, SurfaceRenderer {

    private static final String TAG = "GLTextureViewRenderer";


    /*private float[] vtmp = { 1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f };
    private float[] ttmp = { 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f };*/
    private float[] vtmp = { 1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f};
    private float[] ttmp = {0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f};
    private FloatBuffer vertices;
    private FloatBuffer verticesTxt;
    private int shaderProgram;
    private int[] txtIds = new int[1];

    private boolean takePicture = false;
    private int width = 0;
    private int height = 0;
    private int surfaceWidth = width;
    private int surfaceHeight = height;
    private boolean textureIsAvailable = false;
    private boolean textureRendered = false;

    private OnTakeImageCallback takeImageCallback;
    private SurfaceTexture surfaceTexture;
    private RendererCallback callback;

    private final float[] vPMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] viewMatrix = new float[16];

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

    private final String fss =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "varying vec2 texCoord;\n" +
                    "void main() {\n" +
                    "  gl_FragColor = texture2D(sTexture,texCoord);\n" +
                    "}";


    public GLTextureViewRenderer() {
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
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, txtIds[0]);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);


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
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        gl.glClear(gl.GL_COLOR_BUFFER_BIT);
        if (textureIsAvailable) {
            surfaceTexture.updateTexImage();
            textureIsAvailable = false;
            textureRendered = true;
        }

        if(this.takePicture && textureRendered && takeImageCallback != null) {
            Matrix.setLookAtM(viewMatrix, 0, 0, 0, -3f, 0, 0, 0f, 0f, 1.0f, 0.0f);
            Matrix.setIdentityM(vPMatrix, 0);
            Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
            GLES20.glViewport(0,0,width,height);

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

            GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, vPMatrix, 0);

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
                        GLTextureViewRenderer.this.takeImageCallback.onSaveImg(finalBitmap);
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
            Matrix.setLookAtM(viewMatrix, 0, 0, 0, 3f, 0, 0, 0f, 0f, -1.0f, 0.0f);
        }

        Matrix.setIdentityM(vPMatrix, 0);

        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        GLES20.glUseProgram(shaderProgram);
        int vPMatrixHandle = GLES20.glGetUniformLocation(shaderProgram, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, vPMatrix, 0);

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
        gl.glViewport(0,0,width,height);
        this.surfaceWidth = width;
        this.surfaceHeight = height;
        Matrix.setLookAtM(viewMatrix, 0, 0, 0, 3f, 0, 0, 0f, 0f, -1.0f, 0.0f);
        Matrix.orthoM(projectionMatrix, 0,-1,1, -1, 1, 3, 7);
        textureRendered = false;
    }

    @Override
    public void onSurfaceDestroyed(GL10 gl) {
        gl.glDeleteTextures(1, txtIds, 0);
    }

    @Override
    public void takePicture() {
        this.takePicture = true;
    }

    @Override
    public void onConfigurationChanged() {}

    @Override
    public void onNewVideoSize(int width, int height, int videoWidth, int videoHeight) {
        float scaleX = (float)videoWidth/width;
        float scaleY = (float)videoHeight/height;
        Log.d(TAG, "Correction scale: " + scaleX + " x " + scaleY);
        ttmp[2] = scaleX;
        ttmp[5] = scaleY;
        ttmp[6] = scaleX;
        ttmp[7] = scaleY;
        verticesTxt.put(ttmp);
        verticesTxt.position(0);

        this.width = videoWidth;
        this.height = videoHeight;
        this.getSurfaceTexture().setDefaultBufferSize(width, height);
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

    @Override
    public void recreateSurface() {
        surfaceTexture.release();
        surfaceTexture = new SurfaceTexture(getTxtId());
        surfaceTexture.setDefaultBufferSize(1920,1080);
        surfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                textureIsAvailable = true;
            }
        });
    }

    public int getTxtId() {
        return this.txtIds[0];
    }

    public SurfaceTexture getSurfaceTexture() {
        return surfaceTexture;
    }

}
