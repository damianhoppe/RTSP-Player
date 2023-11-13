package pl.huczeq.rtspplayer.ui.settings.exportbackup;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.IOException;
import java.io.OutputStream;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
import pl.huczeq.rtspplayer.Settings;
import pl.huczeq.rtspplayer.data.repositories.base.CameraRepository;
import pl.huczeq.rtspplayer.domain.usecases.ExportBackupUseCase;

@HiltViewModel
public class ExportBackupViewModel extends ViewModel {

    private final ExportBackupUseCase exportBackupUseCase;

    private final MutableLiveData<Boolean> exportSettingsChecked = new MutableLiveData<>(true);
    private final MutableLiveData<Boolean> exportCamerasChecked = new MutableLiveData<>(true);

    private final MutableLiveData<Boolean> exportingInProgress = new MutableLiveData<>(false);
    private Boolean isExportCompletedSuccessfully = null;

    @Inject
    public ExportBackupViewModel(ExportBackupUseCase exportBackupUseCase) {
        this.exportBackupUseCase = exportBackupUseCase;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        this.exportBackupUseCase.dispose();
    }

    public void exportDataTo(OutputStream outputStream) {
        if(Boolean.TRUE.equals(exportingInProgress.getValue()))
            return;
        exportingInProgress.setValue(true);

        this.exportBackupUseCase.execute(
                new ExportBackupUseCase.Params(
                        outputStream,
                        Boolean.TRUE.equals(exportCamerasChecked.getValue()),
                        Boolean.TRUE.equals(exportSettingsChecked.getValue()))
                , new DisposableCompletableObserver() {

            @Override
            public void onComplete() {
                isExportCompletedSuccessfully = true;
                exportingInProgress.postValue(false);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                e.printStackTrace();
                isExportCompletedSuccessfully = false;
                exportingInProgress.postValue(false);
            }
        });
    }

    public LiveData<Boolean> getExportingInProgress() {
        return exportingInProgress;
    }

    public MutableLiveData<Boolean> getExportSettingsChecked() {
        return exportSettingsChecked;
    }

    public MutableLiveData<Boolean> getExportCamerasChecked() {
        return exportCamerasChecked;
    }

    public Boolean isExportCompletedSuccessfully() {
        return isExportCompletedSuccessfully;
    }

    public void resetExportCompletedSuccessfully() {
        this.isExportCompletedSuccessfully = null;
    }
}
