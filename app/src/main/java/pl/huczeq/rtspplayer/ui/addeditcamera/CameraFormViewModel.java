package pl.huczeq.rtspplayer.ui.addeditcamera;

import androidx.annotation.CallSuper;
import androidx.lifecycle.MutableLiveData;

import com.google.common.base.Optional;

import java.util.List;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.observers.DisposableSingleObserver;
import pl.huczeq.rtspplayer.data.model.CameraPattern;
import pl.huczeq.rtspplayer.data.model.urltemplates.Model;
import pl.huczeq.rtspplayer.data.model.urltemplates.Producer;
import pl.huczeq.rtspplayer.data.repositories.base.UrlTemplateRepository;
import pl.huczeq.rtspplayer.domain.usecases.GenerateCameraGroupUseCase;
import pl.huczeq.rtspplayer.domain.usecases.LoadCameraToPatternUseCase;
import pl.huczeq.rtspplayer.util.states.CompletableState;

public abstract class CameraFormViewModel extends BaseCameraFormViewModel {

    private final MutableLiveData<Boolean> cameraLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Optional<CameraPattern>> cameraPattern = new MutableLiveData<>(null);
    protected final LoadCameraToPatternUseCase.Params params;
    protected long cameraPatternId;

    private LoadCameraToPatternUseCase loadCameraToPatternUseCase;

    public CameraFormViewModel(LoadCameraToPatternUseCase.Params params, LoadCameraToPatternUseCase loadCameraToPatternUseCase,UrlTemplateRepository urlTemplateRepository, GenerateCameraGroupUseCase generateCameraGroupUseCase) {
        super(urlTemplateRepository, generateCameraGroupUseCase);
        this.params = params;
        this.loadCameraToPatternUseCase = loadCameraToPatternUseCase;
        init();

        if(params != null) {
            cameraLoading.setValue(true);
            loadCameraToPatternUseCase.execute(params, new DisposableSingleObserver<CameraPattern>() {
                @Override
                public void onSuccess(@NonNull CameraPattern value) {
                    cameraPattern.setValue(Optional.of(value));
                    cameraLoading.setValue(false);
                    cameraPatternId = value.getId();

                    loadCameraToFormModel(value);
                    updateProducerAndModelInFormModel();
                }

                @Override
                public void onError(@NonNull Throwable e) {
                    e.printStackTrace();
                    cameraPattern.setValue(Optional.absent());
                    cameraLoading.setValue(false);
                }
            });
        }
        this.producers.observeForever(this::onChanged);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        loadCameraToPatternUseCase.dispose();
        this.producers.removeObserver(this::onChanged);
    }


    public void onChanged(List<Producer> producers) {
        if(params != null)
            updateProducerAndModelInFormModel();
    }

    @Override
    @CallSuper
    public final void save() {
        if(isSavingInProgress())
            return;
        if(isLoadingInProgress())
            return;
        if(!cameraFormModel.isFormValid()) {
            this.savingActionState.setValue(CompletableState.Builder.FAILED(new Exception("Form is valid")));
            return;
        }
        this.savingActionState.setValue(CompletableState.Builder.PROCESSING());
        onProcessConfirmed();
    }

    protected abstract void onProcessConfirmed();

    private void loadCameraToFormModel(CameraPattern cameraPattern) {
        this.cameraFormModel.fromModel(cameraPattern);
    }

    private void updateProducerAndModelInFormModel() {
        if(this.cameraPattern.getValue() == null ||
                !this.cameraPattern.getValue().isPresent() ||
                this.producers.getValue() == null)
            return;
        CameraPattern cameraPattern = this.cameraPattern.getValue().get();
        Producer cameraProducer = null;
        Model cameraModel = null;
        if(cameraPattern.getProducer() != null && !cameraPattern.getProducer().isEmpty()) {
            for(Producer producer : this.producers.getValue())
                if(producer.getName().equals(cameraPattern.getProducer())) {
                    cameraProducer = producer;
                    break;
                }
        }
        if(cameraProducer != null && cameraPattern.getModel() != null && !cameraPattern.getModel().isEmpty()) {
            for(Model model : cameraProducer.getModelList())
                if(model.getName().equals(cameraPattern.getModel())) {
                    cameraModel = model;
                    break;
                }
        }

        cameraFormModel.setAdvancedVisible(cameraProducer != null);

        selectProducer(cameraProducer);
        if(cameraModel != null && cameraFormModel.getProducer().getValue() != null)
            selectModel(cameraModel);
    }

    public MutableLiveData<Boolean> getCameraLoading() {
        return cameraLoading;
    }

    public boolean isLoadingInProgress() {
        return cameraLoading != null && Boolean.TRUE.equals(cameraLoading.getValue());
    }
}
