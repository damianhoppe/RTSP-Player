package pl.huczeq.rtspplayer.data.model;

import androidx.room.Embedded;
import androidx.room.Relation;

import lombok.Data;
import lombok.Value;

@Data
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

    public CameraPattern getCameraPattern() {
        return cameraPattern;
    }
}
