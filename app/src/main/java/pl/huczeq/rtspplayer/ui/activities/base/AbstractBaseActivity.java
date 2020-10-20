package pl.huczeq.rtspplayer.ui.activities.base;

import androidx.appcompat.app.AppCompatActivity;

public abstract class AbstractBaseActivity extends AppCompatActivity {
    protected abstract void setViewsWidgets();
    protected abstract void onDataChangedWAA();
}
