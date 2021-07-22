package pl.huczeq.rtspplayer.domain.usecases;

import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;
import pl.huczeq.rtspplayer.AppExecutors;
import pl.huczeq.rtspplayer.data.model.Camera;
import pl.huczeq.rtspplayer.data.repositories.base.CameraRepository;
import pl.huczeq.rtspplayer.domain.usecases.base.SingleUseCase;

public class LoadCameraUseCase extends SingleUseCase<Camera, Long> {

    private CameraRepository cameraRepository;

    @Inject
    public LoadCameraUseCase(AppExecutors executors, CameraRepository cameraRepository) {
        super(executors);
        this.cameraRepository = cameraRepository;
    }

    @Override
    protected Single<Camera> buildObservable(Long cameraInstanceId) {
        return Single.fromCallable(new Callable<Camera>() {
            @Override
            public Camera call() throws Exception {
                return cameraRepository.getCameraByIdSync(cameraInstanceId);
            }
        });
    }
}
