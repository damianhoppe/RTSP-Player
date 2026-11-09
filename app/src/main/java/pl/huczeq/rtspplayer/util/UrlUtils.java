package pl.huczeq.rtspplayer.util;

public class UrlUtils {

    public static String maskCredentials(String url) {
        return url.replaceFirst("://[^/@]*@", "://****@");
    }
}
