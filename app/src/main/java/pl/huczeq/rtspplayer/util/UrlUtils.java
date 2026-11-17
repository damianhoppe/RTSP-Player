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

    public static Uri encodedUri(String url) {
        Uri uri = Uri.parse(url);

        String userinfo = uri.getUserInfo();

        if(userinfo == null)
            return uri;

        String username;
        String password = null;

        int lastIndexOf = userinfo.indexOf(":");

        if (lastIndexOf >= 0) {
            username = userinfo.substring(0, lastIndexOf);
            password = userinfo.substring(lastIndexOf + 1);
        } else {
            username = userinfo;
        }

        String authority = Uri.encode(username);
        if(password != null)
            authority += ":" + Uri.encode(password);

        authority += "@" + uri.getHost();

        if(uri.getPort() != -1)
            authority += ":" + uri.getPort();

        return uri.buildUpon()
                .encodedAuthority(authority)
                .build();
    }
}
