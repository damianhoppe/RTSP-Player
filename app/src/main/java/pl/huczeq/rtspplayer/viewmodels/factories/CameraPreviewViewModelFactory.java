package pl.huczeq.rtspplayer.viewmodels.factories;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import org.jetbrains.annotations.NotNull;

import pl.huczeq.rtspplayer.data.DataManager;
import pl.huczeq.rtspplayer.viewmodels.CameraPreviewViewModel;
import pl.huczeq.rtspplayer.viewmodels.CreateBackupViewModel;

public class CameraPreviewViewModelFactory extends DataManagerViewModelFactory {

    protected int cameraId;
    protected String url;

    public CameraPreviewViewModelFactory(DataManager dataManager, int cameraId, String url) {
        super(dataManager);
        this.cameraId = cameraId;
        this.url = url;
    }

    @NonNull
    @NotNull
    @Override
    public <T extends ViewModel> T create(@NonNull @NotNull Class<T> modelClass) {
        if(modelClass.isAssignableFrom(CameraPreviewViewModel.class)) {
            return (T) new CameraPreviewViewModel(this.dataManager, this.cameraId, this.url);
        }
        throw new IllegalStateException("Unknown ViewModel class: " + modelClass.getName());
    }
}
