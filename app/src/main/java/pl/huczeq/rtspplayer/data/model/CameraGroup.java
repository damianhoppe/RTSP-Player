package pl.huczeq.rtspplayer.data.model;

import androidx.room.Embedded;
import androidx.room.Ignore;
import androidx.room.Relation;

import java.util.List;

import lombok.Data;

@Data
public class CameraGroup {

    @Embedded
    private CameraPattern cameraPattern;

    @Relation(
            parentColumn = "id",
            entityColumn = "patternId"
    )
    private List<CameraInstance> cameraInstances;

    public CameraGroup() {}

    @Ignore
    public CameraGroup(CameraPattern cameraPattern){
        this.cameraPattern = cameraPattern;
    }

    @Ignore
    public CameraGroup(CameraPattern cameraPattern, List<CameraInstance> cameraInstances) {
        this.cameraPattern = cameraPattern;
        this.cameraInstances = cameraInstances;
    }

    public CameraPattern getCameraPattern() {
        return cameraPattern;
    }

    public List<CameraInstance> getCameraInstances() {
        return cameraInstances;
    }

    public void setCameraPattern(CameraPattern cameraPattern) {
        this.cameraPattern = cameraPattern;
    }

    public void setCameraInstances(List<CameraInstance> cameraInstances) {
        this.cameraInstances = cameraInstances;
    }
}
