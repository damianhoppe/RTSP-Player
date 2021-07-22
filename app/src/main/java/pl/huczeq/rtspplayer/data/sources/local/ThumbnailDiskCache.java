package pl.huczeq.rtspplayer.data.sources.local;

import android.graphics.Bitmap;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleObserver;
import pl.huczeq.rtspplayer.AppConfiguration;
import pl.huczeq.rtspplayer.AppExecutors;

@Singleton
public class ThumbnailDiskCache {

    private final AppExecutors appExecutors;
    private final AppExecutors.AppExecutor ioExecutor;
    private final File thumbnailsDirectory;

    @Inject
    public ThumbnailDiskCache(AppExecutors appExecutors, AppConfiguration configuration) {
        this.appExecutors = appExecutors;
        this.ioExecutor = new AppExecutors.AppExecutor(Executors.newFixedThreadPool(2));
        this.thumbnailsDirectory = configuration.getThumbnailDirectory();
    }

    public void saveThumbnail(String name, Bitmap bitmap) {
        ioExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    SaveBitmapTask.save(bitmap, thumbnailsDirectory, name);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void getThumbnail(String id, SingleObserver<Bitmap> observer) {
        Single<Bitmap> observable = Single.fromCallable(new Callable<Bitmap>() {
            @Override
            public Bitmap call() throws Exception {
                return ReadBitmapTask.load(thumbnailsDirectory, id);
            }
        }).subscribeOn(ioExecutor.scheduler())
                .observeOn(appExecutors.mainThread().scheduler());
        observable.subscribe(observer);
    }

    public void deleteThumbnail(String name) {
        ioExecutor.execute(new Runnable() {
            @Override
            public void run() {
                File file = new File(thumbnailsDirectory, name);
                if(file.exists())
                    file.delete();
            }
        });
    }
}
