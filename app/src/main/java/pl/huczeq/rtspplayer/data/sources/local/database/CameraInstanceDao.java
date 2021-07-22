package pl.huczeq.rtspplayer.data.sources.local.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.google.common.base.Optional;

import java.util.List;

import pl.huczeq.rtspplayer.data.model.CameraInstance;

@Dao
public abstract class CameraInstanceDao {

    @Query("SELECT * FROM camerainstance WHERE id = :id")
    public abstract CameraInstance getCameraInstanceByIdSync(long id);

    @Query("SELECT * FROM camerainstance WHERE id = :id")
    public abstract LiveData<Optional<CameraInstance>> getCameraInstanceById(long id);

    @Query("SELECT * FROM camerainstance WHERE patternId == :patternId")
    public abstract List<CameraInstance> getCameraInstancesByPatternIdSync(long patternId);

    @Insert
    public abstract long insertCameraInstance(CameraInstance cameraInstance);

    @Insert
    public abstract void insertCameraInstances(List<CameraInstance> cameraInstances);

    @Update
    public abstract void updateCameraInstance(CameraInstance cameraInstance);

    @Query("DELETE FROM camerainstance WHERE id = :id")
    public abstract void deleteCameraInstancesWithId(long id);

    @Query("DELETE FROM camerainstance WHERE patternId = :patternId")
    public abstract void deleteCameraInstancesWithPatternId(long patternId);
}
