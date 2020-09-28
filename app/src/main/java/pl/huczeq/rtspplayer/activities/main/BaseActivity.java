package pl.huczeq.rtspplayer.activities.main;

import android.os.Bundle;

import pl.huczeq.rtspplayer.interfaces.OnDataChanged;
import pl.huczeq.rtspplayer.utils.data.Camera;
import pl.huczeq.rtspplayer.utils.data.DataManager;

public class BaseActivity extends AbstractBaseActivity implements OnDataChanged {

    private final String TAG = "BaseActivity";
    private boolean dataChanged = false;
    private boolean active = false;

    protected DataManager dataManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dataManager = DataManager.getInstance(this);
        dataManager.loadData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataManager.removeOnDataChangeListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.active = true;
        if(this.dataChanged) onDataChangedWAA();
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.active = false;
    }

    @Override
    protected void setViewsWidgets() {
    }

    /**
     *called when data is changed and activity is active
     */
    protected void onDataChangedWAA() {
        this.dataChanged = false;
    }

    @Override
    public void onDataChanged() {
        if(this.active) onDataChangedWAA(); else this.dataChanged = true;
    }

    public void enableOnDataChangeListener() {
        dataManager.addOnDataChangedListener(this);
    }
}