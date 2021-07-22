package pl.huczeq.rtspplayer.viewmodels.factories;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.jetbrains.annotations.NotNull;

import pl.huczeq.rtspplayer.data.DataManager;
import pl.huczeq.rtspplayer.viewmodels.CamerasListViewModel;
import pl.huczeq.rtspplayer.viewmodels.CreateBackupViewModel;
import pl.huczeq.rtspplayer.viewmodels.RestoreBackupViewModel;
import pl.huczeq.rtspplayer.viewmodels.StartActivityViewModel;
import pl.huczeq.rtspplayer.viewmodels.base.DataManagerViewModel;

public class DataManagerViewModelFactory implements ViewModelProvider.Factory {

    protected DataManager dataManager;

    public DataManagerViewModelFactory(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @NonNull
    @NotNull
    @Override
    public <T extends ViewModel> T create(@NonNull @NotNull Class<T> modelClass) {
        if(modelClass.isAssignableFrom(CamerasListViewModel.class))
            return (T) new CamerasListViewModel(this.dataManager);
        if(modelClass.isAssignableFrom(CreateBackupViewModel.class))
            return (T) new CreateBackupViewModel(this.dataManager);
        if(modelClass.isAssignableFrom(RestoreBackupViewModel.class))
            return (T) new RestoreBackupViewModel(this.dataManager);
        if(modelClass.isAssignableFrom(StartActivityViewModel.class))
            return (T) new StartActivityViewModel(this.dataManager);
        return (T) new DataManagerViewModel(this.dataManager);
    }
}
