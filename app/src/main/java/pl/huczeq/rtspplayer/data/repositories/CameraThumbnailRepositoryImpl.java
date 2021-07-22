package pl.huczeq.rtspplayer.data.repositories;

import android.graphics.Bitmap;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import pl.huczeq.rtspplayer.data.repositories.base.CameraThumbnailRepository;
import pl.huczeq.rtspplayer.data.sources.cache.ThumbnailCache;
import pl.huczeq.rtspplayer.data.sources.local.ThumbnailDiskCache;

@Singleton
public class CameraThumbnailRepositoryImpl implements CameraThumbnailRepository {

    public static final String TAG = "CameraThumbnailRepository";

    private final ThumbnailCache thumbnailCache;
    private final ThumbnailDiskCache thumbnailDiskCache;
    private final PublishSubject<String> thumbnailUpdatedSubject = PublishSubject.create();

    @Inject
    public CameraThumbnailRepositoryImpl(ThumbnailCache thumbnailCache, ThumbnailDiskCache thumbnailDiskCache) {
        this.thumbnailCache = thumbnailCache;
        this.thumbnailDiskCache = thumbnailDiskCache;
    }

    @Override
    public PublishSubject<String> getThumbnailUpdatedSubject() {
        return this.thumbnailUpdatedSubject;
    }

    @Override
    public void saveThumbnail(String name, Bitmap bitmap) {
        this.thumbnailCache.save(name, bitmap);
        this.thumbnailUpdatedSubject.onNext(name);
        this.thumbnailDiskCache.saveThumbnail(name, bitmap);
    }

    @Override
    public Bitmap getThumbnail(String name) {
        Bitmap bitmap = this.thumbnailCache.get(name);
        if(bitmap == null)
            loadThumbnailFromDisk(name);
        return bitmap;
    }

    @Override
    public void deleteThumbnail(String previewImg) {
        this.thumbnailDiskCache.deleteThumbnail(previewImg);
    }

    protected void loadThumbnailFromDisk(String name) {
        thumbnailDiskCache.getThumbnail(name, new SingleObserver<Bitmap>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {}

            @Override
            public void onSuccess(@NonNull Bitmap bitmap) {
                CameraThumbnailRepositoryImpl.this.thumbnailCache.save(name, bitmap);
                CameraThumbnailRepositoryImpl.this.notifyThumbnailUpdated(name);
            }

            @Override
            public void onError(@NonNull Throwable e) {}
        });
    }

    protected void notifyThumbnailUpdated(String name) {
        this.thumbnailUpdatedSubject.onNext(name);
    }
}
