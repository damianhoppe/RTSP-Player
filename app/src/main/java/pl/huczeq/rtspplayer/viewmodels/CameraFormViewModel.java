package pl.huczeq.rtspplayer.viewmodels;

import androidx.lifecycle.MutableLiveData;

import pl.huczeq.rtspplayer.data.DataManager;
import pl.huczeq.rtspplayer.data.objects.Camera;
import pl.huczeq.rtspplayer.data.objects.CameraPattern;
import pl.huczeq.rtspplayer.interfaces.IGetCameraCallback;
import pl.huczeq.rtspplayer.data.utils.DataState;
import pl.huczeq.rtspplayer.viewmodels.base.DataManagerViewModel;

public class CameraFormViewModel extends DataManagerViewModel {

    private MutableLiveData<DataState> cameraLoadingState;
    private CameraPattern cameraPattern;

    public CameraFormViewModel(DataManager dataManager, int cameraId) {
        super(dataManager);
        if(cameraId > -1) {
            this.cameraLoadingState = new MutableLiveData<>();
            this.cameraLoadingState.setValue(DataState.LOADING);
            this.dataManager.getCameraById(cameraId, new IGetCameraCallback() {
                @Override
                public void onGetCamera(Camera camera) {
                    if (camera != null)
                        cameraPattern = camera.getCameraPattern();
                    cameraLoadingState.postValue(DataState.LOADED);
                }
            });
        }
    }

    public MutableLiveData<DataState> getCameraLoadingState() {
        return cameraLoadingState;
    }

    public CameraPattern getCameraPattern() {
        if(this.cameraLoadingState != null && this.cameraLoadingState.getValue() == DataState.LOADED)
            this.cameraLoadingState = null;
        return cameraPattern;
    }
}
