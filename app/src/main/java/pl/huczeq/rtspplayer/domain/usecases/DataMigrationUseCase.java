package pl.huczeq.rtspplayer.domain.usecases;

import android.content.Context;

import java.io.File;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;
import io.reactivex.rxjava3.core.Completable;
import pl.huczeq.rtspplayer.AppExecutors;
import pl.huczeq.rtspplayer.data.JsonToDatabaseMigration;
import pl.huczeq.rtspplayer.data.repositories.base.CameraRepository;
import pl.huczeq.rtspplayer.data.repositories.base.UrlTemplateRepository;
import pl.huczeq.rtspplayer.data.sources.local.JsonDataFileLoader;
import pl.huczeq.rtspplayer.domain.usecases.base.CompletableUseCase;

public class DataMigrationUseCase extends CompletableUseCase<Object> {

    private Context context;
    private CameraRepository cameraRepository;
    private UrlTemplateRepository urlTemplateRepository;

    @Inject
    public DataMigrationUseCase(@ApplicationContext Context context, AppExecutors executors, CameraRepository cameraRepository, UrlTemplateRepository urlTemplateRepository) {
        super(executors);
        this.context = context;
        this.cameraRepository = cameraRepository;
        this.urlTemplateRepository = urlTemplateRepository;
    }

    @Override
    protected Completable buildObservable(Object o) {
        return Completable.fromRunnable(new Runnable() {
            @Override
            public void run() {
                JsonToDatabaseMigration.tryRestoreData(new File(context.getFilesDir(), JsonDataFileLoader.FILE_NAME), cameraRepository, urlTemplateRepository);
            }
        });
    }
}
