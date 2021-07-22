package pl.huczeq.rtspplayer.ui.activities.base;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.StringRes;
import androidx.appcompat.widget.Toolbar;

import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.data.DataManager;
import pl.huczeq.rtspplayer.data.Settings;

public class BaseActivity extends AbstractBaseActivity {

    private final String TAG = "BaseActivity";

    protected Toolbar toolbar;
    private ImageView toolbarIcon;

    protected Settings settings;
    protected DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.settings = Settings.getInstance(getApplicationContext());
        this.dataManager = DataManager.getInstance(getApplicationContext());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void setViewsWidgets() {
        toolbar = findViewById(R.id.include);
        if(toolbar != null) {
            Log.d(TAG, "1");
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle(R.string.title_activity_main);
            toolbarIcon = findViewById(R.id.iconToolbar);
        }
    }

    public void enableToolbarIcon(Drawable icon, View.OnClickListener onClickListener) {
        toolbarIcon.setVisibility(View.VISIBLE);
        toolbarIcon.setImageDrawable(icon);
        if(onClickListener != null)
            toolbarIcon.setOnClickListener(onClickListener);
    }

    public void disableToolbarIcon() {
        toolbarIcon.setVisibility(View.INVISIBLE);
    }

    public void setToolbarTitle(@StringRes int str) {
        if(getSupportActionBar() != null) getSupportActionBar().setTitle(str);
    }
}