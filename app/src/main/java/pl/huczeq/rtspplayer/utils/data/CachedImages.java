package pl.huczeq.rtspplayer.utils.data;

import android.graphics.Bitmap;

import java.util.HashMap;
import java.util.Map;

public class CachedImages {

    private static HashMap<String, Bitmap> cachedBitmaps = new HashMap<>();

    public static void addCachedImage(Camera camera, Bitmap bitmap) {
        synchronized (cachedBitmaps) {
            cachedBitmaps.put(camera.getPreviewImg(), bitmap);
        }
    }

    public static Bitmap getCachedBitmap(Camera camera) {
        synchronized (cachedBitmaps) {
            for (Map.Entry<String, Bitmap> entry : cachedBitmaps.entrySet()) {
                if (camera.getPreviewImg().equalsIgnoreCase(entry.getKey()))
                    return entry.getValue();
            }
            return null;
        }
    }
}
