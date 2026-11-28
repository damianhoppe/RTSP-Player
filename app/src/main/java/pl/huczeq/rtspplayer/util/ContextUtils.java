package pl.huczeq.rtspplayer.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;

public class ContextUtils {

    public static boolean hasLocalNetworkPermission(Context context) {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.CINNAMON_BUN)
            return true;
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_LOCAL_NETWORK) == PackageManager.PERMISSION_GRANTED;
    }
}
