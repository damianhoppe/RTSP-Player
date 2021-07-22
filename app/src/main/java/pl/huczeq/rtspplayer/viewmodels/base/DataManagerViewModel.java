package pl.huczeq.rtspplayer.viewmodels.base;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModel;

import org.jetbrains.annotations.NotNull;

import pl.huczeq.rtspplayer.data.DataManager;

public class DataManagerViewModel extends ViewModel {

    protected DataManager dataManager;

    public DataManagerViewModel(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }
}
