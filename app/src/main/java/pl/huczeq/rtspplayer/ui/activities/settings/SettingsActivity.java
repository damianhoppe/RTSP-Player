package pl.huczeq.rtspplayer.ui.activities.settings;

import android.os.Bundle;

import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.ui.activities.base.BaseActivity;
import pl.huczeq.rtspplayer.ui.fragments.SettingsFragment;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setViewsWidgets();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new SettingsFragment())
                .commit();
    }

    @Override
    protected void setViewsWidgets() {
        super.setViewsWidgets();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_activity_settings);
        }
    }
}
