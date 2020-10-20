package pl.huczeq.rtspplayer.data.threads;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Objects;

import pl.huczeq.rtspplayer.data.Settings;
import pl.huczeq.rtspplayer.data.CachedImages;
import pl.huczeq.rtspplayer.data.objects.Camera;

public class ImageLoadingThread extends Thread{

    public static Message createMessage(Data data) {
        if(data.getCamera() == null || data.getCallback() == null || data.getCamera().getPreviewImg() == null || data.getCamera().getPreviewImg().equalsIgnoreCase("")) return null;
        Message message = new Message();
        message.what = 1;
        message.obj = data;
        return message;
    }

    private final String TAG = "ImageLoadingThread";

    private Handler handler;
    private Context context;

    public ImageLoadingThread(Context context) {
        this.context = context;
    }

    @Override
    public void run() {
        Looper.prepare();
        handler = new Handler(Objects.requireNonNull(Looper.myLooper()), new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message message) {
                final Data data = (Data)message.obj;
                Bitmap bitmap = CachedImages.getCachedBitmap(data.getCamera());
                if(bitmap == null) {
                    Settings settings = Settings.getInstance(context);
                    File f = new File(settings.getPreviewImagesDir(), data.getCamera().getPreviewImg());
                    if(!f.exists()) {
                        return false;
                    }
                    bitmap = loadBitmapFromFile(f);
                    if(bitmap == null) returnMessageToQueue(message);;
                    CachedImages.addCachedImage(data.camera, bitmap);
                }
                final Bitmap finalBitmap = bitmap;
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        data.callback.onImageLoaded(data, finalBitmap);
                    }
                });
                return false;
            }
        });
        Looper.loop();
    }

    private void returnMessageToQueue(Message message) {
        final Data data = (Data) message.obj;
        if(data.getNumberOfReturns() >= 2) {
            Log.e(TAG, "Returned message to queue " + data.getNumberOfReturns() + " times, name: " + data.getCamera().getName() + ", img: " + data.getCamera().getPreviewImg());
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "Returned message to queue " + data.getNumberOfReturns() + " times. " + data.getCamera().getName() + "," + data.getCamera().getPreviewImg(), Toast.LENGTH_LONG).show();
                }
            });
            return;
        }
        data.returnToQueue();
        this.sendMessageDelayed(Message.obtain(message), 1000*30);
    }

    public void sendMessageDelayed(Message message, int delay) {
        if(!this.handler.hasMessages(1, message.obj)) this.handler.sendMessageDelayed(message, delay);
    }

    public void sendMessage(Message message) {
        if(!this.handler.hasMessages(1, message.obj)) this.handler.sendMessage(message);
    }

    public Bitmap loadBitmapFromFile(File file) {
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        try {
            bitmap = BitmapFactory.decodeStream(new FileInputStream(file), null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static class Data {
        Camera camera;
        Callback callback;
        int returns;

        public Data(Camera camera, Callback callback) {
            this.camera = camera;
            this.callback = callback;
            this.returns = 0;
        }

        public Camera getCamera() {
            return camera;
        }

        public void setCamera(Camera camera) {
            this.camera = camera;
        }

        public Callback getCallback() {
            return callback;
        }

        public void setCallback(Callback callback) {
            this.callback = callback;
        }

        public int getNumberOfReturns() {
            return this.returns;
        }

        public void returnToQueue() {
            this.returns++;
        }
    }

    public interface Callback {
        void onImageLoaded(Data data, Bitmap bitmap);
    }
}
