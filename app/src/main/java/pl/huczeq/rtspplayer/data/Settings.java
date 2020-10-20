package pl.huczeq.rtspplayer.data;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import java.io.File;
import java.util.UUID;

import pl.huczeq.rtspplayer.BuildConfig;
import pl.huczeq.rtspplayer.ui.fragments.SettingsFragment;
import pl.huczeq.rtspplayer.utils.Utils;

public class Settings {

    private static Settings instance;

    public static synchronized Settings getInstance(Context context) {
        if(Settings.instance == null)
            Settings.instance = new Settings(context.getApplicationContext());
        return instance;
    }

    private Context context;
    private final String preferencesName = "Settings";
    public static final long adFullscreenDelay = 1000*60*60;

    private SharedPreferences appPref;
    private SharedPreferences.Editor appEditor;

    private SharedPreferences settingsPref;

    private final String KEY_FIRST_LAUNCH = "first_launch";

    private final String KEY_LAST_ADD_TIME = "last_add_time";

    public Settings(Context context) {
        this.context = context;
        this.appPref = this.context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
        this.settingsPref = PreferenceManager.getDefaultSharedPreferences(this.context);
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

    public long getLastAddTime() {
        return this.appPref.getLong(KEY_LAST_ADD_TIME, 0);
    }

    public void setLastAddTime(long time) {
        edit();
        this.appEditor.putLong(KEY_LAST_ADD_TIME, time);
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

    public String getTheme() {
        String theme = this.settingsPref.getString(KEY_THEME, "0");
        return theme;
    }

    public int getFullscreenAdsDelay() {
        String delay = this.settingsPref.getString(KEY_FULLSCREAN_ADS_DELAY, null);
        if(!Utils.isNumeric(delay)) return 1;
        return Integer.parseInt(delay);
    }

    public int getAdsMode() {
        String adsMode = this.settingsPref.getString(KEY_ADS_MODE, null);
        if(!Utils.isNumeric(adsMode)) return ADS_MODE_FULLSCREEN;
        return Integer.parseInt(adsMode);
    }

    //END OF SETTINGS PREFERENCES

    public File getPreviewImagesDir() {
        return this.context.getCacheDir();
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

    public static final int ADS_MODE_BAR = 0;
    public static final int ADS_MODE_FULLSCREEN = 1;
    public static final int ADS_MODE_BAR_AND_FULLSCREEN = 2;

    public static final String KEY_IMPORT_DATA = "importData";
    public static final String KEY_EXPORT_DATA = "exportData";
    public static final String KEY_ABOUT_APP = "appInformations";
    public static final String KEY_SHOW_LICENSE = "showLicense";
    public static final String KEY_THEME = "theme";
    public static final String KEY_ADS_MODE = "adsMode";
    public static final String KEY_FULLSCREAN_ADS_DELAY = "fullscreenAdsDelay";
    public static final String KEY_OPEN_ADD_MODE_FORM = "openAddModelForm";
}
