package pl.huczeq.rtspplayer.ui.start;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.observers.DisposableSingleObserver;
import pl.huczeq.rtspplayer.RtspPlayerApp;
import pl.huczeq.rtspplayer.Settings;
import pl.huczeq.rtspplayer.data.model.Camera;
import pl.huczeq.rtspplayer.domain.usecases.LoadCameraUseCase;
import pl.huczeq.rtspplayer.util.states.CompletableState;

@HiltViewModel
public class StartViewModel extends AndroidViewModel {

    private RtspPlayerApp app;
    private Settings settings;
    public LoadCameraUseCase loadCameraUseCase;

    private MutableLiveData<Boolean> appStarted;
    private Camera appStartCamera;
    private final Observer<CompletableState> dataMigrationStateObserver = new Observer<CompletableState>() {
        @Override
        public void onSubscribe(@NonNull Disposable d) {}

        @Override
        public void onNext(@NonNull CompletableState completableState) {}

        @Override
        public void onError(@NonNull Throwable e) {
            loadAppStartCamera();
        }

        @Override
        public void onComplete() {
            loadAppStartCamera();
        }
    };

    @Inject
    public StartViewModel(@ApplicationContext Context context, Settings settings, LoadCameraUseCase loadCameraUseCase) {
        super((Application) context);
        this.app = RtspPlayerApp.get(context);
        this.settings = settings;
        this.loadCameraUseCase = loadCameraUseCase;

        this.appStarted = new MutableLiveData<>(null);

        settings.verifyDefaultSettings();
        startDataMigration();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        this.loadCameraUseCase.dispose();
    }

    private void startDataMigration() {
        DataMigrationViewModel dataMigrationManager = this.app.dataMigrationManager;
        if(dataMigrationManager != null) {
            dataMigrationManager.getProcessingState().subscribe(this.dataMigrationStateObserver);
        }else {
            loadAppStartCamera();
        }
    }

    private void loadAppStartCamera() {
        if(settings.getAppStartCameraId() > 0) {
            loadCameraUseCase.execute(settings.getAppStartCameraId(), new DisposableSingleObserver<Camera>() {
                @Override
                public void onSuccess(@NonNull Camera cameraInstance) {
                    appStartCamera = cameraInstance;
                    appStarted.setValue(true);
                }

                @Override
                public void onError(@NonNull Throwable e) {
                    appStarted.setValue(true);
                    e.printStackTrace();
                }
            });
        }else {
            appStarted.setValue(true);
        }
    }

    public MutableLiveData<Boolean> getAppStarted() {
        return appStarted;
    }

    public Camera getAppStartCamera() {
        return appStartCamera;
    }
}
