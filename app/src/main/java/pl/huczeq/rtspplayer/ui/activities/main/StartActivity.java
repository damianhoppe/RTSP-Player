package pl.huczeq.rtspplayer.ui.activities.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.util.List;

import pl.huczeq.rtspplayer.data.DataManager;
import pl.huczeq.rtspplayer.data.Settings;
import pl.huczeq.rtspplayer.data.objects.Camera;
import pl.huczeq.rtspplayer.ui.activities.MainActivity;
import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.ui.activities.base.BaseActivity;
import pl.huczeq.rtspplayer.ui.activities.camerapreviews.BasePreviewCameraActivity;
import pl.huczeq.rtspplayer.viewmodels.StartActivityViewModel;
import pl.huczeq.rtspplayer.viewmodels.factories.DataManagerViewModelFactory;

import static pl.huczeq.rtspplayer.data.Settings.KEY_STARTING_CAMERA_ID;

public class StartActivity extends BaseActivity {

    final String TAG = "StartActivity";
    boolean activityReady = false;
    boolean dataLoaded = false;
    boolean nextActivityStarted = false;

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
            if(nextActivityStarted)
                return;
            Log.d(TAG, "OK");
            int startingCameraId = Settings.getInstance(this).getStartingCameraId();
            if(startingCameraId < 0) {
                startMainActivity();
            }else {
                LiveData<List<Camera>> cameraList = dataManager.getCameraById(startingCameraId);
                cameraList.observe(this, new Observer<List<Camera>>() {
                    @Override
                    public void onChanged(List<Camera> cameras) {
                        if(cameras == null)
                            return;
                        cameraList.removeObserver(this);
                        if(cameras.isEmpty()) {
                            Settings.getInstance(StartActivity.this).setStartingCameraId(-1);
                            startMainActivity();
                        }else {
                            startPreviewActivity(cameras.get(0));
                        }
                    }
                });

            }
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra(KEY_STARTING_CAMERA_ID, Settings.getInstance(this).getStartingCameraId());
        startActivity(intent);
        nextActivityStarted = true;
    }

    private void startPreviewActivity(Camera camera) {
        Intent intent = BasePreviewCameraActivity.getStartIntent(getApplicationContext(), camera, true);
        startActivity(intent);
        nextActivityStarted = true;
    }
}