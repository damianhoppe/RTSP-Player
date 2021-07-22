package pl.huczeq.rtspplayer.domain.usecases;

import android.util.Log;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;
import io.reactivex.rxjava3.core.SingleOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Cancellable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.huczeq.rtspplayer.AppExecutors;
import pl.huczeq.rtspplayer.data.model.CameraGroup;
import pl.huczeq.rtspplayer.domain.usecases.base.SingleUseCase;
import pl.huczeq.rtspplayer.domain.cameragenerator.CameraGroupGenerator;
import pl.huczeq.rtspplayer.domain.model.CameraGroupModel;

public class GenerateCameraGroupUseCase extends SingleUseCase<CameraGroup, CameraGroupModel> {

    @Inject
    public GenerateCameraGroupUseCase(AppExecutors executors) {
        super(executors);
    }

    @Override
    public AppExecutors.AppExecutor executor() {
        return new AppExecutors.AppExecutor(Executors.newSingleThreadExecutor());
    }

    @Override
    protected Single<CameraGroup> buildObservable(CameraGroupModel cameraGroupModel) {
        return Single.create(new SingleOnSubscribe<CameraGroup>() {
            @Override
            public void subscribe(@NonNull SingleEmitter<CameraGroup> emitter) throws Throwable {
                Thread workerThread = Thread.currentThread();
                emitter.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() throws Throwable {
                        workerThread.interrupt();
                    }
                });
                CameraGroupGenerator generator = new CameraGroupGenerator();
                try {
                    emitter.onSuccess(generator.generate(cameraGroupModel));
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }
}
