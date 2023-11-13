package pl.huczeq.rtspplayer.data.repositories.base;

import androidx.lifecycle.LiveData;

import java.util.List;

import io.reactivex.rxjava3.subjects.PublishSubject;
import pl.huczeq.rtspplayer.data.model.Camera;
import pl.huczeq.rtspplayer.data.model.CameraGroup;
import pl.huczeq.rtspplayer.data.model.CameraInstance;
import pl.huczeq.rtspplayer.data.model.CameraPattern;

public interface CameraRepository {

    LiveData<List<Camera>> fetchAllCameras();
    List<Camera> getAllCameras();
    LiveData<Camera> getCameraById(long id);
    CameraInstance getCameraInstanceByIdSync(Long cameraInstanceId);
    Camera getCameraByIdSync(long id);
    LiveData<CameraPattern> getCameraPatternById(long id);
    List<CameraPattern> getAllCameraPatternsSync();
    void insertCameraGroup(CameraGroup cameraGroup);
    void insertCameraGroups(List<CameraGroup> cameraGroups);
    void updateCameraGroup(CameraGroup cameraGroup);
    void deleteCameraGroup(CameraPattern cameraPattern);
    LiveData<Integer> getNumberOfCameras();
    void updateCameraInstanceSync(CameraInstance cameraInstance);
    PublishSubject<CameraInstance> getCameraInstancesInvalidatedSubject();
    List<CameraGroup> getAllCameraGroups();
    void clearAndInsertCameraGroups(List<CameraGroup> cameraGroups);
    void insertCameraGroupsSync(List<CameraGroup> cameraGroups);
}
