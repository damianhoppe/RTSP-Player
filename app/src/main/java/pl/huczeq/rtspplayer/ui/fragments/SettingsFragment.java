package pl.huczeq.rtspplayer.ui.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import pl.huczeq.rtspplayer.BuildConfig;
import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.ui.activities.settings.ExportDataActivity;
import pl.huczeq.rtspplayer.ui.activities.settings.ImportDataActivity;
import pl.huczeq.rtspplayer.ui.activities.settings.info.AppInfoActivity;
import pl.huczeq.rtspplayer.ui.activities.settings.info.LicenseActivity;
import pl.huczeq.rtspplayer.data.Settings;

public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "SettingsFragment";

    Preference importData, exportData, aboutApp, showLicense, openAddModelForm;
    ListPreference theme, adsMode, fullscreenAdsDelay;
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        importData = findPreference(Settings.KEY_IMPORT_DATA);
        exportData = findPreference(Settings.KEY_EXPORT_DATA);
        aboutApp = findPreference(Settings.KEY_ABOUT_APP);
        showLicense = findPreference(Settings.KEY_SHOW_LICENSE);
        openAddModelForm = findPreference(Settings.KEY_OPEN_ADD_MODE_FORM);

        if(importData != null) importData.setOnPreferenceClickListener(this);
        if(exportData != null) exportData.setOnPreferenceClickListener(this);
        if(aboutApp != null) aboutApp.setOnPreferenceClickListener(this);
        if(showLicense != null) showLicense.setOnPreferenceClickListener(this);
        if(openAddModelForm != null) openAddModelForm.setOnPreferenceClickListener(this);

        theme = findPreference(Settings.KEY_THEME);
        adsMode = findPreference(Settings.KEY_ADS_MODE);
        fullscreenAdsDelay = findPreference(Settings.KEY_FULLSCREAN_ADS_DELAY);

        PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        Intent intent = null;
        switch(preference.getKey()) {
            case Settings.KEY_IMPORT_DATA:
                intent = new Intent(getContext(), ImportDataActivity.class);
                break;
            case Settings.KEY_EXPORT_DATA:
                intent = new Intent(getContext(), ExportDataActivity.class);
                break;
            case Settings.KEY_ABOUT_APP:
                intent = new Intent(getContext(), AppInfoActivity.class);
                break;
            case Settings.KEY_SHOW_LICENSE:
                intent = new Intent(getContext(), LicenseActivity.class);
                break;
            case Settings.KEY_OPEN_ADD_MODE_FORM:
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
