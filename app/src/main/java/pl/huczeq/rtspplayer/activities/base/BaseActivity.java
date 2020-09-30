package pl.huczeq.rtspplayer.activities.base;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.widget.Toolbar;

import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.interfaces.OnDataChanged;
import pl.huczeq.rtspplayer.utils.Settings;
import pl.huczeq.rtspplayer.utils.data.DataManager;

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
        dataManager = DataManager.getInstance(this);
        settings = Settings.getInstance(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataManager.removeOnDataChangeListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.active = true;
        if(this.dataChanged) onDataChangedWAA();
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.active = false;
    }

    @Override
    protected void setViewsWidgets() {
        Log.d(TAG, "0");
        View v = findViewById(R.id.include);
        if(v == null) return;
        Log.d(TAG, "0.5");
        toolbar = findViewById(R.id.include);
        if(toolbar != null) {
            Log.d(TAG, "1");
            setSupportActionBar(toolbar);
            toolbar.setTitle(R.string.app_name);
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
}