package pl.huczeq.rtspplayer.utils;

import java.util.regex.Pattern;

public class Utils {
    private static Pattern numericPattern = Pattern.compile("-?\\d+(\\.\\d+)?");

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        return numericPattern.matcher(strNum).matches();
    }
}
