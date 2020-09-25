package pl.huczeq.rtspplayer.activities.main;

import androidx.appcompat.app.AppCompatActivity;

import pl.huczeq.rtspplayer.interfaces.OnDataChanged;
import pl.huczeq.rtspplayer.utils.data.Camera;

public abstract class AbstractBaseActivity extends AppCompatActivity {
    protected abstract void setViewsWidgets();
    protected abstract void onDataChangedWAA();
}
