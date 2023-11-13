package pl.huczeq.rtspplayer.data.sources.local.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import pl.huczeq.rtspplayer.data.model.CameraInstance;
import pl.huczeq.rtspplayer.data.model.CameraPattern;
import pl.huczeq.rtspplayer.data.typeconverters.MapConverter;

@Database(entities = {CameraInstance.class, CameraPattern.class}, version = 1, exportSchema = true)
@TypeConverters({MapConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    
    public abstract CameraDao cameraDao();
    public abstract CameraInstanceDao cameraInstanceDao();
    public abstract CameraPatternDao cameraPatternDao();
    public abstract CameraGroupDao cameraGroupDao();
}
