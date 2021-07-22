package pl.huczeq.rtspplayer.data.objects;

import androidx.room.Embedded;
import androidx.room.Relation;

public class Camera {

    @Embedded
    private CameraInstance cameraInstance;

    @Relation(
            parentColumn = "patternId",
            entityColumn = "id"
    )
    private CameraPattern cameraPattern;

    public Camera(CameraInstance cameraInstance, CameraPattern cameraPattern) {
        this.cameraInstance = cameraInstance;
        this.cameraPattern = cameraPattern;
    }

    public CameraInstance getCameraInstance() {
        return cameraInstance;
    }

    public void setCameraInstance(CameraInstance cameraInstance) {
        this.cameraInstance = cameraInstance;
    }

    public CameraPattern getCameraPattern() {
        return cameraPattern;
    }

    public void setCameraPattern(CameraPattern cameraPattern) {
        this.cameraPattern = cameraPattern;
    }
}
