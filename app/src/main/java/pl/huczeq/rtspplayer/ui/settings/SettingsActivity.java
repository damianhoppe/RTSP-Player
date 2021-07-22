package pl.huczeq.rtspplayer.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import dagger.hilt.android.AndroidEntryPoint;
import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.ui.BaseActivity;

@AndroidEntryPoint
public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        showBackToolbarIcon();
//
//        Bundle bundle = new Bundle();
//        bundle.putString(SettingsFragment.ARG_PREFERENCE_ROOT, "root");
//
//        SettingsFragment settingsFragment = new SettingsFragment();
//        settingsFragment.setArguments(bundle);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new SettingsFragment())
                .commit();
    }
}