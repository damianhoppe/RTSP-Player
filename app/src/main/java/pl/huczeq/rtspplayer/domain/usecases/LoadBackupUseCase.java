package pl.huczeq.rtspplayer.domain.usecases;

import java.io.InputStream;
import java.io.OutputStream;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import pl.huczeq.rtspplayer.AppExecutors;
import pl.huczeq.rtspplayer.domain.backup.ExportBackupTask;
import pl.huczeq.rtspplayer.domain.backup.LoadBackupTask;
import pl.huczeq.rtspplayer.domain.backup.LoadedBackup;
import pl.huczeq.rtspplayer.domain.usecases.base.CompletableUseCase;
import pl.huczeq.rtspplayer.domain.usecases.base.SingleUseCase;

public class LoadBackupUseCase extends SingleUseCase<LoadedBackup, InputStream> {

    private LoadBackupTask loadBackupTask;

    @Inject
    public LoadBackupUseCase(AppExecutors executors, LoadBackupTask loadBackupTask) {
        super(executors);
        this.loadBackupTask = loadBackupTask;
    }

    @Override
    protected Single<LoadedBackup> buildObservable(InputStream inputStream) {
        this.loadBackupTask.init(inputStream);
        return Single.fromCallable(this.loadBackupTask);
    }
}
