package pl.huczeq.rtspplayer.viewmodels;

import androidx.lifecycle.LiveData;

import java.util.List;

import pl.huczeq.rtspplayer.data.DataManager;
import pl.huczeq.rtspplayer.data.objects.Camera;
import pl.huczeq.rtspplayer.data.objects.CameraPattern;
import pl.huczeq.rtspplayer.viewmodels.base.DataManagerViewModel;

public class CamerasListViewModel extends DataManagerViewModel {

    public CamerasListViewModel(DataManager dataManager) {
        super(dataManager);
    }

    public LiveData<List<Camera>> getAllCameras() {
        return this.dataManager.getAllCameras();
    }

    public void deleteCameras(CameraPattern cameraPattern) {
        this.dataManager.deleteCameraPattern(cameraPattern);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        this.dataManager.finishImageLoadingTask();
    }
}
