package pl.huczeq.rtspplayer.ui.activities.base;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.StringRes;
import androidx.appcompat.widget.Toolbar;

import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.interfaces.OnDataChanged;
import pl.huczeq.rtspplayer.data.Settings;
import pl.huczeq.rtspplayer.data.DataManager;

public class BaseActivity extends AbstractBaseActivity implements OnDataChanged {

    private final String TAG = "BaseActivity";
    private boolean dataChanged = false;
    private boolean active = false;

    protected Toolbar toolbar;
    private ImageView toolbarIcon;

    protected DataManager dataManager;
    protected Settings settings;


    public BaseActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataManager = DataManager.getInstance(this, new DataManager.Callback() {
            @Override
            public void onDataLoaded() {
                onDataChanged();
            }
        });
        settings = Settings.getInstance(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataManager.removeOnDataChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.active = true;
        if(this.dataChanged) onDataChangedWAA();
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.active = false;
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

    /**
     *called when data is changed and activity is active
     */
    protected void onDataChangedWAA() {
        this.dataChanged = false;
    }

    @Override
    public void onDataChanged() {
        if(this.active) onDataChangedWAA(); else this.dataChanged = true;
    }

    public void enableOnDataChangeListener() {
        dataManager.addOnDataChangedListener(this);
    }

    public void enableToolbarIcon(Drawable icon, View.OnClickListener onClickListener) {
        Log.d(TAG, "2");
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