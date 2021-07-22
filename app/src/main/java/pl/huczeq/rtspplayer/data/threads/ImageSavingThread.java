package pl.huczeq.rtspplayer.data.threads;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

import pl.huczeq.rtspplayer.data.Settings;
import pl.huczeq.rtspplayer.data.objects.CameraInstance;

public class ImageSavingThread extends Thread{

    public static Message createMessage(ImageSavingThread.Data data) {
        if(data.getCamera() == null || data.getBitmap() == null) return null;
        Message message = new Message();
        message.what = 1;
        message.obj = data;
        return message;
    }

    private final String TAG = "ImageSavingThread";

    private Handler handler;
    private Context context;

    public ImageSavingThread(Context context) {
        this.context = context;
    }

    @Override
    public void run() {
        Looper.prepare();
        handler = new Handler(Objects.requireNonNull(Looper.myLooper()), new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message message) {
                final ImageSavingThread.Data data = (ImageSavingThread.Data)message.obj;
                if(!saveBitmapToFile(data.cameraInstance, data.bitmap)) {
                    returnMessageToQueue(message);
                }
                return false;
            }
        });
        Looper.loop();
    }

    private void returnMessageToQueue(Message message) {
        ImageSavingThread.Data data = (ImageSavingThread.Data) message.obj;
        if(data.getNumberOfReturns() >= 2) {
            Log.e(TAG, "Returned message to queue " + data.getNumberOfReturns() + " times. " + data.getCamera().getName() + "," + data.getCamera().getPreviewImg());
            return;
        }
        data.returnToQueue();
        this.sendMessageDelayed(message, 1000*30);
    }

    public void sendMessageDelayed(Message message, int delay) {
        if(!this.handler.hasMessages(1, message.obj)) this.handler.sendMessageDelayed(message, delay);
    }

    public void sendMessage(Message message) {
        if(!this.handler.hasMessages(1, message.obj)) this.handler.sendMessage(message);
    }

    public boolean saveBitmapToFile(CameraInstance cameraInstance, Bitmap bitmap) {
        Settings settings = Settings.getInstance(context);
        File f = new File(settings.getPreviewImagesDir(), cameraInstance.getPreviewImg());
        try {
            FileOutputStream fouts = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fouts);
            fouts.flush();
            fouts.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static class Data {
        CameraInstance cameraInstance;
        Bitmap bitmap;
        int returns;

        public Data(CameraInstance cameraInstance, Bitmap bitmap) {
            this.cameraInstance = cameraInstance;
            this.bitmap = bitmap;
            this.returns = 0;
        }

        public CameraInstance getCamera() {
            return cameraInstance;
        }

        public void setCamera(CameraInstance cameraInstance) {
            this.cameraInstance = cameraInstance;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }

        public void setBitmap(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        public int getNumberOfReturns() {
            return this.returns;
        }

        public void returnToQueue() {
            this.returns++;
        }
    }
}
