package pl.huczeq.rtspplayer.ui.settings.importbackup;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
import io.reactivex.rxjava3.observers.DisposableSingleObserver;
import pl.huczeq.rtspplayer.domain.backup.LoadedBackup;
import pl.huczeq.rtspplayer.domain.usecases.ExportBackupUseCase;
import pl.huczeq.rtspplayer.domain.usecases.ImportBackupUseCase;
import pl.huczeq.rtspplayer.domain.usecases.LoadBackupUseCase;
import pl.huczeq.rtspplayer.util.states.CompletableState;
import pl.huczeq.rtspplayer.util.states.ResultState;

@HiltViewModel
public class ImportBackupViewModel extends ViewModel {

    private final MutableLiveData<ResultState<LoadedBackup>> loadingBackupState = new MutableLiveData<>(ResultState.Builder.idleState());
    private final MutableLiveData<CompletableState> importingBackupState = new MutableLiveData<>(CompletableState.Builder.IDLE());

    private final MutableLiveData<Boolean> importSettingsChecked = new MutableLiveData<>(true);
    private final MutableLiveData<Boolean> importCamerasChecked = new MutableLiveData<>(true);
    private final MutableLiveData<Boolean> clearCamerasChecked = new MutableLiveData<>(true);
    private final LoadBackupUseCase loadBackupUseCase;
    private final ImportBackupUseCase importBackupUseCase;

    @Inject
    public ImportBackupViewModel(LoadBackupUseCase loadBackupUseCase, ImportBackupUseCase importBackupUseCase) {
        this.loadBackupUseCase = loadBackupUseCase;
        this.importBackupUseCase = importBackupUseCase;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        this.loadBackupUseCase.dispose();
        this.importBackupUseCase.dispose();
    }

    public void loadBackup(InputStream inputStream) {
        if(loadingBackupState.getValue() != null && loadingBackupState.getValue().isProcessing())
            return;
        loadingBackupState.setValue(ResultState.Builder.processingState());
        loadBackupUseCase.execute(inputStream, new DisposableSingleObserver<LoadedBackup>() {
            @Override
            public void onSuccess(@NonNull LoadedBackup backup) {
                loadingBackupState.postValue(ResultState.Builder.successfully(backup));
            }

            @Override
            public void onError(@NonNull Throwable e) {
                loadingBackupState.postValue(ResultState.Builder.failed(e));
            }
        });
    }

    public void restore() {
        if(importingBackupState.getValue() != null && importingBackupState.getValue().isProcessing())
            return;
        LoadedBackup backup = loadingBackupState.getValue().getResult();
        if(backup == null)
            return;
        importingBackupState.setValue(ResultState.Builder.processingState());

        importBackupUseCase.execute(new ImportBackupUseCase.Params(backup, Boolean.TRUE.equals(importCamerasChecked.getValue()), Boolean.TRUE.equals(importSettingsChecked.getValue()), Boolean.TRUE.equals(clearCamerasChecked.getValue())), new DisposableCompletableObserver() {
            @Override
            public void onComplete() {
                importingBackupState.postValue(ResultState.Builder.successfully("Ok"));
            }

            @Override
            public void onError(@NonNull Throwable e) {
                e.printStackTrace();
                importingBackupState.postValue(ResultState.Builder.failed(e));
            }
        });
    }

    public LiveData<ResultState<LoadedBackup>> getLoadingBackupState() {
        return loadingBackupState;
    }

    public LiveData<CompletableState> getImportingBackupState() {
        return importingBackupState;
    }

    public void resetImportingBackupStateToIdle() {
        this.importingBackupState.setValue(CompletableState.Builder.IDLE());
    }

    public MutableLiveData<Boolean> getImportSettingsChecked() {
        return importSettingsChecked;
    }

    public MutableLiveData<Boolean> getImportCamerasChecked() {
        return importCamerasChecked;
    }

    public MutableLiveData<Boolean> getClearCamerasChecked() {
        return clearCamerasChecked;
    }
}
