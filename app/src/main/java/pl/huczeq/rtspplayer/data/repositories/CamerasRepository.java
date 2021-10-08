package pl.huczeq.rtspplayer.data.repositories;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.room.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import pl.huczeq.rtspplayer.data.database.AppDatabase;
import pl.huczeq.rtspplayer.data.database.CameraDao;
import pl.huczeq.rtspplayer.data.database.CamerasStats;
import pl.huczeq.rtspplayer.data.objects.Camera;
import pl.huczeq.rtspplayer.data.objects.CameraInstance;
import pl.huczeq.rtspplayer.data.objects.CameraPattern;
import pl.huczeq.rtspplayer.interfaces.ICameraInstancesService;
import pl.huczeq.rtspplayer.interfaces.IGetCameraCallback;
import pl.huczeq.rtspplayer.interfaces.IOnDataUpdated;
import pl.huczeq.rtspplayer.data.utils.ResultRunnable;

public class CamerasRepository {

    public static final String TAG = "CamerasRepository";

    private AppDatabase database;
    private CameraDao cameraDao;
    private LiveData<List<Camera>> allCameras;
    private Executor loadDataExecutor;
    private Executor modifyDataExecutor;

    private Handler uiThreadHandler;

    public CamerasRepository(Context context) {
        this.database = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "database").build();
        this.cameraDao = this.database.cameraDao();
        this.allCameras = cameraDao.getAllCameras();
        this.uiThreadHandler = new Handler(context.getMainLooper());
        this.loadDataExecutor = Executors.newSingleThreadExecutor();
        this.modifyDataExecutor = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Camera>> getAllCameras() {
        return this.allCameras;
    }

    public List<Camera> getCameraList() {
        return this.cameraDao.getCameraList();
    }

    public List<CameraPattern> getCameraPatterns() {
        return this.cameraDao.getCameraPatterns();
    }

    public LiveData<List<Camera>> getCameraById(int id) {
        return this.cameraDao.getCameraById(id);
    }

    public void getCameraById(int id, IGetCameraCallback callback) {
        this.loadDataExecutor.execute(new ResultRunnable<IGetCameraCallback>(callback) {
            @Override
            public void run() {
                List<Camera> cameraList = cameraDao.getCameraListById(id);
                Camera camera = (cameraList.size() > 0)? cameraList.get(0) : null;
                if(this.callbackReference != null) {
                    uiThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(callbackReference.get() != null)
                                callbackReference.get().onGetCamera(camera);
                        }
                    });
                }
            }
        });
    }

    public void addCamera(CameraPattern cameraPattern, ICameraInstancesService cameraInstancesService, IOnDataUpdated callback) {
        this.modifyDataExecutor.execute(new ResultRunnable<IOnDataUpdated>(callback) {
            @Override
            public void run() {
                addCameraSync(cameraPattern, cameraInstancesService);
                if(this.callbackReference != null) {
                    uiThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(callbackReference.get() != null)
                                callbackReference.get().onComplete();
                        }
                    });
                }
            }
        });
    }

    public void addCameraSync(CameraPattern cameraPattern, ICameraInstancesService cameraInstancesService) {
        List<CameraInstance> cameraInstances = cameraInstancesService.generateCameraInstances(cameraPattern);

        cameraPattern.setNumberOfInstances(cameraInstances.size());
        for(CameraInstance cameraInstance:cameraInstances)
            cameraInstance.setPatternId(cameraPattern.getId());
        cameraDao.addCamera(cameraPattern, cameraInstances);
    }

    public boolean addCamerasSync(List<CameraPattern> cameraPatterns, ICameraInstancesService cameraInstancesService, boolean clearData) {
        return this.database.runInTransaction(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                if(clearData) clearData();
                for(CameraPattern cameraPattern : cameraPatterns) {
                    addCameraSync(cameraPattern, cameraInstancesService);
                }
                return true;
            }
        });
    }

    public void updateCamera(CameraPattern cameraPattern, ICameraInstancesService cameraInstancesService, IOnDataUpdated callback) {
        this.modifyDataExecutor.execute(new ResultRunnable<IOnDataUpdated>(callback) {
            @Override
            public void run() {
                List<CameraInstance> newCameraInstances = cameraInstancesService.generateCameraInstances(cameraPattern);
                List<CameraInstance> cameraInstancesToDelete = new ArrayList<>();
                List<CameraInstance> currentCameraInstances = cameraDao.getCamerasByPatternId(cameraPattern.getId());
                cameraInstancesService.updateCameraPattern(cameraPattern, currentCameraInstances, newCameraInstances, cameraInstancesToDelete);
                cameraPattern.setNumberOfInstances(currentCameraInstances.size());
                cameraDao.updateCameraPattern(cameraPattern, currentCameraInstances, cameraInstancesToDelete);
                Log.d(TAG, "updateCamera finished :D - " + (this.callbackReference == null));
                if(this.callbackReference != null) {
                    uiThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "uiThreadHandler :D - " + (callbackReference.get() == null));
                            if(callbackReference.get() != null)
                                callbackReference.get().onComplete();
                        }
                    });
                }
            }
        });
    }

    public void updateCameraInstance(CameraInstance cameraInstance, IOnDataUpdated callback) {
        this.modifyDataExecutor.execute(new ResultRunnable<IOnDataUpdated>(callback) {
            @Override
            public void run() {
                cameraDao.updateCameraInstance(cameraInstance);
                if(this.callbackReference != null) {
                    uiThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(callbackReference.get() != null)
                                callbackReference.get().onComplete();
                        }
                    });
                }
            }
        });
    }

    public void deleteCameraPattern(CameraPattern cameraPattern) {
        this.modifyDataExecutor.execute(new Runnable() {
            @Override
            public void run() {
                cameraDao.deleteCameraPatternWithInstances(cameraPattern);
            }
        });
    }

    public void clearData() {
        this.database.clearAllTables();
    }

    public LiveData<CamerasStats> getStats() {
        return Transformations.map(this.cameraDao.getStats(), new Function<List<Integer>, CamerasStats>() {
            @Override
            public CamerasStats apply(List<Integer> input) {
                if(input == null)
                    return null;
                return new CamerasStats(input);
            }
        });
    }

    public void addUser(CameraPattern cam, IOnDataUpdated callback) {
        this.modifyDataExecutor.execute(new ResultRunnable<IOnDataUpdated>(callback) {
            @Override
            public void run() {
                cameraDao.addCamera(cam, new ArrayList<>());
            }
        });
    }
}