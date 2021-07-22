package pl.huczeq.rtspplayer.domain.usecases;

import javax.inject.Inject;

import pl.huczeq.rtspplayer.AppExecutors;
import pl.huczeq.rtspplayer.data.model.Camera;
import pl.huczeq.rtspplayer.data.repositories.base.CameraRepository;
import pl.huczeq.rtspplayer.domain.usecases.base.NoResultUseCase;

public class DeleteCameraUseCase extends NoResultUseCase<Camera> {

    private CameraRepository cameraRepository;

    @Inject
    public DeleteCameraUseCase(CameraRepository cameraRepository, AppExecutors executors) {
        super(executors);
        this.cameraRepository = cameraRepository;
    }

    @Override
    protected Runnable buildRunnable(Camera camera) {
        return new Runnable() {
            @Override
            public void run() {
                cameraRepository.deleteCameraGroup(camera.getCameraPattern());
            }
        };
    }
}