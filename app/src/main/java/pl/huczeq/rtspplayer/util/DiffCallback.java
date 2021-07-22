package pl.huczeq.rtspplayer.util;

import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

public abstract class DiffCallback<T> extends DiffUtil.Callback {

    protected List<T> oldItems;
    protected List<T> newItems;

    public DiffCallback(List<T> oldItems, List<T> newItems) {
        this.oldItems = oldItems;
        this.newItems = newItems;
    }

    @Override
    public int getOldListSize() {
        return this.oldItems.size();
    }

    @Override
    public int getNewListSize() {
        return this.newItems.size();
    }
}
