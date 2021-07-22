package pl.huczeq.rtspplayer.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import java.io.File;
import java.util.UUID;

import pl.huczeq.rtspplayer.BuildConfig;

public class Settings {

    private final static String TAG = "Settings";

    private static Settings instance;

    public static synchronized Settings getInstance(Context context) {
        if(Settings.instance == null)
            Settings.instance = new Settings(context.getApplicationContext());
        return instance;
    }

    private Context appContext;
    private final String preferencesName = "Settings";
    public static final long adFullscreenDelay = 1000*60*60;
    public static final long CLICK_MIN_DELTA_TIME = 150;

    private SharedPreferences appPref;
    private SharedPreferences.Editor appEditor;

    private SharedPreferences settingsPref;
    private SharedPreferences.Editor settingsEditor;

    private final String KEY_FIRST_LAUNCH = "first_launch";

    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    public Settings(Context context) {
        this.appContext = context.getApplicationContext();
        this.appPref = this.appContext.getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
        this.settingsPref = PreferenceManager.getDefaultSharedPreferences(this.appContext);
    }

    public Context getAppContext() {
        return this.appContext;
    }

    public void setListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        this.listener = listener;
    }

    public void callListener(String key) {
        if(this.listener == null) return;
        this.listener.onSharedPreferenceChanged(this.settingsPref, key);
    }

    // APP PREFERENCES

    private void edit() {
        this.appEditor = this.appPref.edit();
    }
    private void commit() {
        this.appEditor.commit();
    }

    public boolean isFirstLaunch() {
        return this.appPref.getBoolean(KEY_FIRST_LAUNCH, true);
    }

    public void setFirstLaunch(boolean firstLaunch) {
        edit();
        this.appEditor.putBoolean(KEY_FIRST_LAUNCH, firstLaunch);
        commit();
    }

    //END OF APP PREFERENCES

    public static String getFullVersion() {
        return BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ") - " + BuildConfig.BUILD_TIME;
    }

    public String getUniquePreviewImageName() {
        File file;
        String name;
        do {
            name = UUID.randomUUID().toString() + ".png";
            file = new File(getPreviewImagesDir(), name);
        } while(file.exists());
        return name;
    }

    //SETTINGS PREFERENCES

    private void editSettings() {
        this.settingsEditor = this.settingsPref.edit();
    }
    private void commitSettings() {
        this.settingsEditor.commit();
    }

    public String getTheme() {
        String theme = this.settingsPref.getString(KEY_THEME, "0");
        return theme;
    }

    public void setTheme() {
        switch(getTheme()) {
            case "1":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "2":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }

    public String getDefaultOrientationValue() {
        return this.settingsPref.getString(KEY_DEFAULT_ORIENTATION, "0");
    }

    public String getOrientationModeValue() {
        return this.settingsPref.getString(KEY_ORIENTATION_MODE,"0");
    }

    public ORIENTATION getDefaultOrientation() {
        switch(getDefaultOrientationValue()) {
            case "0":
                return ORIENTATION.AUTOMATIC;
            default:
                return ORIENTATION.HORIZONTAL;
        }
    }

    public ORIENTATION_MODE getOrientationMode() {
        switch(getOrientationModeValue()) {
            case "1":
                return ORIENTATION_MODE.AUTO_SYS;
            case "2":
                return ORIENTATION_MODE.LOCKED;
            default:
                return ORIENTATION_MODE.AUTO_SENSOR;
        }
    }

    public int getCachingBufferSize() {
        return this.settingsPref.getInt(KEY_CACHING_BUFFER_SIZE, 100);
    }

    public void setCachinBufferSize(int bufferSize) {
        if(bufferSize < 10) bufferSize = 10;
        if(bufferSize > 5000) bufferSize = 5000;
        editSettings();
        this.settingsEditor.putInt(KEY_CACHING_BUFFER_SIZE, bufferSize);
        commitSettings();
    }

    public boolean isEnabledHardwareAcceleration() {
        return this.settingsPref.getBoolean(KEY_HARDWARE_ACCELERATION, false);
    }

    public void setEnabledHardwareAcceleration(boolean enabled) {
        editSettings();
        this.settingsEditor.putBoolean(KEY_HARDWARE_ACCELERATION, enabled);
        commitSettings();
    }

    public boolean isEnabledAVCodes() {
        return this.settingsPref.getBoolean(KEY_AVCODES_FAST, false);
    }

    public PLAYER_SURFACE getPlayerSurfaceEnum() {
        switch(getPlayerSurface()) {
            case "0":
                return PLAYER_SURFACE.SURFACEVIEW;
            case "1":
                return PLAYER_SURFACE.GLTEXTUREVIEW;
            case "2":
                return PLAYER_SURFACE.TEXTUREVIEW;
            default:
                return PLAYER_SURFACE.SURFACEVIEW;
        }
    }

    public String getPlayerSurface() {
        return this.settingsPref.getString(KEY_PLAYER_SURFACE, "0");
    }

    public boolean isEnabledGenerateCameraTumbnailOnce() {
        return this.settingsPref.getBoolean(KEY_GENERATE_CAMERA_TUMBNAIL_ONCE,true);
    }
    //END OF SETTINGS PREFERENCES

    public File getPreviewImagesDir() {
        return this.appContext.getCacheDir();
        //return this.context.getExternalCacheDir();
    }
    public File getBackupsDir() {
        File dir;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            dir = new File(appContext.getExternalFilesDir(null), "Backups");
        }else {
            String path = appContext.getExternalFilesDir(null).getAbsolutePath();
            if(path.contains(File.separator + "Android")) {
                path = path.split(File.separator + "Android")[0];
            }
            dir = new File(path, "RTSP Player" + File.separator + "Backups");
        }
        return dir;
    }

    public SharedPreferences getAppPref() {
        return this.appPref;
    }

    public SharedPreferences getSettingsPref() {
        return this.appPref;
    }

    public static final String KEY_RESTORE_BACKUP = "restoreBackup";
    public static final String KEY_CREATE_BACKUP = "createBackup";
    public static final String KEY_ABOUT_APP = "appInformations";
    public static final String KEY_SHOW_LICENSE = "showLicense";
    public static final String KEY_THEME = "theme";
    public static final String KEY_DEFAULT_ORIENTATION = "defaultOrientation";
    public static final String KEY_GENERATE_CAMERA_TUMBNAIL_ONCE = "generateCameratTumbnailOnce";
    public static final String KEY_ORIENTATION_MODE = "orientationMode";
    public static final String KEY_OPEN_ADD_MODEL_FORM = "openAddModelForm";
    public static final String KEY_PLAYER_SURFACE = "playerSurface";
    public static final String KEY_CACHING_BUFFER_SIZE = "cachingBufferSize";
    public static final String KEY_HARDWARE_ACCELERATION = "hardwareAcceleration";
    public static final String KEY_AVCODES_FAST = "avcodesFast";

    public enum ORIENTATION_MODE {
        AUTO_SYS, AUTO_SENSOR, LOCKED;
    }
    public enum ORIENTATION {
        AUTOMATIC, HORIZONTAL;
    }
    public enum PLAYER_SURFACE {
        SURFACEVIEW, TEXTUREVIEW, GLTEXTUREVIEW;
    }
}
