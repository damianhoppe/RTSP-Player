package pl.huczeq.rtspplayer.ui.addeditcamera.addcamera;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import javax.inject.Inject;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
import pl.huczeq.rtspplayer.data.repositories.base.UrlTemplateRepository;
import pl.huczeq.rtspplayer.domain.usecases.CreateCameraGroupUseCase;
import pl.huczeq.rtspplayer.domain.usecases.GenerateCameraGroupUseCase;
import pl.huczeq.rtspplayer.domain.usecases.LoadCameraToPatternUseCase;
import pl.huczeq.rtspplayer.ui.addeditcamera.CameraFormViewModel;
import pl.huczeq.rtspplayer.util.states.CompletableState;

public class AddCameraViewModel extends CameraFormViewModel {

    @Inject
    public CreateCameraGroupUseCase createCameraGroupUseCase;

    @AssistedInject
    public AddCameraViewModel(@Assisted LoadCameraToPatternUseCase.Params params, LoadCameraToPatternUseCase loadCameraToPatternUseCase, UrlTemplateRepository urlTemplateRepository, GenerateCameraGroupUseCase generateCameraGroupUseCase) {
        super(params, loadCameraToPatternUseCase, urlTemplateRepository, generateCameraGroupUseCase);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        this.createCameraGroupUseCase.dispose();
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void onProcessConfirmed() {
        this.createCameraGroupUseCase.execute(this.cameraFormModel.toModel(), new DisposableCompletableObserver() {
            @Override
            public void onComplete() {
                savingActionState.setValue(CompletableState.Builder.SUCCESSFULLY());
            }

            @Override
            public void onError(@NonNull Throwable e) {
                e.printStackTrace();
                savingActionState.setValue(CompletableState.Builder.FAILED(e));
            }
        });
    }

    @dagger.assisted.AssistedFactory
    public interface AssistedFactory {
        AddCameraViewModel create(LoadCameraToPatternUseCase.Params params);
    }

    public static class Factory implements ViewModelProvider.Factory {

        private AddCameraViewModel.AssistedFactory assistedFactory;
        private LoadCameraToPatternUseCase.Params params;

        public Factory(AddCameraViewModel.AssistedFactory assistedFactory, LoadCameraToPatternUseCase.Params params) {
            this.assistedFactory = assistedFactory;
            this.params = params;
        }

        @SuppressWarnings("unchecked")
        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) this.assistedFactory.create(params);
        }
    }
}
