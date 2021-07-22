package pl.huczeq.rtspplayer.common;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;

import static android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES;

public class GLUtils {

    private static String TAG = "GLUtils";

    public static int createShaderProgram( String vss, String fss ) {
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

    public static Bitmap saveTexture(int texture, int width, int height) {
        checkGLError(TAG + ":saveTexture() - init");
        int[] frame = new int[1];
        GLES20.glGenFramebuffers(1, frame, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frame[0]);

        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GL_TEXTURE_EXTERNAL_OES, texture, 0);
        if(checkGLError(TAG + ":saveTexture()")) {
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, texture, 0);
            if(checkGLError(TAG + ":saveTexture()")) {
                return null;
            }
        }

        ByteBuffer buffer = ByteBuffer.allocate(width * height * 4);
        GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glDeleteFramebuffers(1, frame, 0);

        return bitmap;
    }

    public static boolean checkGLError(String TAG) {
        int error;
        boolean errorCccurred = false;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            errorCccurred = true;
            Log.e(TAG, "glError: " + error);
        }
        return errorCccurred;
    }
}
