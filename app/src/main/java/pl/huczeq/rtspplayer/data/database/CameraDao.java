package pl.huczeq.rtspplayer.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

import pl.huczeq.rtspplayer.data.objects.Camera;
import pl.huczeq.rtspplayer.data.objects.CameraInstance;
import pl.huczeq.rtspplayer.data.objects.CameraPattern;

@Dao
public abstract class CameraDao {

    @Transaction
    @Query("SELECT * FROM cameraInstance WHERE patternId == :patternId")
    public abstract List<CameraInstance> getCamerasByPatternId(int patternId);

    @Query("SELECT * FROM cameraInstance WHERE id = :id")
    public abstract CameraInstance getCameraInstanceById(int id);

    @Transaction
    @Query("SELECT * FROM cameraInstance WHERE id = :id")
    public abstract Camera getCameraById(int id);

    @Insert
    protected abstract void insertCameraInstance(CameraInstance cameraInstance);

    @Insert
    protected abstract void insertCameraInstances(List<CameraInstance> cameraInstances);

    @Insert
    protected abstract long insertCameraPattern(CameraPattern cameraPattern);

    @Update
    public abstract void updateCameraInstance(CameraInstance cameraInstance);

    @Update
    protected abstract void updateCameraPattern(CameraPattern cameraPattern);

    @Delete
    protected abstract void deleteCameraPattern(CameraPattern cameraPattern);

    @Query("DELETE FROM cameraInstance WHERE patternId = :patternId")
    public abstract void deleteCameraInstanceWithPatternId(int patternId);

    @Delete
    protected abstract void deleteCameraInstance(CameraInstance cameraInstance);

    @Transaction
    @Query("SELECT * FROM camerainstance ORDER BY patternId ASC")
    public abstract LiveData<List<Camera>> getAllCameras();

    @Transaction
    @Query("SELECT * FROM camerainstance")
    public abstract List<Camera> getCameraList();

    @Query("SELECT * FROM camerapattern")
    public abstract List<CameraPattern> getCameraPatterns();

    @Query("SELECT COUNT(id) FROM camerainstance UNION ALL SELECT COUNT(id) FROM camerapattern UNION ALL SELECT COUNT(id) FROM camerapattern WHERE numberOfInstances = 1")
    public abstract LiveData<List<Integer>> getStats();

    @Transaction
    public void updateCameraPattern(CameraPattern cameraPattern, List<CameraInstance> cameraInstances, List<CameraInstance> instancesToDelete) {
        updateCameraPattern(cameraPattern);
        for(CameraInstance cameraInstance : instancesToDelete)
            deleteCameraInstance(cameraInstance);
        for(CameraInstance cameraInstance : cameraInstances) {
            cameraInstance.setPatternId(cameraPattern.getId());
            if(getCameraInstanceById(cameraInstance.getId()) == null)
                insertCameraInstance(cameraInstance);
            else
                updateCameraInstance(cameraInstance);
        }
    }

    @Transaction
    public void addCamera(CameraPattern cameraPattern, List<CameraInstance> cameraInstances) {
        int patternId = (int)insertCameraPattern(cameraPattern);
        for(CameraInstance instance : cameraInstances)
            instance.setPatternId(patternId);
        insertCameraInstances(cameraInstances);
    }

    @Transaction
    public void deleteCameraPatternWithInstances(CameraPattern cameraPattern) {
        deleteCameraPattern(cameraPattern);
        deleteCameraInstanceWithPatternId(cameraPattern.getId());
    }
}
