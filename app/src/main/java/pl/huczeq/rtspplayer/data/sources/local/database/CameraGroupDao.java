package pl.huczeq.rtspplayer.data.sources.local.database;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import pl.huczeq.rtspplayer.data.model.CameraGroup;

@Dao
public abstract class CameraGroupDao {

    @Transaction
    @Query("SELECT * FROM camerapattern")
    public abstract List<CameraGroup> getAll();
}
