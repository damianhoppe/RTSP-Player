package pl.huczeq.rtspplayer.domain.usecases;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Completable;
import pl.huczeq.rtspplayer.AppExecutors;
import pl.huczeq.rtspplayer.data.model.CameraGroup;
import pl.huczeq.rtspplayer.data.repositories.base.CameraRepository;
import pl.huczeq.rtspplayer.domain.usecases.base.CompletableUseCase;
import pl.huczeq.rtspplayer.domain.cameragenerator.CameraGroupGenerator;
import pl.huczeq.rtspplayer.domain.model.CameraGroupModel;

public class CreateCameraGroupUseCase extends CompletableUseCase<CameraGroupModel> {

    @Inject
    public CameraRepository cameraRepository;

    @Inject
    public CreateCameraGroupUseCase(AppExecutors executors) {
        super(executors);
    }

    @Override
    protected Completable buildObservable(CameraGroupModel cameraGroupModel) {
        return Completable.fromRunnable(new Runnable() {
            @Override
            public void run() {
                CameraGroupGenerator cameraGroupGenerator = new CameraGroupGenerator();
                CameraGroup cameraGroup = cameraGroupGenerator.generate(cameraGroupModel);
                cameraRepository.insertCameraGroup(cameraGroup);
            }
        });
    }
}
