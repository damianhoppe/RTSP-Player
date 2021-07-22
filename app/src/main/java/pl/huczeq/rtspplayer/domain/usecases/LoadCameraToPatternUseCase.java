package pl.huczeq.rtspplayer.domain.usecases;

import android.util.Log;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;
import pl.huczeq.rtspplayer.AppExecutors;
import pl.huczeq.rtspplayer.data.model.Camera;
import pl.huczeq.rtspplayer.data.model.CameraInstance;
import pl.huczeq.rtspplayer.data.model.CameraPattern;
import pl.huczeq.rtspplayer.data.repositories.base.CameraRepository;
import pl.huczeq.rtspplayer.domain.cameragenerator.CameraPatternMapper;
import pl.huczeq.rtspplayer.domain.usecases.base.SingleUseCase;

public class LoadCameraToPatternUseCase extends SingleUseCase<CameraPattern, LoadCameraToPatternUseCase.Params> {

    private CameraRepository cameraRepository;

    @Inject
    public LoadCameraToPatternUseCase(AppExecutors executors, CameraRepository cameraRepository) {
        super(executors);
        this.cameraRepository = cameraRepository;
    }

    public static CameraPattern toCameraPattern(Camera camera) {
        CameraInstance cameraInstance = camera.getCameraInstance();
        CameraPattern cameraPattern = camera.getCameraPattern();
        CameraPattern newCameraPattern = CameraPatternMapper.fillCameraPattern(cameraPattern, cameraInstance.getVariablesData());
        newCameraPattern.setId(cameraPattern.getId());
        return newCameraPattern;
    }

    public static CameraPattern toCameraPatternGroup(Camera camera) {
        CameraPattern cameraPattern = camera.getCameraPattern();
        CameraPattern newCameraPattern = CameraPatternMapper.hideSpecialVariableNames(cameraPattern);
        newCameraPattern.setId(cameraPattern.getId());
        return newCameraPattern;
    }

    @Override
    protected Single<CameraPattern> buildObservable(Params params) {
        return Single.fromCallable(new Callable<CameraPattern>() {
            @Override
            public CameraPattern call() throws Exception {
                Camera camera = cameraRepository.getCameraByIdSync(params.cameraInstanceId);
                if(params.mode == Mode.LOAD_CAMERA_GROUP) {
                    return toCameraPatternGroup(camera);
                }
                CameraPattern cameraPattern = toCameraPattern(camera);
                return cameraPattern;
            }
        });
    }


    public static class Params {
        public final long cameraInstanceId;
        public final @Mode int mode;

        public Params(long cameraId, int mode) {
            this.cameraInstanceId = cameraId;
            this.mode = mode;
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Mode.LOAD_CAMERA_INSTANCE, Mode.LOAD_CAMERA_GROUP})
    public @interface Mode {
        int LOAD_CAMERA_INSTANCE = 1;
        int LOAD_CAMERA_GROUP = 2;
    }
}
