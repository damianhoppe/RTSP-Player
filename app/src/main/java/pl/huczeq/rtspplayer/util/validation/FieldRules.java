package pl.huczeq.rtspplayer.util.validation;

import android.os.Build;
import android.util.Patterns;

import com.google.common.base.Strings;
import com.google.common.net.InetAddresses;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

import pl.huczeq.rtspplayer.util.validation.interfaces.FieldRule;

public class FieldRules {

    static final Pattern numericPattern = Pattern.compile("-?\\d+(\\.\\d+)?");

    public static FieldRule NUMERIC() {
        return new FieldRule() {
            @Override
            public Integer checkValidity(String text) {
                if(Strings.isNullOrEmpty(text))
                    return Errors.INCORRECT_VALUE;
                if(!isNumeric(text))
                    return Errors.INCORRECT_VALUE;
                return null;
            }

            public boolean isNumeric(@NotNull String strNum) {
                return numericPattern.matcher(strNum).matches();
            }
        };
    }

    public static FieldRule REQUIRED() {
        return new FieldRule() {
            @Override
            public Integer checkValidity(String text) {
                if(text == null || text.isEmpty())
                    return Errors.IS_REQUIRED;
                return null;
            }
        };
    }

    public static FieldRule ADDRESS_IP() {
        return new FieldRule() {
            @Override
            public Integer checkValidity(String text) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if(!InetAddresses.isInetAddress(text) && !Patterns.DOMAIN_NAME.matcher(text).matches())
                        return Errors.INCORRECT_VALUE;
                }
                return null;
            }
        };
    }

    public static FieldRule PORT() {
        return new FieldRule() {
            @Override
            public Integer checkValidity(String text) {
                try {
                    int port = Integer.parseInt(text);
                    if(port < 0 || port > 65536)
                        return Errors.INCORRECT_VALUE;
                }catch (Exception e) {
                    return Errors.INCORRECT_VALUE;
                }
                return null;
            }
        };
    }
}
