package pl.huczeq.rtspplayer.ui.activities.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import pl.huczeq.rtspplayer.data.Settings;
import pl.huczeq.rtspplayer.ui.activities.MainActivity;
import pl.huczeq.rtspplayer.data.DataManager;
import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.ui.activities.base.BaseActivity;

public class StartActivity extends BaseActivity {

    final String TAG = "StartActivity";
    boolean activityReady = false;
    boolean dataLoaded = false;
    boolean adsInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        if(dataManager.isDataLoaded())
            onDataChangedWAA();
        /* MIN ACTIVITY TIME FOR TEST */
        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "ActivityRead");
                activityReady = true;
                tryStartNextActivity();
            }
        }, 50);

        Log.d(TAG, String.valueOf(adsInitialized));
        /* ADS INITIALIZE */
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                Log.d(TAG, "AdsInitialized");
                adsInitialized = true;
                tryStartNextActivity();
            }
        });
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
        Log.d(TAG, "adsInitialized: " + adsInitialized);
        if(activityReady && dataLoaded && adsInitialized) {
            Log.d(TAG, "OK");
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
    }
}