package pl.huczeq.rtspplayer.viewmodels;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import pl.huczeq.rtspplayer.data.DataManager;
import pl.huczeq.rtspplayer.data.DataConverter;
import pl.huczeq.rtspplayer.viewmodels.base.DataManagerViewModel;

public class StartActivityViewModel extends DataManagerViewModel {

    private MutableLiveData<Boolean> isOldDataConverted;

    public StartActivityViewModel(DataManager dataManager) {
        super(dataManager);
        this.isOldDataConverted = new MutableLiveData<>();
        this.isOldDataConverted.setValue(false);

        new Thread(new Runnable() {
            @Override
            public void run() {
                DataConverter.jsonFile2Database(dataManager);
                isOldDataConverted.postValue(true);
            }
        }).start();
    }

    public MutableLiveData<Boolean> getIsOldDataConverted() {
        return isOldDataConverted;
    }
}
