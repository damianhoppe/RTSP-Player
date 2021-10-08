package pl.huczeq.rtspplayer.viewmodels;

import androidx.lifecycle.LiveData;

import java.util.List;

import pl.huczeq.rtspplayer.data.DataManager;
import pl.huczeq.rtspplayer.data.objects.Camera;
import pl.huczeq.rtspplayer.data.objects.CameraInstance;
import pl.huczeq.rtspplayer.viewmodels.base.DataManagerViewModel;

public class SelectCameraViewModel extends DataManagerViewModel {

    public SelectCameraViewModel(DataManager dataManager) {
        super(dataManager);
    }

    public LiveData<List<Camera>> getAllCameras() {
        return this.dataManager.getAllCameras();
    }
}
