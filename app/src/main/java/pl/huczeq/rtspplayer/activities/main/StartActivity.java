package pl.huczeq.rtspplayer.activities.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import pl.huczeq.rtspplayer.activities.MainActivity;
import pl.huczeq.rtspplayer.utils.data.DataManager;
import pl.huczeq.rtspplayer.R;

public class StartActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        dataManager.loadData();

        //TODO First launch

        startActivity(new Intent(getApplicationContext(), MainActivity.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("StartActivity", "DESTROYED");
    }
}