package pl.huczeq.rtspplayer.data.utils;


import java.lang.ref.WeakReference;

public abstract class WeakCallback<T> {

    protected WeakReference<T> callbackReference;

    public WeakCallback(T callback) {
        if(callback != null)
            callbackReference = new WeakReference<>(callback);
    }

    public WeakReference<T> getCallbackReference() {
        return callbackReference;
    }
}
