package pl.huczeq.rtspplayer.ui;

import android.graphics.drawable.Drawable;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.CallSuper;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import dagger.hilt.internal.Preconditions;
import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.Settings;

@AndroidEntryPoint
public abstract class BaseActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageView toolbarIcon;

    @Inject
    protected Settings settings;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        initToolbar();
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        initToolbar();
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        initToolbar();
    }

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        if(toolbar != null)
            initToolbar(toolbar);
    }

    @CallSuper
    protected void initToolbar(Toolbar toolbar) {
        Preconditions.checkNotNull(toolbar);
        setSupportActionBar(toolbar);
        if(getTitle() == null)
            getSupportActionBar().setTitle(R.string.app_name);
        else
            getSupportActionBar().setTitle(getTitle());
        toolbarIcon = toolbar.findViewById(R.id.iconToolbar);
    }

    public void enableToolbarIcon(Drawable icon, View.OnClickListener onClickListener) {
        Preconditions.checkNotNull(icon);
        Preconditions.checkNotNull(toolbarIcon);
        toolbarIcon.setVisibility(View.VISIBLE);
        toolbarIcon.setImageDrawable(icon);
        if(onClickListener != null)
            toolbarIcon.setOnClickListener(onClickListener);
    }

    public void showBackToolbarIcon() {
        if(getSupportActionBar() == null)
            return;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}