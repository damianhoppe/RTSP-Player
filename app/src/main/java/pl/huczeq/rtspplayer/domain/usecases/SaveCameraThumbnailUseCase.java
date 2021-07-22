package pl.huczeq.rtspplayer.domain.usecases;

import android.graphics.Bitmap;

import androidx.room.util.StringUtil;

import com.google.common.base.Strings;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Completable;
import pl.huczeq.rtspplayer.AppExecutors;
import pl.huczeq.rtspplayer.data.model.CameraInstance;
import pl.huczeq.rtspplayer.data.repositories.base.CameraRepository;
import pl.huczeq.rtspplayer.data.repositories.base.CameraThumbnailRepository;
import pl.huczeq.rtspplayer.domain.usecases.base.CompletableUseCase;

public class SaveCameraThumbnailUseCase extends CompletableUseCase<SaveCameraThumbnailUseCase.Params> {

    private CameraRepository cameraRepository;
    private CameraThumbnailRepository thumbnailRepository;

    @Inject
    public SaveCameraThumbnailUseCase(AppExecutors executors, CameraRepository cameraRepository, CameraThumbnailRepository cameraThumbnailRepository) {
        super(executors);
        this.cameraRepository = cameraRepository;
        this.thumbnailRepository = cameraThumbnailRepository;
    }

    @Override
    protected Completable buildObservable(Params params) {
        return Completable.fromRunnable(new Runnable() {
            @Override
            public void run() {
                CameraInstance cameraInstance = cameraRepository.getCameraInstanceByIdSync(params.cameraId);
                String thumbnailFileName = cameraInstance.getPreviewImg();
                boolean fileNameChanged = false;
                if(Strings.isNullOrEmpty(thumbnailFileName)) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
                    thumbnailFileName = cameraInstance.getId() + "_" + simpleDateFormat.format(Calendar.getInstance().getTime()) + ".png";
                    cameraInstance.setPreviewImg(thumbnailFileName);
                    fileNameChanged = true;
                }
                thumbnailRepository.saveThumbnail(thumbnailFileName, params.thumbnailBitmap);
                if(fileNameChanged)
                    cameraRepository.updateCameraInstanceSync(cameraInstance);
            }
        });
    }

    public static class Params {
        private long cameraId;
        private Bitmap thumbnailBitmap;

        public Params(long cameraId, Bitmap thumbnailBitmap) {
            this.cameraId = cameraId;
            this.thumbnailBitmap = thumbnailBitmap;
        }
    }
}
