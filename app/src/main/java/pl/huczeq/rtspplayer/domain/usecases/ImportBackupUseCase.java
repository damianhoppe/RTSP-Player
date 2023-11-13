package pl.huczeq.rtspplayer.domain.usecases;

import java.io.InputStream;
import java.io.OutputStream;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import pl.huczeq.rtspplayer.AppExecutors;
import pl.huczeq.rtspplayer.domain.backup.ImportBackupTask;
import pl.huczeq.rtspplayer.domain.backup.LoadBackupTask;
import pl.huczeq.rtspplayer.domain.backup.LoadedBackup;
import pl.huczeq.rtspplayer.domain.usecases.base.CompletableUseCase;
import pl.huczeq.rtspplayer.domain.usecases.base.SingleUseCase;

public class ImportBackupUseCase extends CompletableUseCase<ImportBackupUseCase.Params> {

    private ImportBackupTask importBackupTask;

    @Inject
    public ImportBackupUseCase(AppExecutors executors, ImportBackupTask importBackupTask) {
        super(executors);
        this.importBackupTask = importBackupTask;
    }

    @Override
    protected Completable buildObservable(ImportBackupUseCase.Params params) {
        this.importBackupTask.init(params.loadedBackup, params.importCameras, params.clearImport, params.importSettings);
        return Completable.fromRunnable(this.importBackupTask);
    }

    public static class Params {
        public final LoadedBackup loadedBackup;
        public final boolean importCameras;
        public final boolean importSettings;
        public final boolean clearImport;

        public Params(LoadedBackup loadedBackup, boolean importCameras, boolean importSettings, boolean clearImport) {
            this.loadedBackup = loadedBackup;
            this.importCameras = importCameras;
            this.importSettings = importSettings;
            this.clearImport = clearImport;
        }
    }
}
