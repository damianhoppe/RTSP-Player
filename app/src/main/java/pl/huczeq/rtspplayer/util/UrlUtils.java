package pl.huczeq.rtspplayer.util;

import android.net.Uri;

public class UrlUtils {

    public static String maskCredentials(String url) {
        Uri uri = Uri.parse(url);

        String userinfo = uri.getUserInfo();

        if(userinfo == null)
            return url;

        String authority = "****@" + uri.getHost();

        if(uri.getPort() != -1) {
            authority += ":" + uri.getPort();
        }

        return uri.buildUpon().encodedAuthority(authority).build().toString();
    }
}
