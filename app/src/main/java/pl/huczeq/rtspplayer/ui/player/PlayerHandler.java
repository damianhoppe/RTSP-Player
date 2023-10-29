package pl.huczeq.rtspplayer.ui.player;

public interface PlayerHandler {
    void togglePlayerControlVisibility();
    void reconnect();
    void switchVolume();
    void enterPlayerIntoPictureInPictureMode();

    void back();
}
