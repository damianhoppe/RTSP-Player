package pl.huczeq.rtspplayer.interfaces;

import java.util.List;

import pl.huczeq.rtspplayer.data.objects.CameraInstance;
import pl.huczeq.rtspplayer.data.objects.CameraPattern;

public interface ICameraInstancesService {
    List<CameraInstance> generateCameraInstances(CameraPattern cameraPattern);
    void updateCameraPattern(CameraPattern cameraPattern, List<CameraInstance> currentCameraInstances, List<CameraInstance> newCameraInstances, List<CameraInstance> cameraInstancesToDelete);
}
