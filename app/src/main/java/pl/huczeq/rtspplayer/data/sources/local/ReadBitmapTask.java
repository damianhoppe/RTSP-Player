package pl.huczeq.rtspplayer.data.sources.local;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ReadBitmapTask {

    public static Bitmap load(File directory, String fileName) {
        File f = new File(directory, fileName);
        if(!f.exists()) {
            return null;
        }
        return loadBitmapFromFile(f);
    }

    private static Bitmap loadBitmapFromFile(File file) {
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        try {
            bitmap = BitmapFactory.decodeStream(new FileInputStream(file), null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}