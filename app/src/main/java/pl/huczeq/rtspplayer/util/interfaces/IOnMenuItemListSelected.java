package pl.huczeq.rtspplayer.util.interfaces;

import android.view.MenuItem;

public interface IOnMenuItemListSelected<T> {
    void onMenuItemSelected(MenuItem menuItem, T item);
}
