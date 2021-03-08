package pl.huczeq.rtspplayer.ui.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SeekBarPreference;

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
    ListPreference theme, orientationMode, defaultOrientation;
    CheckBoxPreference useNewPlayer, useHardwareAcceleration, useAVCodesFast;
    SeekBarPreference cachingBufferSize;

    SharedPreferences.OnSharedPreferenceChangeListener onNewSettingsLoaded = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String s) {
            Log.d(TAG, "onSharedPreferenceChanged1:" + s);
            switch(s) {
                case Settings.KEY_THEME:
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            theme.setValue(sharedPreferences.getString(Settings.KEY_THEME, "1"));
                        }
                    });
                    break;
                case Settings.KEY_DEFAULT_ORIENTATION:
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            defaultOrientation.setValue(sharedPreferences.getString(Settings.KEY_DEFAULT_ORIENTATION, "0"));
                        }
                    });
                    break;
                case Settings.KEY_ORIENTATION_MODE:
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            orientationMode.setValue(sharedPreferences.getString(Settings.KEY_ORIENTATION_MODE, "0"));
                        }
                    });
                    break;
                case Settings.KEY_USE_NEW_PLAYER:
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            useNewPlayer.setChecked(sharedPreferences.getBoolean(Settings.KEY_USE_NEW_PLAYER, true));
                        }
                    });
                    break;
                case Settings.KEY_HARDWARE_ACCELERATION:
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            useHardwareAcceleration.setChecked(sharedPreferences.getBoolean(Settings.KEY_HARDWARE_ACCELERATION, true));
                        }
                    });
                    break;
                case Settings.KEY_AVCODES_FAST:
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            useAVCodesFast.setChecked(sharedPreferences.getBoolean(Settings.KEY_AVCODES_FAST, false));
                        }
                    });
                    break;
                case Settings.KEY_CACHING_BUFFER_SIZE:
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            cachingBufferSize.setValue(sharedPreferences.getInt(Settings.KEY_CACHING_BUFFER_SIZE, 200));
                        }
                    });
                    break;
                default:
            }
        }
    };

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        restoreBackup = findPreference(Settings.KEY_RESTORE_BACKUP);
        createBackup = findPreference(Settings.KEY_CREATE_BACKUP);
        aboutApp = findPreference(Settings.KEY_ABOUT_APP);
        showLicense = findPreference(Settings.KEY_SHOW_LICENSE);
        openAddModelForm = findPreference(Settings.KEY_OPEN_ADD_MODEL_FORM);

        useNewPlayer = findPreference(Settings.KEY_USE_NEW_PLAYER);
        useHardwareAcceleration = findPreference(Settings.KEY_HARDWARE_ACCELERATION);
        useAVCodesFast = findPreference(Settings.KEY_AVCODES_FAST);
        cachingBufferSize = findPreference(Settings.KEY_CACHING_BUFFER_SIZE);

        if(restoreBackup != null) restoreBackup.setOnPreferenceClickListener(this);
        if(createBackup != null) createBackup.setOnPreferenceClickListener(this);
        if(aboutApp != null) aboutApp.setOnPreferenceClickListener(this);
        if(showLicense != null) showLicense.setOnPreferenceClickListener(this);
        if(openAddModelForm != null) openAddModelForm.setOnPreferenceClickListener(this);

        theme = findPreference(Settings.KEY_THEME);
        defaultOrientation = findPreference(Settings.KEY_DEFAULT_ORIENTATION);
        orientationMode = findPreference(Settings.KEY_ORIENTATION_MODE);

        PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(this);
        Settings.getInstance(getContext()).setListener(this.onNewSettingsLoaded);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        PreferenceManager.getDefaultSharedPreferences(getContext()).unregisterOnSharedPreferenceChangeListener(this);
        Settings.getInstance(getContext()).setListener(null);
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
                Settings.getInstance(getContext()).setTheme();
                break;
        }
    }
}
