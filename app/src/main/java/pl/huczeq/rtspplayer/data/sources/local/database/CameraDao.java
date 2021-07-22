package pl.huczeq.rtspplayer.data.sources.local.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import pl.huczeq.rtspplayer.data.model.Camera;

@Dao
public abstract class CameraDao {

    @Transaction
    @Query("SELECT * FROM camerainstance WHERE id = :id")
    public abstract LiveData<Camera> getCameraById(long id);

    @Transaction
    @Query("SELECT * FROM camerainstance WHERE id = :id")
    public abstract Camera getCameraByIdSync(long id);

    @Transaction
    @Query("SELECT * FROM camerainstance")
    public abstract LiveData<List<Camera>> getAllCameras2();

    @Transaction
    @Query("SELECT * FROM camerainstance ORDER BY patternId ASC, id ASC")
    public abstract LiveData<List<Camera>> getAllCameras();

    @Transaction
    @Query("SELECT * FROM camerainstance")
    public abstract List<Camera> getAllCamerasSync();
}
