package pl.huczeq.rtspplayer.ui.activities.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import pl.huczeq.rtspplayer.data.DataManager;
import pl.huczeq.rtspplayer.ui.activities.MainActivity;
import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.ui.activities.base.BaseActivity;
import pl.huczeq.rtspplayer.viewmodels.StartActivityViewModel;
import pl.huczeq.rtspplayer.viewmodels.factories.DataManagerViewModelFactory;

public class StartActivity extends BaseActivity {

    final String TAG = "StartActivity";
    boolean activityReady = false;
    boolean dataLoaded = false;
    boolean activityMainStarted = false;

    private StartActivityViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        dataLoaded = false;
        /* MIN ACTIVITY TIME FOR TEST */
        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                activityReady = true;
                tryStartNextActivity();
            }
        }, 30);

        this.viewModel = ViewModelProviders.of(this, new DataManagerViewModelFactory(DataManager.getInstance(getApplicationContext()))).get(StartActivityViewModel.class);
        this.viewModel.getIsOldDataConverted().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean b) {
                dataLoaded = b;
                tryStartNextActivity();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
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