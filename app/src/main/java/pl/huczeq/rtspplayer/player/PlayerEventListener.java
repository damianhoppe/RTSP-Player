package pl.huczeq.rtspplayer.player;

public interface PlayerEventListener {
    void onStartRendering();
    void onPlaying();
    void onEndReached();
    void onEncounteredError();
}
