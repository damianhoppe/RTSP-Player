package pl.huczeq.rtspplayer.utils;

import android.content.Context;
import android.provider.Settings;

import java.util.regex.Pattern;

public class Utils {
    private static Pattern numericPattern = Pattern.compile("-?\\d+(\\.\\d+)?");

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        return numericPattern.matcher(strNum).matches();
    }

    public static boolean isSystemOrientationLocked(Context context) {
        try {
            return Settings.System.getInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION) != 1;
        } catch (Settings.SettingNotFoundException e) {
            return true;
        }
    }
}
