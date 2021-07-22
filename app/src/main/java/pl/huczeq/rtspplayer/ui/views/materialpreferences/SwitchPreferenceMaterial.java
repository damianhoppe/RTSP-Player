package pl.huczeq.rtspplayer.ui.views.materialpreferences;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.preference.PreferenceViewHolder;
import androidx.preference.SwitchPreference;

import pl.huczeq.rtspplayer.R;

public class SwitchPreferenceMaterial extends SwitchPreference {

    public SwitchPreferenceMaterial(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public SwitchPreferenceMaterial(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public SwitchPreferenceMaterial(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SwitchPreferenceMaterial(@NonNull Context context) {
        super(context);
        init();
    }

    private void init() {
        setWidgetLayoutResource(R.layout.pref_m3_switch);
    }

    @SuppressLint("RestrictedApi")
    @Override
    protected void performClick(@NonNull View view) {
        super.performClick(view);
        syncSwitch(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        syncSwitch(holder.itemView);
    }

    private void syncSwitch(View view) {
        SwitchCompat switchView = view.findViewById(R.id.switchWidget);
        if(switchView == null)
            return;
        switchView.setChecked(mChecked);
    }
}
