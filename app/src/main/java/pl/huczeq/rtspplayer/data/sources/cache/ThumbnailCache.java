package pl.huczeq.rtspplayer.data.sources.cache;

import android.graphics.Bitmap;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ThumbnailCache {

    private Map<String, Bitmap> bitmaps;

    @Inject
    public ThumbnailCache() {
        this.bitmaps = new HashMap<>();
    }

    public void save(String key, Bitmap bitmap) {
        bitmaps.put(key, bitmap);
    }

    public Bitmap get(String key) {
        if(key == null)
            return null;
        return bitmaps.get(key);
    }

    public void cleanUp() {
        bitmaps.clear();
    }
}
