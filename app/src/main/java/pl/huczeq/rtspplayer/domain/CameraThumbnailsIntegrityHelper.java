package pl.huczeq.rtspplayer.domain;

import android.util.Log;

import com.google.common.base.Strings;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.disposables.Disposable;
import pl.huczeq.rtspplayer.data.model.CameraInstance;
import pl.huczeq.rtspplayer.data.repositories.base.CameraRepository;
import pl.huczeq.rtspplayer.data.repositories.base.CameraThumbnailRepository;

@Singleton
public class CameraThumbnailsIntegrityHelper {

    private final CameraThumbnailRepository cameraThumbnailRepository;

    @Inject
    public CameraThumbnailsIntegrityHelper(CameraRepository cameraRepository, CameraThumbnailRepository cameraThumbnailRepository) {
        this.cameraThumbnailRepository = cameraThumbnailRepository;
        cameraRepository.getCameraInstancesInvalidatedSubject().subscribe(this::accept);
    }

    public void accept(CameraInstance cameraInstance) {
        if(Strings.isNullOrEmpty(cameraInstance.getPreviewImg()))
            return;
        cameraThumbnailRepository.deleteThumbnail(cameraInstance.getPreviewImg());
    }
}
