package pl.huczeq.rtspplayer.data.repositories;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Message;
import android.util.Log;

import pl.huczeq.rtspplayer.data.CachedThumbnails;
import pl.huczeq.rtspplayer.data.Settings;
import pl.huczeq.rtspplayer.data.objects.CameraInstance;
import pl.huczeq.rtspplayer.data.threads.ImageLoadingThread;
import pl.huczeq.rtspplayer.data.threads.ImageSavingThread;
import pl.huczeq.rtspplayer.data.utils.WeakCallback;

public class ThumbnailsRepository {

    public static final String TAG = "ThumbnailRepository";

    private Context appContext;
    private CachedThumbnails cachedThumbnails;
    private ImageLoadingThread imageLoadingThread;
    private ImageSavingThread imageSavingThread;
    private Settings settings;

    public ThumbnailsRepository(Context context) {
        this.appContext = context.getApplicationContext();
        this.cachedThumbnails = new CachedThumbnails();
        this.imageLoadingThread = new ImageLoadingThread(this.appContext);
        this.imageLoadingThread.start();
        this.imageSavingThread = new ImageSavingThread(this.appContext);
        this.imageSavingThread.start();
        this.settings = Settings.getInstance(appContext);
    }

    public void savePreviewImg(CameraInstance cameraInstance, final Bitmap bitmap) {
        Log.d(TAG, "Save preview image for camera: " + cameraInstance.getName());

        if(bitmap == null) return;
        if(cameraInstance.getPreviewImg() == null || cameraInstance.getPreviewImg().trim().isEmpty()) {
            final String fileName = settings.getUniquePreviewImageName();
            cameraInstance.setPreviewImg(fileName);
            Log.d(TAG, "Cached preview image");
        }

        cameraInstance.setPrevImgLastUpdateTime(System.currentTimeMillis());
        cachedThumbnails.addCachedImage(cameraInstance, bitmap);
        Log.d(TAG, "Cached preview image");

        Message msg = ImageSavingThread.createMessage(new ImageSavingThread.Data(cameraInstance, bitmap));
        if(msg == null) return;

        imageSavingThread.sendMessage(msg);
        Log.d(TAG, "Messege sent for ImageSavingThread");
    }

    public void loadPreviewImg(CameraInstance cameraInstance, ImageLoadingThread.Callback callback) {
        loadPreviewImg(new ImageLoadingThread.Data(cameraInstance, new WeakImageLoadingThreadCallback<ImageLoadingThread.Callback>(callback) {
            @Override
            public void onImageLoaded(ImageLoadingThread.Data data, Bitmap bitmap) {
                cachedThumbnails.addCachedImage(cameraInstance, bitmap);
                if(this.callbackReference != null && this.callbackReference.get() != null)
                    this.callbackReference.get().onImageLoaded(data, bitmap);
            }
        }));
    }

    private void loadPreviewImg(ImageLoadingThread.Data data) {
        if(data.getCamera().getPreviewImg() == null || data.getCamera().getPreviewImg().trim().isEmpty()) {
            Log.d(TAG, "loadPreviewImg() - data error: ");
            return;
        }
        Bitmap bitmap = cachedThumbnails.getCachedBitmap(data.getCamera());
        if(bitmap != null) {
            Log.d(TAG, "loadPreviewImg() - cached bitmap is not null");/*
            if(data.getCallback() != null && data.getCallback().get() != null) {
                    data.getCallback().get().onImageLoaded(data, bitmap);
            }*/
            data.getCallback().onImageLoaded(data, bitmap);
            return;
        }
        Log.d(TAG, "loadPreviewImg() - bitmap is null, send");
        Message msg = ImageLoadingThread.createMessage(data);
        if(msg == null) return;
        Log.d(TAG, "loadPreviewImg() - sendMessage");

        this.imageLoadingThread.sendMessage(msg);
    }

    public void finishImageLoadingTask() {
        this.imageLoadingThread.clearQueue();
        this.imageLoadingThread.interrupt();
        this.imageLoadingThread = new ImageLoadingThread(this.appContext);
        this.imageLoadingThread.start();
    }

    public abstract static class WeakImageLoadingThreadCallback<T> extends WeakCallback<T> implements ImageLoadingThread.Callback {

        public WeakImageLoadingThreadCallback(T callback) {
            super(callback);
        }

        @Override
        public abstract void onImageLoaded(ImageLoadingThread.Data data, Bitmap bitmap);
    }
}
