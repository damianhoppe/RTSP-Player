package pl.huczeq.rtspplayer.ui.adapters.base;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayDeque;
import java.util.List;

public abstract class BaseRecyclerViewAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    protected List<T> dataSet;
    private final ArrayDeque<List<T>> pendingUpdates = new ArrayDeque<>();
    private final Handler uiThreadHandler = new Handler(Looper.getMainLooper());

    public BaseRecyclerViewAdapter(@NonNull List<T> dataSet) {
        this.dataSet = dataSet;
    }

    public abstract DiffUtil.Callback buildDiffUtilCallback(List<T> oldDataSet, List<T> newDataSet);

    @MainThread
    public void updateDataSet(List<T> newDataSet) {
        if(this.dataSet == newDataSet)
            return;
        pendingUpdates.add(newDataSet);
        if(pendingUpdates.size() == 1)
            internalUpdate(newDataSet);
    }

    private void internalUpdate(List<T> newDataSet) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DiffUtil.DiffResult result = DiffUtil.calculateDiff(buildDiffUtilCallback(dataSet, newDataSet), false);
                uiThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        dataSet = newDataSet;
                        result.dispatchUpdatesTo(BaseRecyclerViewAdapter.this);
                        processQueue();
                    }
                });
            }
        }).start();
    }

    @MainThread
    protected void processQueue() {
        pendingUpdates.remove();
        if(pendingUpdates.isEmpty())
            return;
        List<T> lastList = pendingUpdates.peekLast();
        if(!pendingUpdates.isEmpty()) {
            pendingUpdates.clear();
        }
        internalUpdate(lastList);
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}
