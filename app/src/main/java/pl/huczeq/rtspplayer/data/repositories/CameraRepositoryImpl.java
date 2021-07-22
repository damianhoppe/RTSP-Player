package pl.huczeq.rtspplayer.data.repositories;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.subjects.PublishSubject;
import pl.huczeq.rtspplayer.data.model.CameraGroup;
import pl.huczeq.rtspplayer.data.repositories.base.CameraRepository;
import pl.huczeq.rtspplayer.data.sources.local.database.AppDatabase;
import pl.huczeq.rtspplayer.data.sources.local.database.CameraDao;
import pl.huczeq.rtspplayer.data.model.Camera;
import pl.huczeq.rtspplayer.data.model.CameraInstance;
import pl.huczeq.rtspplayer.data.model.CameraPattern;
import pl.huczeq.rtspplayer.data.sources.local.database.CameraInstanceDao;
import pl.huczeq.rtspplayer.data.sources.local.database.CameraPatternDao;

@Singleton
public class CameraRepositoryImpl implements CameraRepository {

    public static final String TAG = "CamerasRepository";

    private final AppDatabase database;
    private final CameraDao cameraDao;
    private final CameraInstanceDao cameraInstanceDao;
    private final CameraPatternDao cameraPatternDao;
    private PublishSubject<CameraInstance> cameraInstanceInvalidatedSubject = PublishSubject.create();

    @Inject
    public CameraRepositoryImpl(AppDatabase database) {
        this.database = database;
        this.cameraDao = this.database.cameraDao();
        this.cameraInstanceDao = this.database.cameraInstanceDao();
        this.cameraPatternDao = this.database.cameraPatternDao();
    }

    @Override
    public LiveData<List<Camera>> getAllCameras() {
        return this.cameraDao.getAllCameras();
    }

    @Override
    public LiveData<Camera> getCameraById(long id) {
        return this.cameraDao.getCameraById(id);
    }

    @Override
    public CameraInstance getCameraInstanceByIdSync(Long cameraInstanceId) {
        return this.cameraInstanceDao.getCameraInstanceByIdSync(cameraInstanceId);
    }

    @Override
    public Camera getCameraByIdSync(long id) {
        return cameraDao.getCameraByIdSync(id);
    }

    @Override
    public LiveData<CameraPattern> getCameraPatternById(long id) {
        return cameraPatternDao.getCameraPatternByIdSync(id);
    }

    @Override
    public List<CameraPattern> getAllCameraPatternsSync() {
        return this.cameraPatternDao.getAllCameraPatternsSync();
    }

    private void insertCameraGroupIntoDatabase(CameraGroup cameraGroup) {
        long id = cameraPatternDao.insertCameraPattern(cameraGroup.getCameraPattern());
        for(CameraInstance cameraInstance : cameraGroup.getCameraInstances())
            cameraInstance.setPatternId(id);
        cameraInstanceDao.insertCameraInstances(cameraGroup.getCameraInstances());
    }

    @Override
    public void insertCameraGroup(CameraGroup cameraGroup) {
        cameraGroup.getCameraPattern().setNumberOfInstances(cameraGroup.getCameraInstances().size());
        this.database.runInTransaction(new Runnable() {
            @Override
            public void run() {
                insertCameraGroupIntoDatabase(cameraGroup);
            }
        });
    }

    @Override
    public void insertCameraGroups(List<CameraGroup> cameraGroups) {
        this.database.runInTransaction(new Runnable() {
            @Override
            public void run() {
                for(CameraGroup cameraGroup : cameraGroups) {
                    cameraGroup.getCameraPattern().setNumberOfInstances(cameraGroup.getCameraInstances().size());
                    insertCameraGroupIntoDatabase(cameraGroup);
                }
            }
        });
    }

    @Override
    public void updateCameraGroup(CameraGroup cameraGroup) {
        this.database.runInTransaction(new Runnable() {
            @Override
            public void run() {
                cameraGroup.getCameraPattern().setNumberOfInstances(cameraGroup.getCameraInstances().size());
                cameraPatternDao.updateCameraPattern(cameraGroup.getCameraPattern());

                List<CameraInstance> oldCameraInstances = cameraInstanceDao.getCameraInstancesByPatternIdSync(cameraGroup.getCameraPattern().getId());
                int i;

                //Remove excess
                while(cameraGroup.getCameraInstances().size() < oldCameraInstances.size()) {
                    i = oldCameraInstances.size() - 1;
                    cameraInstanceInvalidatedSubject.onNext(oldCameraInstances.get(i));
                    cameraInstanceDao.deleteCameraInstancesWithId(oldCameraInstances.get(i).getId());
                    oldCameraInstances.remove(i);
                }

                //Update old and inserts excess
                for(i = 0; i < cameraGroup.getCameraInstances().size(); i++) {
                    CameraInstance newCameraInstance = cameraGroup.getCameraInstances().get(i);
                    newCameraInstance.setPatternId(cameraGroup.getCameraPattern().getId());
                    if(i < oldCameraInstances.size()) {
                        newCameraInstance.setId(oldCameraInstances.get(i).getId());
                        if(Objects.equals(newCameraInstance.getUrl(), oldCameraInstances.get(i).getUrl()))
                            newCameraInstance.setPreviewImg(oldCameraInstances.get(i).getPreviewImg());
                        else
                            cameraInstanceInvalidatedSubject.onNext(oldCameraInstances.get(i));
                        cameraInstanceDao.updateCameraInstance(newCameraInstance);
                    }else {
                        cameraInstanceDao.insertCameraInstance(newCameraInstance);
                    }
                }
            }
        });
    }

    @Override
    public void deleteCameraGroup(CameraPattern cameraPattern) {
        this.database.runInTransaction(new Runnable() {
            @Override
            public void run() {
                List<CameraInstance> instances = cameraInstanceDao.getCameraInstancesByPatternIdSync(cameraPattern.getId());
                for(CameraInstance cameraInstance : instances) {
                    cameraInstanceInvalidatedSubject.onNext(cameraInstance);
                    cameraInstanceDao.deleteCameraInstancesWithId(cameraInstance.getId());
                }
                cameraPatternDao.deleteCameraPattern(cameraPattern.getId());
            }
        });
    }

    @Override
    public LiveData<Integer> getNumberOfCameras() {
        return Transformations.map(this.cameraDao.getAllCameras(), allCameras -> {
                if(allCameras == null)
                    return null;
                return allCameras.size();
            });
    }

    @Override
    public void updateCameraInstanceSync(CameraInstance cameraInstance) {
        this.cameraInstanceDao.updateCameraInstance(cameraInstance);
    }

    @Override
    public PublishSubject<CameraInstance> getCameraInstancesInvalidatedSubject() {
        return this.cameraInstanceInvalidatedSubject;
    }
}