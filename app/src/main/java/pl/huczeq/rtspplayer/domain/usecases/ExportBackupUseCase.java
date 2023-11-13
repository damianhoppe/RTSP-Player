package pl.huczeq.rtspplayer.domain.usecases;

import java.io.OutputStream;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Completable;
import pl.huczeq.rtspplayer.AppExecutors;
import pl.huczeq.rtspplayer.domain.backup.ExportBackupTask;
import pl.huczeq.rtspplayer.domain.usecases.base.CompletableUseCase;

public class ExportBackupUseCase extends CompletableUseCase<ExportBackupUseCase.Params> {

    private ExportBackupTask exportBackupTask;

    @Inject
    public ExportBackupUseCase(AppExecutors executors, ExportBackupTask exportBackupTask) {
        super(executors);
        this.exportBackupTask = exportBackupTask;
    }

    @Override
    protected Completable buildObservable(Params params) {
        this.exportBackupTask.init(params.outputStream, params.exportCameras, params.exportSettings);
        return Completable.fromRunnable(this.exportBackupTask);
    }

    public static class Params {
        public final OutputStream outputStream;
        public final boolean exportCameras;
        public final boolean exportSettings;

        public Params(OutputStream outputStream, boolean exportCameras, boolean exportSettings) {
            this.outputStream = outputStream;
            this.exportCameras = exportCameras;
            this.exportSettings = exportSettings;
        }
    }
}
