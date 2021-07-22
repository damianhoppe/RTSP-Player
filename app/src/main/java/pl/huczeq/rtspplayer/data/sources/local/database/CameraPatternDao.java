package pl.huczeq.rtspplayer.data.sources.local.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import pl.huczeq.rtspplayer.data.model.CameraPattern;

@Dao
public abstract class CameraPatternDao {

    @Query("SELECT * FROM camerapattern WHERE id = :id")
    public abstract LiveData<CameraPattern> getCameraPatternByIdSync(long id);

    @Query("SELECT * FROM camerapattern")
    public abstract List<CameraPattern> getAllCameraPatternsSync();

    @Insert
    public abstract long insertCameraPattern(CameraPattern cameraPattern);

    @Update
    public abstract void updateCameraPattern(CameraPattern cameraPattern);

    @Query("DELETE FROM camerapattern WHERE id = :id")
    public abstract void deleteCameraPattern(long id);
}
