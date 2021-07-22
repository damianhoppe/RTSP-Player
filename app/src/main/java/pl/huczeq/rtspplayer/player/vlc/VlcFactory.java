package pl.huczeq.rtspplayer.player.vlc;

import android.content.Context;

import org.videolan.libvlc.LibVLC;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import pl.huczeq.rtspplayer.Settings;

public class VlcFactory {

    public static ArrayList<String> buildArgumentList(Settings settings, String... params) {
        ArrayList<String> args = new ArrayList<>(Arrays.asList("--vout=android-display,none", "-vvv"));

        args.add("--avcodec-skiploopfilter");
        args.add("0");
        args.add("--avcodec-skip-frame");
        args.add("0");
        args.add("--avcodec-skip-idct");
        args.add("0");

        Collections.addAll(args, params);
        return args;
    }

    public static LibVLC buildLibVLC(Context context, Settings settings, String ...params) {
        ArrayList<String> args = buildArgumentList(settings, params);
        return new LibVLC(context, args);
    }
}
