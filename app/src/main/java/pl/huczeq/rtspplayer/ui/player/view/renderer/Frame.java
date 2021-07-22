package pl.huczeq.rtspplayer.ui.player.view.renderer;

import static android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Frame {

    private static FloatBuffer loadIntoBuffer(float[] data) {
        FloatBuffer buffer = ByteBuffer.allocateDirect(data.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(data);
        buffer.position(0);
        return buffer;
    }

    private FloatBuffer verticeCoords;
    private FloatBuffer textureCoords;
    private int vertices;
    private int[] textureId = new int[1];
    private float[] textureScale = new float[]{1,1};

    public void init() {
        float[] vertexCoordsArray = {
                -1f, -1f,
                -1f, 1f,
                1, -1f,
                1f, 1f
        };
        float[] textureCoordsArray = {
                0f, 0f,
                0f, 1f,
                1f, 0f,
                1f, 1f
        };
        this.vertices = 4;

        this.verticeCoords = loadIntoBuffer(vertexCoordsArray);
        this.textureCoords = loadIntoBuffer(textureCoordsArray);

        GLES20.glGenTextures(1, textureId, 0);
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureId[0]);
        GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, 0);
    }

    public void draw(FrameShader shader) {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, this.textureId[0]);
        shader.loadTexture(0);
        shader.loadTextureScale(this.textureScale);

        GLES20.glEnableVertexAttribArray(shader.getVertexPositionAttrib());
        GLES20.glEnableVertexAttribArray(shader.getTextureCoordsAttrib());

        GLES20.glVertexAttribPointer(shader.getVertexPositionAttrib(), 2, GLES20.GL_FLOAT, false, this.vertices * 2, verticeCoords);
        GLES20.glVertexAttribPointer(shader.getTextureCoordsAttrib(), 2, GLES20.GL_FLOAT, false, this.vertices * 2, textureCoords);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, 0);
    }

    public void setTextureScale(float x, float y) {
        this.textureScale[0] = x;
        this.textureScale[1] = y;
    }

    public int getTextureId() {
        return this.textureId[0];
    }
}
