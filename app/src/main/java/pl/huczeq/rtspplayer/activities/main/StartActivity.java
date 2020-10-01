package pl.huczeq.rtspplayer.activities.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import pl.huczeq.rtspplayer.activities.MainActivity;
import pl.huczeq.rtspplayer.utils.data.DataManager;
import pl.huczeq.rtspplayer.R;

public class StartActivity extends AppCompatActivity implements DataManager.Callback {

    final String TAG = "StartActivity";
    boolean activityReady = false;
    boolean dataLoaded = false;
    boolean adsInitialized = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        /* DATA */
        DataManager d = DataManager.getInstance(this, this);
        if(d.isDataLoaded())
            onDataLoaded();

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
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        pause = false;
        tryStartNextActivity();
    }

    boolean pause = false;
    @Override
    protected void onPause() {
        super.onPause();
        pause = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDataLoaded() {
        Log.d(TAG, "DataLoaded");
        dataLoaded = true;
        tryStartNextActivity();
    }

    public void tryStartNextActivity() {
        Log.d(TAG, "activityReady: " + activityReady);
        Log.d(TAG, "dataLoaded: " + dataLoaded);
        Log.d(TAG, "adsInitialized: " + adsInitialized);
        Log.d(TAG, "pause: " + pause);
        if(activityReady && dataLoaded && adsInitialized && !pause) {
            Log.d(TAG, "OK");
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
    }
}