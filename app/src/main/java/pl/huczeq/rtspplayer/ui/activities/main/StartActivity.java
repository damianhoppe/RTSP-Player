package pl.huczeq.rtspplayer.ui.activities.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import pl.huczeq.rtspplayer.ui.activities.MainActivity;
import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.ui.activities.base.BaseActivity;

public class StartActivity extends BaseActivity {

    final String TAG = "StartActivity";
    boolean activityReady = false;
    boolean dataLoaded = false;
    boolean activityMainStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        /* MIN ACTIVITY TIME FOR TEST */
        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "ActivityRead");
                activityReady = true;
                tryStartNextActivity();
            }
        }, 30);
    }

    @Override
    protected void onResume() {
        super.onResume();
        tryStartNextActivity();
    }

    @Override
    protected void onDataChangedWAA() {
        super.onDataChangedWAA();
        Log.d(TAG, "DataLoaded");
        dataLoaded = true;
        tryStartNextActivity();
    }

    public void tryStartNextActivity() {
        Log.d(TAG, "activityReady: " + activityReady);
        Log.d(TAG, "dataLoaded: " + dataLoaded);
        if(activityReady && dataLoaded) {
            if(activityMainStarted)
                return;
            Log.d(TAG, "OK");
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            activityMainStarted = true;
        }
    }
}