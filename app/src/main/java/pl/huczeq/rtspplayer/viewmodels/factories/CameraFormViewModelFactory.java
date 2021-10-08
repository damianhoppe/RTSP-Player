package pl.huczeq.rtspplayer.viewmodels.factories;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import org.jetbrains.annotations.NotNull;

import pl.huczeq.rtspplayer.data.DataManager;
import pl.huczeq.rtspplayer.viewmodels.CameraFormViewModel;
import pl.huczeq.rtspplayer.viewmodels.CameraPreviewViewModel;

public class CameraFormViewModelFactory extends DataManagerViewModelFactory {

    protected int cameraId;
    protected boolean loadOnlyInstanceData;

    public CameraFormViewModelFactory(DataManager dataManager, int cameraId, boolean loadOnlyInstanceData) {
        super(dataManager);
        this.cameraId = cameraId;
        this.loadOnlyInstanceData = loadOnlyInstanceData;
    }

    @NonNull
    @NotNull
    @Override
    public <T extends ViewModel> T create(@NonNull @NotNull Class<T> modelClass) {
        if(modelClass.isAssignableFrom(CameraFormViewModel.class)) {
            return (T) new CameraFormViewModel(this.dataManager, this.cameraId, loadOnlyInstanceData);
        }
        throw new IllegalStateException("Unknown ViewModel class: " + modelClass.getName());
    }
}
