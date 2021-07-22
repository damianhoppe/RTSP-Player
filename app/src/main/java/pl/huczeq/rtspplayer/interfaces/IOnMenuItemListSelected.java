package pl.huczeq.rtspplayer.interfaces;

import android.view.MenuItem;

import pl.huczeq.rtspplayer.data.objects.Camera;

public interface IOnMenuItemListSelected {
    void onMenuItemSelected(MenuItem menuItem, Camera camera);
}
