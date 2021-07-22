package pl.huczeq.rtspplayer.ui.player.view.renderer;

import android.opengl.GLES20;

public class FrameShader extends Shader {


    public static final String vertexShader =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "uniform mat4 mvpMatrix;\n" +
                    "uniform mat4 stMatrix;\n" +
                    "attribute  vec2 vertexPosition;\n" +
                    "attribute vec2 textureCoords;\n" +
                    "varying vec2 finalTextureCoords;\n" +
                    "void main() {\n" +
                    "  finalTextureCoords = (stMatrix * vec4(textureCoords,1.0,1.0)).xy;\n" +
                    "  gl_Position = mvpMatrix * vec4(vertexPosition.x, vertexPosition.y, 0, 1.0);\n" +
                    "}";

    public static final String fragmentShader =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "uniform samplerExternalOES texture;\n" +
                    "uniform vec2 textureScale;\n" +
                    "varying vec2 finalTextureCoords;\n" +
                    "void main() {\n" +
                    "  gl_FragColor = texture2D(texture, finalTextureCoords * textureScale);\n" +
                    "}";

    private int mvpMatrixUniform;
    private int stMatrixUniform;
    private int textureUniform;
    private int textureScaleUniform;
    private int vertexPositionAttrib;
    private int textureCoordsAttrib;

    public FrameShader() {
        super(vertexShader, fragmentShader);

        this.mvpMatrixUniform = GLES20.glGetUniformLocation(program, "mvpMatrix");
        this.stMatrixUniform = GLES20.glGetUniformLocation(program, "stMatrix");
        this.textureUniform = GLES20.glGetUniformLocation(program, "texture");
        this.textureScaleUniform = GLES20.glGetUniformLocation(program, "textureScale");
        this.vertexPositionAttrib = GLES20.glGetAttribLocation(program, "vertexPosition");
        this.textureCoordsAttrib = GLES20.glGetAttribLocation(program, "textureCoords");
    }

    public void loadMVPMatrix(float[] matrix) {
        GLES20.glUniformMatrix4fv(this.mvpMatrixUniform, 1, false, matrix, 0);
    }

    public void loadSTMatrix(float[] matrix) {
        GLES20.glUniformMatrix4fv(this.stMatrixUniform, 1, false, matrix, 0);
    }
    public void loadTexture(int i) {
        GLES20.glUniform1i(this.textureUniform, i);
    }

    public void loadTextureScale(float[] scaleFactor) {
        GLES20.glUniform2fv(this.textureScaleUniform, 1, scaleFactor, 0);
    }

    public int getVertexPositionAttrib() {
        return vertexPositionAttrib;
    }

    public int getTextureCoordsAttrib() {
        return textureCoordsAttrib;
    }
}
