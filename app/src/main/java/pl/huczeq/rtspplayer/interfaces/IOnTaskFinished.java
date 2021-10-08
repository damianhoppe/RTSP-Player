package pl.huczeq.rtspplayer.interfaces;

public interface IOnTaskFinished {
    void onComplete();
    void onError(Exception exception);
}
