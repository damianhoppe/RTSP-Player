package pl.huczeq.rtspplayer.data.database;

import androidx.room.RoomDatabase;

import pl.huczeq.rtspplayer.data.objects.CameraInstance;
import pl.huczeq.rtspplayer.data.objects.CameraPattern;

@androidx.room.Database(entities = {CameraInstance.class, CameraPattern.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract CameraDao cameraDao();
}
