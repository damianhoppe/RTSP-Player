package pl.huczeq.rtspplayer.ui.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import pl.huczeq.rtspplayer.BuildConfig;
import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.ui.activities.settings.CreateBackupActivity;
import pl.huczeq.rtspplayer.ui.activities.settings.RestoreBackupActivity;
import pl.huczeq.rtspplayer.ui.activities.settings.info.AppInfoActivity;
import pl.huczeq.rtspplayer.ui.activities.settings.info.LicenseActivity;
import pl.huczeq.rtspplayer.data.Settings;

public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "SettingsFragment";

    Preference restoreBackup, createBackup, aboutApp, showLicense, openAddModelForm;
    ListPreference theme;
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        restoreBackup = findPreference(Settings.KEY_RESTORE_BACKUP);
        createBackup = findPreference(Settings.KEY_CREATE_BACKUP);
        aboutApp = findPreference(Settings.KEY_ABOUT_APP);
        showLicense = findPreference(Settings.KEY_SHOW_LICENSE);
        openAddModelForm = findPreference(Settings.KEY_OPEN_ADD_MODEL_FORM);

        if(restoreBackup != null) restoreBackup.setOnPreferenceClickListener(this);
        if(createBackup != null) createBackup.setOnPreferenceClickListener(this);
        if(aboutApp != null) aboutApp.setOnPreferenceClickListener(this);
        if(showLicense != null) showLicense.setOnPreferenceClickListener(this);
        if(openAddModelForm != null) openAddModelForm.setOnPreferenceClickListener(this);

        theme = findPreference(Settings.KEY_THEME);

        PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        PreferenceManager.getDefaultSharedPreferences(getContext()).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        Intent intent = null;
        switch(preference.getKey()) {
            case Settings.KEY_RESTORE_BACKUP:
                intent = new Intent(getContext(), RestoreBackupActivity.class);
                break;
            case Settings.KEY_CREATE_BACKUP:
                intent = new Intent(getContext(), CreateBackupActivity.class);
                break;
            case Settings.KEY_ABOUT_APP:
                intent = new Intent(getContext(), AppInfoActivity.class);
                break;
            case Settings.KEY_SHOW_LICENSE:
                intent = new Intent(getContext(), LicenseActivity.class);
                break;
            case Settings.KEY_OPEN_ADD_MODEL_FORM:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.URL_ADD_MODEL_FORM));
                break;
        }
        if(intent != null) startActivity(intent);
        return false;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        switch(s) {
            case Settings.KEY_THEME:
                Log.d(TAG, s + " : " + sharedPreferences.getString(s, "-"));
                Settings.getInstance(getContext()).setTheme();
                break;
        }
    }
}
