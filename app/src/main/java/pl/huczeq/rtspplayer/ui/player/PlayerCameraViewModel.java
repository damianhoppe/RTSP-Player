package pl.huczeq.rtspplayer.ui.player;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Objects;

import javax.inject.Inject;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
import io.reactivex.rxjava3.observers.DisposableSingleObserver;
import lombok.Builder;
import lombok.Getter;
import pl.huczeq.rtspplayer.data.model.CameraInstance;
import pl.huczeq.rtspplayer.data.repositories.base.CameraRepository;
import pl.huczeq.rtspplayer.data.repositories.base.CameraThumbnailRepository;
import pl.huczeq.rtspplayer.domain.usecases.LoadCameraUseCase;
import pl.huczeq.rtspplayer.domain.usecases.SaveCameraThumbnailUseCase;

public class PlayerCameraViewModel extends ViewModel {

    private final MediatorLiveData<Params> streamParams = new MediatorLiveData<>();
    private SaveCameraThumbnailUseCase saveCameraThumbnailUseCase;

    @Inject
    public PlayerCameraViewModel(SaveCameraThumbnailUseCase saveCameraThumbnailUseCase) {
        this.saveCameraThumbnailUseCase = saveCameraThumbnailUseCase;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        this.saveCameraThumbnailUseCase.dispose();
    }

    public void setStreamParams(Params newParams) {
        Objects.requireNonNull(newParams);

        if (this.streamParams.getValue() != null && newParams.equals(this.streamParams.getValue()))
            return;

        streamParams.setValue(newParams);
    }

    public void saveCameraThumbnail(Bitmap bitmap) {
        this.saveCameraThumbnailUseCase.execute(new SaveCameraThumbnailUseCase.Params(this.streamParams.getValue().cameraId, bitmap), new DisposableCompletableObserver() {
            @Override
            public void onComplete() {}

            @Override
            public void onError(@NonNull Throwable e) {
                e.printStackTrace();
            }
        });
    }

    public Params getStreamParams() {
        return this.streamParams.getValue();
    }

    @Getter
    @Builder
    public static class Params {
        private String url;
        private boolean forceTcpEnabled;
        private Long cameraId;

        public boolean equals(Params params) {
            return this.cameraId != null &&
                    Objects.equals(this.cameraId, params.cameraId) &&
                    this.cameraId > 0
                    ||
                    Objects.equals(this.url, params.url) &&
                    this.forceTcpEnabled  == params.forceTcpEnabled;
        }
    }
}
