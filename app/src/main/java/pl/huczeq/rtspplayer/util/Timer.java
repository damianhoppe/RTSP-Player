package pl.huczeq.rtspplayer.util;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

public class Timer {

    public interface Callback {
        void onStart();
        void onFinished();
    }

    private int delay;
    private Callback callback;
    private Thread thread;
    private Handler uiThreadHandler = new Handler(Looper.getMainLooper());

    public Timer(int delay, @NotNull Callback callback) {
        this.delay = delay;
        this.callback = callback;
    }

    public void start() {
        stopWithoutNotifying();
        thread = createThread();
        thread.start();
    }

    public void stop() {
        if(thread != null) {
            thread.interrupt();
            callback.onFinished();
        }
        thread = null;
    }

    public void stopWithoutNotifying() {
        if(thread != null)
            thread.interrupt();
        thread = null;
    }

    private Thread createThread() {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                final Thread currentThread = Thread.currentThread();
                if(currentThread.isInterrupted())
                    return;
                uiThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(currentThread.isInterrupted())
                            return;
                        callback.onStart();
                    }
                });
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
                uiThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(currentThread.isInterrupted())
                            return;
                        callback.onFinished();
                    }
                });
            }
        });
    }
}
