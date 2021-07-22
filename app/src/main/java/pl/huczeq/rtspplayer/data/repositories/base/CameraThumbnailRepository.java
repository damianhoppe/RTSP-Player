package pl.huczeq.rtspplayer.data.repositories.base;

import android.graphics.Bitmap;

import io.reactivex.rxjava3.subjects.PublishSubject;

public interface CameraThumbnailRepository {

    PublishSubject<String> getThumbnailUpdatedSubject();
    void saveThumbnail(String id, Bitmap bitmap);
    Bitmap getThumbnail(String id);
    void deleteThumbnail(String previewImg);
}
