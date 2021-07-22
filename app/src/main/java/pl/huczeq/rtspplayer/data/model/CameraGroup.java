package pl.huczeq.rtspplayer.data.model;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

import lombok.Data;

@Data
public class CameraGroup {

    @Embedded
    private CameraPattern cameraPattern;

    @Relation(
            parentColumn = "patternId",
            entityColumn = "id"
    )
    private List<CameraInstance> cameraInstances;

    public CameraGroup() {}

    public CameraGroup(CameraPattern cameraPattern){
        this.cameraPattern = cameraPattern;
    }

    public CameraGroup(CameraPattern cameraPattern, List<CameraInstance> cameraInstances) {
        this.cameraPattern = cameraPattern;
        this.cameraInstances = cameraInstances;
    }
}
