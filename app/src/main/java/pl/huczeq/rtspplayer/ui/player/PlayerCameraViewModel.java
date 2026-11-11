package pl.huczeq.rtspplayer.ui.player;

import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.core.util.Consumer;

import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
import io.reactivex.rxjava3.observers.DisposableSingleObserver;
import lombok.Builder;
import lombok.Getter;
import pl.huczeq.rtspplayer.AppExecutors;
import pl.huczeq.rtspplayer.Settings;
import pl.huczeq.rtspplayer.data.model.Camera;
import pl.huczeq.rtspplayer.domain.usecases.LoadAdjacentCamerasUseCase;
import pl.huczeq.rtspplayer.domain.usecases.SaveCameraThumbnailUseCase;

public class PlayerCameraViewModel extends ViewModel {

    private final MediatorLiveData<Params> streamParams = new MediatorLiveData<>();
    private final SaveCameraThumbnailUseCase saveCameraThumbnailUseCase;
    private final LoadAdjacentCamerasUseCase loadAdjacentCamerasUseCase;
    private final AppExecutors executors;
    private final Settings settings;
    private final MutableLiveData<Pair<Camera, Camera>> adjacentCameras = new MutableLiveData<>();
    private final MutableLiveData<Integer> reconnectTimer = new MutableLiveData<>(null);
    private boolean timerRunning = false;
    private long reconnectDeadlineSeconds;
    private final Runnable timerRunnable;

    @Inject
    public PlayerCameraViewModel(SaveCameraThumbnailUseCase saveCameraThumbnailUseCase, LoadAdjacentCamerasUseCase loadAdjacentCamerasUseCase, AppExecutors executors, Settings settings) {
        this.saveCameraThumbnailUseCase = saveCameraThumbnailUseCase;
        this.loadAdjacentCamerasUseCase = loadAdjacentCamerasUseCase;
        this.executors = executors;
        this.settings = settings;
        this.timerRunnable = new Runnable() {
            @Override
            public void run() {
                if(!timerRunning)
                    return;

                if(!settings.isReconnectTimerEnabled()) {
                    timerRunning = false;
                    reconnectTimer.setValue(null);
                    return;
                }

                long remainingMillis = reconnectDeadlineSeconds - SystemClock.elapsedRealtime();

                int remainingSeconds = (int) Math.ceil(remainingMillis / 1000.0);

                reconnectTimer.setValue(remainingSeconds);

                if(remainingSeconds == 0) {
                    timerRunning = false;
                    return;
                }

                executors.mainThread().executeDelayed(this, 1, TimeUnit.SECONDS);
            }
        };
    }

    private Pair<Camera, Camera> awaitAdjacentCamera() {
        Semaphore semaphore = new Semaphore(0);
        AtomicReference<Pair<Camera, Camera>> result = new AtomicReference<>();

        Observer<Pair<Camera, Camera>> observer = new Observer<>() {
            @Override
            public void onChanged(Pair<Camera, Camera> value) {
                if(value != null) {
                    result.set(value);
                    semaphore.release();
                    adjacentCameras.removeObserver(this);
                }
            }
        };

        executors.mainThread().execute(() -> adjacentCameras.observeForever(observer));
        try {
            if(!semaphore.tryAcquire(5, TimeUnit.SECONDS)) {
                return null;
            }
            return result.get();
        } catch (InterruptedException ignored) {
        } finally {
            executors.mainThread().execute(() -> adjacentCameras.removeObserver(observer));
        }

        return null;
    }

    /** Finds the next (1) or previous (-1) camera in list order, wrapping around, and returns it on the main thread. */
    public void loadAdjacentCamera(int direction, Consumer<Camera> callback) {
        this.executors.dbIO().execute(() -> {
            Pair<Camera, Camera> cameraPair = awaitAdjacentCamera();

            if(cameraPair == null) {
                executors.mainThread().execute(() -> callback.accept(null));
                return;
            }

            executors.mainThread().execute(() -> {
                if(direction < 0) {
                    callback.accept(cameraPair.first);
                }else if(direction > 0) {
                    callback.accept(cameraPair.second);
                }
            });
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        this.saveCameraThumbnailUseCase.dispose();
        this.loadAdjacentCamerasUseCase.dispose();
    }

    public void startReconnectTimer() {
        if(timerRunning)
            return;

        int userReconnectTimer = this.settings.getReconnectTimer();

        timerRunning = true;
        reconnectDeadlineSeconds = SystemClock.elapsedRealtime() + userReconnectTimer * 1000L;
        reconnectTimer.setValue(userReconnectTimer);

        executors.mainThread().executeDelayed(timerRunnable, 1, TimeUnit.SECONDS);
    }

    public void stopReconnectTimer() {
        timerRunning = false;
        reconnectTimer.setValue(null);
    }

    public boolean isReconnectTimerRunning() {
        return this.timerRunning && reconnectDeadlineSeconds > SystemClock.elapsedRealtime();
    }

    public void setStreamParams(Params newParams) {
        Objects.requireNonNull(newParams);

        if (this.streamParams.getValue() != null && newParams.equals(this.streamParams.getValue()))
            return;

        streamParams.setValue(newParams);

        adjacentCameras.setValue(null);
        loadAdjacentCamerasUseCase.execute(newParams.getCameraId(), new DisposableSingleObserver<Pair<Camera, Camera>>() {
            @Override
            public void onSuccess(Pair<Camera, Camera> adjacentCameras) {
                if(adjacentCameras == null) {
                    PlayerCameraViewModel.this.adjacentCameras.postValue(Pair.create(null, null));
                }else {
                    PlayerCameraViewModel.this.adjacentCameras.postValue(adjacentCameras);
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {
                e.printStackTrace();
            }
        });
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

    public LiveData<Pair<Camera, Camera>> getAdjacentCamera() {
        return this.adjacentCameras;
    }

    public LiveData<Integer> getReconnectTimer() {
        return this.reconnectTimer;
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
