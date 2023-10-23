package pl.huczeq.rtspplayer.domain;

import com.google.common.base.Strings;

import javax.inject.Inject;
import javax.inject.Singleton;

import pl.huczeq.rtspplayer.Settings;
import pl.huczeq.rtspplayer.data.model.CameraInstance;
import pl.huczeq.rtspplayer.data.repositories.base.CameraRepository;
import pl.huczeq.rtspplayer.data.repositories.base.CameraThumbnailRepository;

@Singleton
public class StartingCameraIntegrityHelper {

    private final Settings settings;

    @Inject
    public StartingCameraIntegrityHelper(CameraRepository cameraRepository, Settings settings) {
        this.settings = settings;
        cameraRepository.getCameraInstancesInvalidatedSubject().subscribe(this::accept);
    }

    public void accept(CameraInstance cameraInstance) {
        long cameraInstanceId = cameraInstance.getId();
        if(settings.getAppStartCameraId() == cameraInstanceId) {
            settings.setAppStartCameraId(-1);
        }
    }
}
