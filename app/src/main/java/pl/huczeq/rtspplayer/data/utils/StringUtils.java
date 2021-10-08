package pl.huczeq.rtspplayer.data.utils;

public class StringUtils {

    public static int countChar(String text, char c) {
        int count = 0;
        for(int i = 0; i < text.length(); i++) {
            if(c == text.charAt(i))
                count++;
        }
        return count;
    }
}
