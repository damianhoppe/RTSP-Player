package pl.huczeq.rtspplayer.domain.usecases;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Completable;
import lombok.Builder;
import lombok.Getter;
import pl.huczeq.rtspplayer.AppExecutors;
import pl.huczeq.rtspplayer.data.model.CameraGroup;
import pl.huczeq.rtspplayer.data.repositories.base.CameraRepository;
import pl.huczeq.rtspplayer.domain.usecases.base.CompletableUseCase;
import pl.huczeq.rtspplayer.domain.cameragenerator.CameraGroupGenerator;
import pl.huczeq.rtspplayer.domain.model.CameraGroupModel;

public class UpdateCameraGroupUseCase extends CompletableUseCase<UpdateCameraGroupUseCase.Params> {

    @Inject
    public CameraRepository cameraRepository;

    @Inject
    public UpdateCameraGroupUseCase(AppExecutors executors) {
        super(executors);
    }

    @Override
    protected Completable buildObservable(UpdateCameraGroupUseCase.Params params) {
        return Completable.fromRunnable(new Runnable() {
            @Override
            public void run() {
                CameraGroupGenerator cameraGroupGenerator = new CameraGroupGenerator();
                CameraGroup cameraGroup = cameraGroupGenerator.generate(params.model);
                cameraGroup.getCameraPattern().setId(params.cameraPatternId);
                cameraRepository.updateCameraGroup(cameraGroup);
            }
        });
    }

    @Builder
    @Getter
    public static class Params {
        private final long cameraPatternId;
        private final CameraGroupModel model;
    }
}
