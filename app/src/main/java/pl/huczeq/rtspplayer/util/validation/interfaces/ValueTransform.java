package pl.huczeq.rtspplayer.util.validation.interfaces;


public interface ValueTransform<T> {

    T transform(T value);
}