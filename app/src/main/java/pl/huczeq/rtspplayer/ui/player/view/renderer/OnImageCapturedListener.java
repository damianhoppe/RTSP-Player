package pl.huczeq.rtspplayer.ui.player.view.renderer;

import android.graphics.Bitmap;

public interface OnImageCapturedListener {
    void onImageCaptured(Bitmap bitmap, long id);
}
