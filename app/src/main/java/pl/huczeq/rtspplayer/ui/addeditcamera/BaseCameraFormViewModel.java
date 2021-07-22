package pl.huczeq.rtspplayer.ui.addeditcamera;

import io.reactivex.rxjava3.annotations.NonNull;

import androidx.annotation.CallSuper;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import io.reactivex.rxjava3.observers.DisposableSingleObserver;
import pl.huczeq.rtspplayer.data.model.CameraGroup;
import pl.huczeq.rtspplayer.data.model.urltemplates.Model;
import pl.huczeq.rtspplayer.data.model.urltemplates.Producer;
import pl.huczeq.rtspplayer.data.repositories.base.UrlTemplateRepository;
import pl.huczeq.rtspplayer.domain.usecases.GenerateCameraGroupUseCase;
import pl.huczeq.rtspplayer.util.states.CompletableState;
import pl.huczeq.rtspplayer.util.states.ResultState;
import pl.huczeq.rtspplayer.util.states.ProcessingStateType;

public abstract class BaseCameraFormViewModel extends ViewModel {

    protected final CameraFormModel cameraFormModel = new CameraFormModel();
    protected final LiveData<List<Producer>> producers;
    protected final MutableLiveData<List<Model>> models = new MutableLiveData<>(null);
    protected final MutableLiveData<CompletableState> savingActionState = new MutableLiveData<>(null);
    private final MutableLiveData<ResultState<CameraGroup>> previewActionState = new MutableLiveData<>(null);

    public final UrlTemplateRepository urlTemplateRepository;
    public final GenerateCameraGroupUseCase generateCameraGroupUseCase;

    public BaseCameraFormViewModel(UrlTemplateRepository urlTemplateRepository, GenerateCameraGroupUseCase generateCameraGroupUseCase) {
        this.urlTemplateRepository = urlTemplateRepository;
        this.generateCameraGroupUseCase = generateCameraGroupUseCase;
        this.producers = this.urlTemplateRepository.getAllProducers();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        this.generateCameraGroupUseCase.dispose();
    }

    @CallSuper
    protected void init() {
    }

    public abstract void save();

    public void selectProducer(Producer producer) {
        if(getCameraFormModel().getProducer().getValue() == producer)
            return;
        if(producer == null || producer.getModelList() == null || producer.getModelList().isEmpty()) {
            this.models.setValue(null);
            this.cameraFormModel.setProducer(null);
            this.cameraFormModel.setModel(null);
            return;
        }
        this.models.setValue(producer.getModelList());
        this.cameraFormModel.setProducer(producer);
        this.cameraFormModel.setModel(producer.getModelList().get(0));
    }

    public void selectModel(@NonNull Model model) {
        this.cameraFormModel.setModel(model);
    }

    public void generateCamerasForPreview() {
        if(!cameraFormModel.isUrlFormValid())
            return;
        if(previewActionState.getValue() != null && previewActionState.getValue().isProcessing())
            return;
        previewActionState.setValue(ResultState.Builder.processingState());
        this.generateCameraGroupUseCase.execute(cameraFormModel.toModel(), new DisposableSingleObserver<CameraGroup>() {
            @Override
            public void onSuccess(@NonNull CameraGroup cameraGroup) {
                previewActionState.setValue(ResultState.Builder.successfully(cameraGroup));
            }

            @Override
            public void onError(@NonNull Throwable e) {
                previewActionState.setValue(ResultState.Builder.failed(e));
            }
        });
    }

    public boolean isSavingInProgress() {
        return savingActionState.getValue() != null &&
                savingActionState.getValue().getType() == ProcessingStateType.PROCESSING;
    }

    public CameraFormModel getCameraFormModel() {
        return cameraFormModel;
    }

    public LiveData<List<Producer>> getProducers() {
        return producers;
    }

    public LiveData<List<Model>> getModels() {
        return this.models;
    }

    public LiveData<CompletableState> getSavingActionState() {
        return savingActionState;
    }

    public LiveData<ResultState<CameraGroup>> getPreviewActionState() {
        return previewActionState;
    }
}
