package pl.huczeq.rtspplayer.ui.renderers;

import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import pl.huczeq.rtspplayer.ui.views.player.GLTextureView;

import static android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES;

public class SurfaceTextureRenderer implements GLTextureView.Renderer {

    private static final String TAG = "Renderer";

    private int[] txtIds = new int[1];

    private float[] vtmp = { 1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f };
    private float[] ttmp = { 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f };
    private FloatBuffer vertices;
    private FloatBuffer verticesTxt;

    private int shaderProgram;
    private final String vss =
            "#extension GL_OES_EGL_image_external : require\n" +
            "attribute vec2 vPosition;\n" +
                    "attribute vec2 vTexCoord;\n" +
                    "varying vec2 texCoord;\n" +
                    "void main() {\n" +
                    "  texCoord = vTexCoord;\n" +
                    "  gl_Position = vec4 ( vPosition.x, vPosition.y, 0.0, 1.0 );\n" +
                    "}";

    private final String fss =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "varying vec2 texCoord;\n" +
                    "void main() {\n" +
                    "  gl_FragColor = texture2D(sTexture,texCoord);\n" +
                    "}";

    private boolean textureIsAvailable = false;
    private boolean textureRendered = false;

    private SurfaceTexture surfaceTexture;
    public Callback callback;

    public SurfaceTextureRenderer() {
        vertices = ByteBuffer.allocateDirect(8*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertices.put(vtmp);
        vertices.position(0);

        verticesTxt = ByteBuffer.allocateDirect(8*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        verticesTxt.put(ttmp);
        verticesTxt.position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig eglConfig) {
        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
        shaderProgram = createProgram(vss, fss);

        gl.glGenTextures(1, txtIds, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, txtIds[0]);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);


        surfaceTexture = new SurfaceTexture(getTxtId());
        surfaceTexture.setDefaultBufferSize(1920,1080);
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
        gl.glLoadIdentity();
        if (textureIsAvailable) {
            surfaceTexture.updateTexImage();
            textureIsAvailable = false;
            textureRendered = true;
        }

        GLES20.glUseProgram(shaderProgram);
        int ph = GLES20.glGetAttribLocation(shaderProgram, "vPosition");
        int tch = GLES20.glGetAttribLocation(shaderProgram, "vTexCoord");
        int th = GLES20.glGetUniformLocation(shaderProgram, "sTexture");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, getTxtId());
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
        gl.glViewport(0,0,width,height);
        textureRendered = false;
    }

    @Override
    public void onSurfaceDestroyed(GL10 gl) {
        Log.d(TAG, "onSurfaceDestroyed");
        gl.glDeleteTextures(1, txtIds, 0);
    }

    public int getTxtId() {
        return this.txtIds[0];
    }

    public SurfaceTexture getSurfaceTexture() {
        return surfaceTexture;
    }

    public interface Callback {
        void onReady(SurfaceTexture surfaceTexture);
    }

    private static int createProgram( String vss, String fss ) {
        int vshader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vshader, vss);
        GLES20.glCompileShader(vshader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(vshader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e("Shader", "Could not compile vshader");
            Log.v("Shader", "Could not compile vshader:" + GLES20.glGetShaderInfoLog(vshader));
            GLES20.glDeleteShader(vshader);
            vshader = 0;
        }

        Log.d("loadShader", "3");
        int fshader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fshader, fss);
        GLES20.glCompileShader(fshader);
        GLES20.glGetShaderiv(fshader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e("Shader", "Could not compile fshader");
            Log.v("Shader", "Could not compile fshader:" + GLES20.glGetShaderInfoLog(fshader));
            GLES20.glDeleteShader(fshader);
            fshader = 0;
        }

        int program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vshader);
        GLES20.glAttachShader(program, fshader);
        GLES20.glLinkProgram(program);

        return program;
    }

    public boolean isTextureRendered() {
        return this.textureRendered;
    }

}
