package pl.huczeq.rtspplayer.viewmodels;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import pl.huczeq.rtspplayer.data.DataManager;
import pl.huczeq.rtspplayer.data.objects.Camera;
import pl.huczeq.rtspplayer.interfaces.IGetCameraCallback;
import pl.huczeq.rtspplayer.viewmodels.base.DataManagerViewModel;

public class CameraPreviewViewModel extends DataManagerViewModel {

    private MutableLiveData<String> previewUrl;
    private Camera camera;
    private String url;

    public CameraPreviewViewModel(DataManager dataManager, int cameraId, String url) {
        super(dataManager);
        this.url = url;
        this.previewUrl = new MutableLiveData<>();
        this.previewUrl.setValue(null);
        this.dataManager.getCameraById(cameraId, new IGetCameraCallback() {
            @Override
            public void onGetCamera(Camera camera) {
                String tempUrl = (camera == null)? url : camera.getCameraInstance().getUrl();
                CameraPreviewViewModel.this.camera = camera;
                if(tempUrl == null)
                    previewUrl.postValue("");
                else
                    previewUrl.postValue(tempUrl);
            }
        });
    }

    public Camera getCamera() {
        return this.camera;
    }

    public MutableLiveData<String> getPreviewUrl() {
        return this.previewUrl;
    }
}
