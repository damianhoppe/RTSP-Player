package pl.huczeq.rtspplayer.ui.start;

import android.content.Context;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import pl.huczeq.rtspplayer.RtspPlayerApp;
import pl.huczeq.rtspplayer.data.repositories.base.CameraRepository;
import pl.huczeq.rtspplayer.domain.usecases.DataMigrationUseCase;
import pl.huczeq.rtspplayer.util.states.CompletableState;

public class DataMigrationViewModel {

    private RtspPlayerApp app;
    private CameraRepository cameraRepository;
    private BehaviorSubject<CompletableState> processingState;
    private DataMigrationUseCase dataMigrationUseCase;

    @Inject
    public DataMigrationViewModel(@ApplicationContext Context app, CameraRepository cameraRepository, DataMigrationUseCase dataMigrationUseCase) {
        this.app = (RtspPlayerApp) app;
        this.cameraRepository = cameraRepository;
        this.processingState = BehaviorSubject.create();
        this.processingState.onNext(CompletableState.Builder.IDLE());
        this.dataMigrationUseCase = dataMigrationUseCase;
    }

    public void startProcessing() {
        if(this.processingState.getValue().isProcessing())
            return;
        this.processingState.onNext(CompletableState.Builder.PROCESSING());
        this.dataMigrationUseCase.execute(null, new DisposableCompletableObserver() {
            @Override
            public void onComplete() {
                processingState.onNext(CompletableState.Builder.SUCCESSFULLY());
                processingState.onComplete();
                app.dataMigrationManager = null;
            }

            @Override
            public void onError(@NonNull Throwable e) {
                processingState.onError(e);
            }
        });
    }

    public BehaviorSubject<CompletableState> getProcessingState() {
        return processingState;
    }
}
