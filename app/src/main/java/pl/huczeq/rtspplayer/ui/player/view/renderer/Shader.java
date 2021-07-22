package pl.huczeq.rtspplayer.ui.player.view.renderer;

import android.opengl.GLES20;
import android.util.Log;

public class Shader {

    private static final String TAG = "Shader";

    private static int loadShader(int type, String source) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
        if(compileStatus[0] == 0) {
            GLES20.glDeleteShader(shader);
            String errorMessage = "Error: " + GLES20.glGetShaderInfoLog(shader);
            throw new RuntimeException(errorMessage);
        }
        return shader;
    }

    protected int program;
    private int vertexShader;
    private int fragmentShader;

    public Shader(String vertexShaderSource, String fragmentShaderSource) {
        this.program = GLES20.glCreateProgram();
        this.vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderSource);
        this.fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderSource);

        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);
        GLES20.glUseProgram(program);
    }
}
