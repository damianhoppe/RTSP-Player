package pl.huczeq.rtspplayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.IntDef;
import androidx.annotation.MainThread;
import androidx.preference.PreferenceManager;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class Settings {

    public static void tryLoadPreferences(Settings settings, JsonObject jsonSettings) {
        Map<String, Class> settingsToImport = new HashMap<>();
        settingsToImport.put(Settings.KEY.APP_START_CAMERA_ENABLED, Boolean.class);
        settingsToImport.put(Settings.KEY.APP_START_CAMERA_ID, Long.class);
        settingsToImport.put(Settings.KEY.THEME, String.class);
        settingsToImport.put(Settings.KEY.DYNAMIC_COLORS, Boolean.class);
        settingsToImport.put(Settings.KEY.INITIAL_ORIENTATION, String.class);
        settingsToImport.put(Settings.KEY.ORIENTATION_MODE, String.class);
        settingsToImport.put(Settings.KEY.PLAYBACK_LIBRARY, String.class);
        settingsToImport.put(Settings.KEY.CACHING_BUFFER_SIZE, Integer.class);
        settingsToImport.put(Settings.KEY.HARDWARE_ACCELERATION_ENABLED, Boolean.class);
        settingsToImport.put(Settings.KEY.MUTE_AUDIO_DEFAULT_ENABLED, Boolean.class);
        settingsToImport.put(Settings.KEY.FORCE_USE_RTSP_TCP_ENABLED, Boolean.class);
        settingsToImport.put(Settings.KEY.AUTO_ENTER_PIP_MODE_ENABLED, Boolean.class);


        settings.editSettings();
        for(Map.Entry<String, Class> settingToImport : settingsToImport.entrySet()) {
            if(!jsonSettings.has(settingToImport.getKey()))
                continue;
            JsonElement jsonElement = jsonSettings.get(settingToImport.getKey());
            if(jsonElement.isJsonNull())
                continue;
            try {
                if (settingToImport.getValue() == Boolean.class)
                    settings.settingsEditor.putBoolean(settingToImport.getKey(), jsonElement.getAsBoolean());
                else if (settingToImport.getValue() == Integer.class)
                    settings.settingsEditor.putInt(settingToImport.getKey(), jsonElement.getAsInt());
                else if (settingToImport.getValue() == Long.class)
                    settings.settingsEditor.putLong(settingToImport.getKey(), jsonElement.getAsLong());
                else if (settingToImport.getValue() == String.class)
                    settings.settingsEditor.putString(settingToImport.getKey(), jsonElement.getAsString());
            }catch (Exception ignored){}
        }
        settings.commitSettings();
    }

    public static final class ACTION {
        public static final String NEW_CAMERA_MODEL_REQUEST_FORM = "actionNewCameraModelRequestForm";
        public static final String CREATE_BACKUP = "actionCreateBackup";
        public static final String RESTORE_BACKUP = "actionRestoreBackup";
        public static final String ABOUT_APP = "actionAboutApp";
        public static final String SHOW_LICENSE = "actionShowLicense";
    }

    public static final class KEY {
        public static final String CATEGORY_VLC_OPTIONS = "categoryVlcOptions";
        public static final String APP_START_CAMERA_ENABLED = "appStartCameraEnabled";
        public static final String APP_START_CAMERA_ID = "appStartCameraId";
        public static final String THEME = "theme";
        public static final String DYNAMIC_COLORS = "useDynamicColors";
        public static final String INITIAL_ORIENTATION = "initialOrientation";
        public static final String ORIENTATION_MODE = "orientationMode";
        public static final String GENERATION_CAMERA_THUMBNAIL_ONCE_ENABLED = "generationCameraThumbnailOnce";
        public static final String PLAYBACK_LIBRARY = "playbackLibrary";
        public static final String CACHING_BUFFER_SIZE = "cachingBufferSize";
        public static final String HARDWARE_ACCELERATION_ENABLED = "hardwareAccelerationEnabled";
        public static final String MUTE_AUDIO_DEFAULT_ENABLED = "muteAutoDefaultEnabled";
        public static final String FORCE_USE_RTSP_TCP_ENABLED = "forceUseRtspTcpEnabled";
        public static final String AUTO_ENTER_PIP_MODE_ENABLED = "autoEnterPipModeEnabled";
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Theme.LIGHT, Theme.DARK, Theme.FOLLOW_SYSTEM})
    public @interface Theme {
        int FOLLOW_SYSTEM = 0;
        int LIGHT = 1;
        int DARK = 2;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({InitialOrientation.AUTOMATIC, InitialOrientation.HORIZONTAL})
    public @interface InitialOrientation {
        int AUTOMATIC = 0;
        int HORIZONTAL = 1;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({OrientationMode.FOLLOW_SYSTEM, OrientationMode.FOLLOW_SENSOR, OrientationMode.LOCKED})
    public @interface OrientationMode {
        int FOLLOW_SYSTEM = 0;
        int FOLLOW_SENSOR = 1;
        int LOCKED = 2;
    }

    private Context appContext;

    private SharedPreferences settings;
    private SharedPreferences.Editor settingsEditor;

    @Inject
    public Settings(@ApplicationContext Context context) {
        this.appContext = context.getApplicationContext();
        this.settings = PreferenceManager.getDefaultSharedPreferences(this.appContext);
    }

    public void verifyDefaultSettings() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N || !appContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
            setAutoEnterPipModeEnabled(false);
        }
    }

    public @Theme int getTheme() {
        switch(this.settings.getString(KEY.THEME, "0")) {
            case "1":
                return Theme.LIGHT;
            case "2":
                return Theme.DARK;
            default:
                return Theme.FOLLOW_SYSTEM;
        }
    }

    public boolean dynamicColorsEnabled() {
        return settings.getBoolean(KEY.DYNAMIC_COLORS, true);
    }

    public String getPlaybackLibrary() {
        return settings.getString(KEY.PLAYBACK_LIBRARY,"0");
    }

    public boolean useExoPlayer() {
        return Objects.equals(getPlaybackLibrary(), "1");
    }

    public @InitialOrientation int getInitialOrientation() {
        if(Objects.equals(this.settings.getString(KEY.INITIAL_ORIENTATION, "0"), "0"))
            return InitialOrientation.AUTOMATIC;
        else
            return InitialOrientation.HORIZONTAL;
    }

    public @OrientationMode int getOrientationMode() {
        switch(this.settings.getString(KEY.ORIENTATION_MODE, "0")) {
            case "1":
                return OrientationMode.FOLLOW_SENSOR;
            case "2":
                return OrientationMode.LOCKED;
            default:
                return OrientationMode.FOLLOW_SYSTEM;
        }
    }

    public boolean isMuteAudioDefaultEnabled() {
        return this.settings.getBoolean(KEY.MUTE_AUDIO_DEFAULT_ENABLED, false);
    }

    public boolean isForceUseRtspTcpEnabled() {
        return this.settings.getBoolean(KEY.FORCE_USE_RTSP_TCP_ENABLED, false);
    }

    public boolean autoEnterPipModeEnabled() {
        return settings.getBoolean(KEY.AUTO_ENTER_PIP_MODE_ENABLED, true);
    }

    public void setAutoEnterPipModeEnabled(boolean enabled) {
        editSettings();
        settingsEditor.putBoolean(KEY.AUTO_ENTER_PIP_MODE_ENABLED, enabled);
        commitSettings();
    }

    private void editSettings() {
        this.settingsEditor = this.settings.edit();
    }

    private void commitSettings() {
        this.settingsEditor.commit();
    }

    public long getAppStartCameraId() {
        return this.settings.getLong(KEY.APP_START_CAMERA_ID, -1);
    }

    @MainThread
    public void setAppStartCameraId(long cameraId) {
        editSettings();
        this.settingsEditor.putLong(KEY.APP_START_CAMERA_ID, cameraId);
        this.settingsEditor.putBoolean(KEY.APP_START_CAMERA_ENABLED, cameraId >= 0);
        commitSettings();
    }

    public Boolean isAppStartCameraEnabled() {
        return this.settings.getBoolean(KEY.APP_START_CAMERA_ENABLED, false);
    }

    public int getCachingBufferSize() {
        return this.settings.getInt(KEY.CACHING_BUFFER_SIZE, 500);
    }

    public boolean isEnabledHardwareAcceleration() {
        return this.settings.getBoolean(KEY.HARDWARE_ACCELERATION_ENABLED, false);
    }
}
