package pl.huczeq.rtspplayer.activities.main;

import android.os.Bundle;

import pl.huczeq.rtspplayer.utils.data.DataManager;

public class FirstLaunchActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DataManager d = DataManager.getInstance(this);
        d.loadData();
    }
}
