package pl.huczeq.rtspplayer.data.sources.local;

import android.graphics.Bitmap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SaveBitmapTask {

    public static void save(Bitmap bitmap, File directory, String fileName) throws IOException {
        File f = new File(directory, fileName);
        FileOutputStream fouts = new FileOutputStream(f);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fouts);
        fouts.flush();
        fouts.close();
    }
}
