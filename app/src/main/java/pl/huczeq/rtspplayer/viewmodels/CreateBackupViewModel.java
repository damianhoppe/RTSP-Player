package pl.huczeq.rtspplayer.viewmodels;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.data.DataConverter;
import pl.huczeq.rtspplayer.data.DataManager;
import pl.huczeq.rtspplayer.data.Settings;
import pl.huczeq.rtspplayer.data.database.CamerasStats;
import pl.huczeq.rtspplayer.data.objects.CameraPattern;
import pl.huczeq.rtspplayer.interfaces.IOnTaskFinished;
import pl.huczeq.rtspplayer.viewmodels.base.DataManagerViewModel;

public class CreateBackupViewModel extends DataManagerViewModel {

    private LiveData<CamerasStats> camerasStats;
    private MutableLiveData<Boolean> isBackupCreating;
    private Thread backupCreatingThread;

    public CreateBackupViewModel(DataManager dataManager) {
        super(dataManager);
        this.camerasStats = dataManager.getCamerasStats();
        this.isBackupCreating = new MutableLiveData<>();
        this.isBackupCreating.setValue(false);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        interruptBackupCreatingThread();
    }

    public LiveData<CamerasStats> getCamerasStats() {
        return camerasStats;
    }

    public MutableLiveData<Boolean> getIsBackupCreating() {
        return isBackupCreating;
    }

    private void interruptBackupCreatingThread() {
        if(this.backupCreatingThread == null)
            return;
        if(this.backupCreatingThread.isAlive())
            this.backupCreatingThread.interrupt();
        this.backupCreatingThread = null;
        this.isBackupCreating.setValue(false);
    }

    public void createBackup(OutputStream outputStream, boolean camerasBackup, boolean settingsBackup, IOnTaskFinished callback) {
        interruptBackupCreatingThread();
        this.backupCreatingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    work();
                } catch (IOException e) {
                    callback.onError(e);
                    return;
                } finally {
                    isBackupCreating.postValue(false);
                }
                callback.onComplete();
            }

            private void work() throws IOException {
                JSONObject json = new JSONObject();
                if(camerasBackup) {
                    JSONArray jsonCameras = new JSONArray();
                    List<CameraPattern> cameraPatterns = dataManager.getCameraPatternListSync();
                    for (CameraPattern c : cameraPatterns) {
                        JSONObject obj = new JSONObject();
                        try {
                            if (c.getName() != null && !c.getName().isEmpty())
                                obj.put(DataConverter.JSONName, c.getName());
                            if (c.getUrl() != null && !c.getUrl().isEmpty())
                                obj.put(DataConverter.JSONUrl, c.getUrl());
                            if (c.getProducer() != null && !c.getProducer().isEmpty())
                                obj.put(DataConverter.JSONProducer, c.getProducer());
                            if (c.getModel() != null && !c.getModel().isEmpty())
                                obj.put(DataConverter.JSONModel, c.getModel());
                            if (c.getUserName() != null && !c.getUserName().isEmpty())
                                obj.put(DataConverter.JSONUserName, c.getUserName());
                            if (c.getPassword() != null && !c.getPassword().isEmpty())
                                obj.put(DataConverter.JSONPassword, c.getPassword());
                            if (c.getAddressIp() != null && !c.getAddressIp().isEmpty())
                                obj.put(DataConverter.JSONAddressIP, c.getAddressIp());
                            if (c.getPort() != null && !c.getPort().isEmpty())
                                obj.put(DataConverter.JSONPort, c.getPort());
                            if (c.getChannel() != null && !c.getChannel().isEmpty())
                                obj.put(DataConverter.JSONChannel, c.getChannel());
                            if (c.getStream() != null && !c.getStream().isEmpty())
                                obj.put(DataConverter.JSONStream, c.getStream());
                            if (c.getServerUrl() != null && !c.getServerUrl().isEmpty())
                                obj.put(DataConverter.JSONServerUrl, c.getServerUrl());
                            JSONArray jsonVariables = c.getJSONArrayVariables();
                            if(jsonVariables != null) {
                                obj.put(DataConverter.JSONVariables, jsonVariables);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            break;
                        }
                        jsonCameras.put(obj);
                    }
                    try {
                        json.put("cameras", jsonCameras);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if(settingsBackup) {
                    JSONObject jsonSettings = new JSONObject();
                    try {
                        jsonSettings.put(Settings.KEY_THEME, dataManager.getSettings().getTheme());
                        jsonSettings.put(Settings.KEY_DEFAULT_ORIENTATION, dataManager.getSettings().getDefaultOrientationValue());
                        jsonSettings.put(Settings.KEY_ORIENTATION_MODE, dataManager.getSettings().getOrientationModeValue());
                        jsonSettings.put(Settings.KEY_PLAYER_SURFACE, dataManager.getSettings().getPlayerSurface());
                        jsonSettings.put(Settings.KEY_GENERATE_CAMERA_TUMBNAIL_ONCE, dataManager.getSettings().isEnabledGenerateCameraTumbnailOnce());
                        jsonSettings.put(Settings.KEY_CACHING_BUFFER_SIZE, dataManager.getSettings().getCachingBufferSize());
                        jsonSettings.put(Settings.KEY_HARDWARE_ACCELERATION, dataManager.getSettings().isEnabledHardwareAcceleration());
                        jsonSettings.put(Settings.KEY_AVCODES_FAST, dataManager.getSettings().isEnabledAVCodes());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        json.put("settings", jsonSettings);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                outputStream.write(json.toString().getBytes());
                outputStream.flush();
                outputStream.close();
            }
        });
        this.isBackupCreating.setValue(true);
        this.backupCreatingThread.start();
    }
}
