package pl.huczeq.rtspplayer.ui.cameralist;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.observers.DisposableObserver;
import io.reactivex.rxjava3.observers.DisposableSingleObserver;
import pl.huczeq.rtspplayer.data.model.Camera;
import pl.huczeq.rtspplayer.data.repositories.base.CameraRepository;
import pl.huczeq.rtspplayer.data.repositories.base.CameraThumbnailRepository;
import pl.huczeq.rtspplayer.domain.usecases.DeleteCameraUseCase;
import pl.huczeq.rtspplayer.domain.usecases.LoadCameraUseCase;
import pl.huczeq.rtspplayer.util.Event;

@HiltViewModel
public class CameraListViewModel extends ViewModel {

    private final CameraRepository cameraRepository;
    private final CameraThumbnailRepository thumbnailRepository;

    private final MutableLiveData<LinkedList<String>> thumbnailsUpdatedList;
    private final DisposableObserver<String> thumbnailsUpdateObserver;

    private final DeleteCameraUseCase deleteCameraUseCase;
    private final LoadCameraUseCase loadCameraUseCase;
    private final MutableLiveData<Event<Camera>> appStartCamera = new MutableLiveData<>(null);


    @Inject
    public CameraListViewModel(CameraRepository cameraRepository, CameraThumbnailRepository thumbnailRepository, DeleteCameraUseCase deleteCameraUseCase, LoadCameraUseCase loadCameraUseCase) {
        this.cameraRepository = cameraRepository;
        this.thumbnailRepository = thumbnailRepository;
        this.deleteCameraUseCase = deleteCameraUseCase;
        this.loadCameraUseCase = loadCameraUseCase;

        this.thumbnailsUpdatedList = new MutableLiveData<>(new LinkedList<>());
        this.thumbnailsUpdateObserver = new DisposableObserver<String>() {
            @Override
            public void onNext(String thumbnailName) {
                thumbnailsUpdatedList.getValue().add(thumbnailName);
                thumbnailsUpdatedList.setValue(thumbnailsUpdatedList.getValue());
            }
            @Override
            public void onError(@NonNull Throwable e) {}
            @Override
            public void onComplete() {}
        };
        this.thumbnailRepository.getThumbnailUpdatedSubject()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this.thumbnailsUpdateObserver);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if(this.thumbnailsUpdateObserver != null)
            this.thumbnailsUpdateObserver.dispose();
        this.loadCameraUseCase.dispose();
    }

    public LiveData<List<Camera>> getAllCameras() {
        return this.cameraRepository.fetchAllCameras();
    }

    public MutableLiveData<LinkedList<String>> getThumbnailsUpdatedList() {
        return thumbnailsUpdatedList;
    }

    public void deleteCamera(Camera camera) {
        this.deleteCameraUseCase.execute(camera);
    }

    public LiveData<Event<Camera>> getAppStartCamera() {
        return this.appStartCamera;
    }

    public void loadAppStartCamera(long appStartCameraId) {
        loadCameraUseCase.execute(appStartCameraId, new DisposableSingleObserver<Camera>() {
            @Override
            public void onSuccess(Camera camera) {
                appStartCamera.postValue(new Event<>(camera));
            }

            @Override
            public void onError(@NonNull Throwable e) {}
        });
    }
}
