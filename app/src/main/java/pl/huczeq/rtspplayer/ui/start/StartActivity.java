package pl.huczeq.rtspplayer.ui.start;

import android.content.Intent;
import android.os.Bundle;

import androidx.core.splashscreen.SplashScreen;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import pl.huczeq.rtspplayer.AppNavigator;
import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.Settings;
import pl.huczeq.rtspplayer.data.model.Camera;
import pl.huczeq.rtspplayer.ui.BaseActivity;

@AndroidEntryPoint
public class StartActivity extends BaseActivity {

    @Inject
    public AppNavigator navigator;

    public StartViewModel viewModel;

    @Inject
    public Settings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        this.viewModel = new ViewModelProvider(this).get(StartViewModel.class);

        splashScreen.setKeepOnScreenCondition(new SplashScreen.KeepOnScreenCondition() {
            @Override
            public boolean shouldKeepOnScreen() {
                return !Boolean.TRUE.equals(viewModel.getAppStarted().getValue());
            }
        });

        viewModel.getAppStarted().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean ready) {
                if(ready != null && ready)
                    startNextActivity(viewModel.getAppStartCamera());
            }
        });
    }

    private void startNextActivity(Camera camera) {
        if(camera != null)
            startActivities(new Intent[]{navigator.buildMainActivityIntent(), navigator.buildPlayerCameraActivityIntent(camera)});
        else
            navigator.startMainActivity();
        finish();
    }
}