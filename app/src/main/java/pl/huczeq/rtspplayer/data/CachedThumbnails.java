package pl.huczeq.rtspplayer.data;

import android.graphics.Bitmap;

import java.util.HashMap;
import java.util.Map;

import pl.huczeq.rtspplayer.data.objects.CameraInstance;

public class CachedThumbnails {

    private HashMap<String, Bitmap> cachedBitmaps = new HashMap<>();

    public void addCachedImage(CameraInstance cameraInstance, Bitmap bitmap) {
        synchronized (CachedThumbnails.class) {
            if(getCachedBitmap(cameraInstance) != null)
                cachedBitmaps.remove(cameraInstance.getPreviewImg());
            cachedBitmaps.put(cameraInstance.getPreviewImg(), bitmap);
        }
    }

    public Bitmap getCachedBitmap(CameraInstance cameraInstance) {
        if(cameraInstance.getPreviewImg() == null) return null;
        synchronized (CachedThumbnails.class) {
            for (Map.Entry<String, Bitmap> entry : cachedBitmaps.entrySet()) {
                if (cameraInstance.getPreviewImg().equalsIgnoreCase(entry.getKey()))
                    return entry.getValue();
            }
            return null;
        }
    }
}
