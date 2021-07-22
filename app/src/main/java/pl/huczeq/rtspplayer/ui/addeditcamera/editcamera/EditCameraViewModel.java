package pl.huczeq.rtspplayer.ui.addeditcamera.editcamera;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import javax.inject.Inject;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
import pl.huczeq.rtspplayer.data.repositories.base.UrlTemplateRepository;
import pl.huczeq.rtspplayer.domain.usecases.GenerateCameraGroupUseCase;
import pl.huczeq.rtspplayer.domain.usecases.LoadCameraToPatternUseCase;
import pl.huczeq.rtspplayer.domain.usecases.UpdateCameraGroupUseCase;
import pl.huczeq.rtspplayer.ui.addeditcamera.CameraFormViewModel;
import pl.huczeq.rtspplayer.util.states.CompletableState;

public class EditCameraViewModel extends CameraFormViewModel {

    @Inject
    public UpdateCameraGroupUseCase updateCameraGroupUseCase;

    @AssistedInject
    public EditCameraViewModel(@Assisted Long cameraId, LoadCameraToPatternUseCase loadCameraToPatternUseCase, UrlTemplateRepository urlTemplateRepository, GenerateCameraGroupUseCase generateCameraGroupUseCase) {
        super(new LoadCameraToPatternUseCase.Params(cameraId, LoadCameraToPatternUseCase.Mode.LOAD_CAMERA_GROUP), loadCameraToPatternUseCase, urlTemplateRepository, generateCameraGroupUseCase);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        this.updateCameraGroupUseCase.dispose();
    }

    @Override
    public void onProcessConfirmed() {
        UpdateCameraGroupUseCase.Params params = UpdateCameraGroupUseCase.Params.builder().cameraPatternId(this.cameraPatternId).model(cameraFormModel.toModel()).build();
        this.updateCameraGroupUseCase.execute(params, new DisposableCompletableObserver() {
            @Override
            public void onComplete() {
                savingActionState.setValue(CompletableState.Builder.SUCCESSFULLY());
            }

            @Override
            public void onError(@NonNull Throwable e) {
                savingActionState.setValue(CompletableState.Builder.FAILED(e));
            }
        });
    }

    @dagger.assisted.AssistedFactory
    public interface AssistedFactory {
        EditCameraViewModel create(Long cameraId);
    }

    public static class Factory implements ViewModelProvider.Factory {

        private EditCameraViewModel.AssistedFactory assistedFactory;
        private Long cameraId;

        public Factory(EditCameraViewModel.AssistedFactory assistedFactory, Long cameraId) {
            this.assistedFactory = assistedFactory;
            this.cameraId = cameraId;
        }

        @SuppressWarnings("unchecked")
        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) this.assistedFactory.create(cameraId);
        }
    }
}
