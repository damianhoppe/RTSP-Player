package pl.huczeq.rtspplayer.ui.settings;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import com.google.android.material.color.DynamicColors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import pl.huczeq.rtspplayer.AppNavigator;
import pl.huczeq.rtspplayer.AppThemeHelper;
import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.Settings;
import pl.huczeq.rtspplayer.ui.selectcamera.SelectCameraActivity;
import pl.huczeq.rtspplayer.ui.views.materialpreferences.ListDialogFragmentMaterial;

@AndroidEntryPoint
public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "SettingsFragment";

    @Inject
    public AppNavigator navigator;

    @Inject
    public Settings settings;

    @Inject
    public AppThemeHelper themeHelper;

    Preference restoreBackup, createBackup, aboutApp, showLicense, openAddModelForm, dynamicColors;
    Preference vlcOptions, cachingBufferSize, hardwareAcceleration, forceUseRtspTcp, autoEnterInPipMode;
    SwitchPreference startingCamera;

    private ActivityResultLauncher<Intent> selectAppStartCameraResultLauncher;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        selectAppStartCameraResultLauncher = requireActivity().registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            long selectedCameraId = result.getData().getLongExtra(SelectCameraActivity.EXTRA_SELECTED_CAMERA_ID, -1);
                            settings.setAppStartCameraId((selectedCameraId <= 0)? -1 : (int) selectedCameraId);
                        }
                    }
                });

        setPreferencesFromResource(R.xml.preferences, "root");

        startingCamera = (SwitchPreference) findPreference(Settings.KEY.APP_START_CAMERA_ENABLED);
        vlcOptions = findPreference(Settings.KEY.CATEGORY_VLC_OPTIONS);
        cachingBufferSize = findPreference(Settings.KEY.CACHING_BUFFER_SIZE);
        hardwareAcceleration = findPreference(Settings.KEY.HARDWARE_ACCELERATION_ENABLED);
        forceUseRtspTcp = findPreference(Settings.KEY.FORCE_USE_RTSP_TCP_ENABLED);

        updateVlcOptionsState();

        dynamicColors = findPreference(Settings.KEY.DYNAMIC_COLORS);
        if(dynamicColors != null)
            dynamicColors.setEnabled(DynamicColors.isDynamicColorAvailable());

        autoEnterInPipMode = findPreference(Settings.KEY.AUTO_ENTER_PIP_MODE_ENABLED);
        if(autoEnterInPipMode != null) {
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N || !requireActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
                autoEnterInPipMode.setEnabled(false);
            }
        }

        createBackup = findPreference(Settings.ACTION.CREATE_BACKUP);
        restoreBackup = findPreference(Settings.ACTION.RESTORE_BACKUP);
        aboutApp = findPreference(Settings.ACTION.ABOUT_APP);
        showLicense = findPreference(Settings.ACTION.SHOW_LICENSE);
        openAddModelForm = findPreference(Settings.ACTION.NEW_CAMERA_MODEL_REQUEST_FORM);

        if(startingCamera != null) startingCamera.setOnPreferenceClickListener(this);
        if(restoreBackup != null) restoreBackup.setOnPreferenceClickListener(this);
        if(createBackup != null) createBackup.setOnPreferenceClickListener(this);
        if(aboutApp != null) aboutApp.setOnPreferenceClickListener(this);
        if(showLicense != null) showLicense.setOnPreferenceClickListener(this);
        if(openAddModelForm != null) openAddModelForm.setOnPreferenceClickListener(this);

        PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        PreferenceManager.getDefaultSharedPreferences(getContext()).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        switch(s) {
            case Settings.KEY.THEME:
                themeHelper.applyDarkLightTheme();
                break;
            case Settings.KEY.APP_START_CAMERA_ENABLED:
                startingCamera.setChecked(sharedPreferences.getBoolean(Settings.KEY.APP_START_CAMERA_ENABLED, false));
            case Settings.KEY.DYNAMIC_COLORS:
                themeHelper.updateActivitiesTheme();
            case Settings.KEY.PLAYBACK_LIBRARY:
                updateVlcOptionsState();
        }
    }

    @Override
    public boolean onPreferenceClick(androidx.preference.Preference preference) {
        switch(preference.getKey()) {
            case Settings.KEY.APP_START_CAMERA_ENABLED:
                startingCamera.setChecked(!startingCamera.isChecked());
                selectAppStartCameraResultLauncher.launch(navigator.buildSelectCameraActivityIntent(settings.getAppStartCameraId()));
                return false;
            case Settings.ACTION.CREATE_BACKUP:
                navigator.startCreateBackupActivity();
                break;
            case Settings.ACTION.RESTORE_BACKUP:
                navigator.startRestoreBackupActivity();
                break;
            case Settings.ACTION.ABOUT_APP:
                navigator.startAboutAppActivity();
                break;
            case Settings.ACTION.SHOW_LICENSE:
                navigator.startLicenseViewerActivity();
                break;
            case Settings.ACTION.NEW_CAMERA_MODEL_REQUEST_FORM:
                navigator.startNewCameraModelRequestForm();
                break;
        }
        return false;
    }

    @Override
    public void onDisplayPreferenceDialog(@NonNull Preference preference) {
        if(preference instanceof ListPreference) {
            ListDialogFragmentMaterial dialog = ListDialogFragmentMaterial.newInstance(preference.getKey());
            dialog.setTargetFragment(this, 0);
            dialog.show(getParentFragmentManager(), "ListDialogFragmentMaterial");
            return;
        }
        super.onDisplayPreferenceDialog(preference);
    }

    private void updateVlcOptionsState() {
        cachingBufferSize.setEnabled(!settings.useExoPlayer());
        hardwareAcceleration.setEnabled(!settings.useExoPlayer());
    }
}
