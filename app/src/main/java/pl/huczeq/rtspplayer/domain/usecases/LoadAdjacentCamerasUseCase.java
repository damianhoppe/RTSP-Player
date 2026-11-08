package pl.huczeq.rtspplayer.domain.usecases;

import android.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;
import pl.huczeq.rtspplayer.AppExecutors;
import pl.huczeq.rtspplayer.data.model.Camera;
import pl.huczeq.rtspplayer.data.repositories.base.CameraRepository;
import pl.huczeq.rtspplayer.domain.usecases.base.SingleUseCase;

public class LoadAdjacentCamerasUseCase extends SingleUseCase<Pair<Camera, Camera>, Long> {

    private final CameraRepository cameraRepository;

    @Inject
    public LoadAdjacentCamerasUseCase(AppExecutors executors, CameraRepository cameraRepository) {
        super(executors);
        this.cameraRepository = cameraRepository;
    }

    @Override
    protected Single<Pair<Camera, Camera>> buildObservable(Long cameraInstanceId) {
        return Single.fromCallable(new Callable<Pair<Camera, Camera>>() {
            @Override
            public Pair<Camera, Camera> call() throws Exception {
                List<Camera> cameras = new ArrayList<>(cameraRepository.getAllCameras());
                if (cameras.size() < 2)
                    return null;
                Collections.sort(cameras, new Comparator<Camera>() {
                    @Override
                    public int compare(Camera c1, Camera c2) {
                        long pattern1 = c1.getCameraInstance().getPatternId();
                        long pattern2 = c2.getCameraInstance().getPatternId();

                        if (pattern1 < pattern2) return -1;
                        if (pattern1 > pattern2) return 1;

                        long id1 = c1.getCameraInstance().getId();
                        long id2 = c2.getCameraInstance().getId();

                        if (id1 < id2) return -1;
                        if (id1 > id2) return 1;

                        return 0;
                    }
                });
                int index = -1;
                for (int i = 0; i < cameras.size(); i++) {
                    if (cameras.get(i).getCameraInstance().getId() == cameraInstanceId) {
                        index = i;
                        break;
                    }
                }
                if (index < 0)
                    return null;

                Camera previous = cameras.get(Math.floorMod(index - 1, cameras.size()));
                Camera next = cameras.get(Math.floorMod(index + 1, cameras.size()));
                return new Pair<>(previous, next);
            }
        });
    }
}
