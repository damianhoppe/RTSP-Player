package pl.huczeq.rtspplayer.utils.data.threads;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import pl.huczeq.rtspplayer.utils.data.Camera;

public class ImageSavingThread extends Thread{
    Handler handler;
    @Override
    public void run() {
        Looper.prepare();
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message message) {
                return false;
            }
        });
        Looper.loop();
    }

    public Message createMessage(Camera camera) {
        Message message = new Message();
        //message.obj =
        return message;
    }
}
