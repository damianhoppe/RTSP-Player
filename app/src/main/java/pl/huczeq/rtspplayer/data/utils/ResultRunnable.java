package pl.huczeq.rtspplayer.data.utils;

public abstract class ResultRunnable<T> extends WeakCallback<T> implements Runnable {

    public ResultRunnable(T callback) {
        super(callback);
    }
}
