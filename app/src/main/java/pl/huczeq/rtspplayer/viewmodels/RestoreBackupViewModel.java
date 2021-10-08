package pl.huczeq.rtspplayer.viewmodels;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.data.DataConverter;
import pl.huczeq.rtspplayer.data.DataManager;
import pl.huczeq.rtspplayer.data.Settings;
import pl.huczeq.rtspplayer.data.objects.CameraInstance;
import pl.huczeq.rtspplayer.data.objects.CameraPattern;
import pl.huczeq.rtspplayer.interfaces.IOnTaskFinished;
import pl.huczeq.rtspplayer.viewmodels.base.DataManagerViewModel;

public class RestoreBackupViewModel extends DataManagerViewModel {

    public static class DataModel {
        public JSONObject jsonObject;
        public int numberOfCameras;
        public String fileName;
        public boolean containsSettings;
        public boolean containsCameras;
    }
    private MutableLiveData<DataModel> data;
    private MutableLiveData<Boolean> isRunning;
    private ExecutorService executor;

    public RestoreBackupViewModel(DataManager dataManager) {
        super(dataManager);
        this.data = new MutableLiveData<>();
        this.data.setValue(null);
        this.isRunning = new MutableLiveData<>();
        this.isRunning.setValue(false);
        this.executor = Executors.newSingleThreadExecutor();
    }

    public boolean canRestoreData() {
        return !this.isRunning.getValue() && this.data != null;
    }

    public void initData(String fileName, InputStream inputStream, IOnTaskFinished callback) {
        this.data.setValue(null);
        this.isRunning.setValue(true);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    data.postValue(work(inputStream));
                } catch (Exception e) {
                    callback.onError(e);
                    return;
                } finally {
                    isRunning.postValue(false);
                }
                callback.onComplete();
            }

            private DataModel work(InputStream stream) throws IOException, JSONException {
                DataModel dataModel = new DataModel();

                StringBuilder stringBuilder = new StringBuilder();
                BufferedReader br = new BufferedReader(new InputStreamReader(stream));
                String line;
                while ((line = br.readLine()) != null) {
                    stringBuilder.append(line);
                    stringBuilder.append('\n');
                }
                br.close();
                JSONObject json = new JSONObject(stringBuilder.toString());

                JSONArray cameras = json.optJSONArray("cameras");
                JSONObject settings = json.optJSONObject("settings");
                dataModel.containsCameras = cameras != null;
                dataModel.containsSettings = settings != null;
                dataModel.numberOfCameras = cameras.length();
                dataModel.fileName = fileName;
                dataModel.jsonObject = json;
                return dataModel;
            }
        });
    }

    public void restoreData(boolean restoreCameras, boolean restoreSettings, IOnTaskFinished callback) {
        DataModel data = this.data.getValue();
        this.isRunning.setValue(true);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                //TODO error stats
                Log.d("Test", "Restore start");
                if(restoreCameras && data.containsCameras) {
                    Log.d("Test", "Restore cameras");
                    JSONArray jsonCameras = data.jsonObject.optJSONArray("cameras");
                    if(jsonCameras != null) {
                        List<CameraPattern> cameraPatterns = new ArrayList<>();
                        for (int i = 0; i < jsonCameras.length(); i++) {
                            JSONObject obj = null;
                            try {
                                obj = jsonCameras.getJSONObject(i);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                continue;
                            }
                            CameraPattern cameraPattern = new CameraPattern();
                            try {
                                cameraPattern.setName(obj.getString(DataConverter.JSONName));
                                cameraPattern.setUrl(obj.getString(DataConverter.JSONUrl));
                            } catch (JSONException e) {
                                e.printStackTrace();
                                continue;
                            }
                            try {
                                cameraPattern.setProducer(obj.getString(DataConverter.JSONProducer));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                cameraPattern.setModel(obj.getString(DataConverter.JSONModel));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                cameraPattern.setUserName(obj.getString(DataConverter.JSONUserName));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                cameraPattern.setPassword(obj.getString(DataConverter.JSONPassword));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                cameraPattern.setAddressIp(obj.getString(DataConverter.JSONAddressIP));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                cameraPattern.setPort(obj.getString(DataConverter.JSONPort));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                cameraPattern.setChannel(obj.getString(DataConverter.JSONChannel));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                cameraPattern.setStream(obj.getString(DataConverter.JSONStream));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                cameraPattern.setServerUrl(obj.getString(DataConverter.JSONServerUrl));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                cameraPattern.setJsonVariables(obj.getString(DataConverter.JSONVariables));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            cameraPatterns.add(cameraPattern);
                        }
                        dataManager.addCamerasSync(cameraPatterns, true);
                    }
                }
                if(restoreSettings && data.containsSettings) {
                    Log.d("Test", "Restore settings");
                    JSONObject jsonSettings = data.jsonObject.optJSONObject("settings");
                    if(jsonSettings != null) {
                        Settings settings = dataManager.getSettings();
                        SharedPreferences pref = settings.getSettingsPref();
                        SharedPreferences.Editor editor = pref.edit();
                        if (jsonSettings.has(Settings.KEY_THEME)) {
                            editor.putString(Settings.KEY_THEME, jsonSettings.optString(Settings.KEY_THEME, "0"));
                            settings.callListener(Settings.KEY_THEME);
                        }
                        if (jsonSettings.has(Settings.KEY_DEFAULT_ORIENTATION)) {
                            editor.putString(Settings.KEY_DEFAULT_ORIENTATION, jsonSettings.optString(Settings.KEY_DEFAULT_ORIENTATION, "0"));
                            settings.callListener(Settings.KEY_DEFAULT_ORIENTATION);
                        }
                        if (jsonSettings.has(Settings.KEY_ORIENTATION_MODE)) {
                            editor.putString(Settings.KEY_ORIENTATION_MODE, jsonSettings.optString(Settings.KEY_ORIENTATION_MODE, "0"));
                            settings.callListener(Settings.KEY_ORIENTATION_MODE);
                        }
                        if (jsonSettings.has(Settings.KEY_PLAYER_SURFACE)) {
                            editor.putString(Settings.KEY_PLAYER_SURFACE, jsonSettings.optString(Settings.KEY_PLAYER_SURFACE, "0"));
                            settings.callListener(Settings.KEY_PLAYER_SURFACE);
                        }
                        if (jsonSettings.has(Settings.KEY_GENERATE_CAMERA_TUMBNAIL_ONCE)) {
                            editor.putBoolean(Settings.KEY_GENERATE_CAMERA_TUMBNAIL_ONCE, jsonSettings.optBoolean(Settings.KEY_GENERATE_CAMERA_TUMBNAIL_ONCE, true));
                            settings.callListener(Settings.KEY_GENERATE_CAMERA_TUMBNAIL_ONCE);
                        }
                        if (jsonSettings.has(Settings.KEY_CACHING_BUFFER_SIZE)) {
                            editor.putInt(Settings.KEY_CACHING_BUFFER_SIZE, jsonSettings.optInt(Settings.KEY_CACHING_BUFFER_SIZE, 10));
                            settings.callListener(Settings.KEY_CACHING_BUFFER_SIZE);
                        }
                        if (jsonSettings.has(Settings.KEY_HARDWARE_ACCELERATION)) {
                            editor.putBoolean(Settings.KEY_HARDWARE_ACCELERATION, jsonSettings.optBoolean(Settings.KEY_HARDWARE_ACCELERATION, true));
                            settings.callListener(Settings.KEY_HARDWARE_ACCELERATION);
                        }
                        if (jsonSettings.has(Settings.KEY_AVCODES_FAST)) {
                            editor.putBoolean(Settings.KEY_AVCODES_FAST, jsonSettings.optBoolean(Settings.KEY_AVCODES_FAST, false));
                            settings.callListener(Settings.KEY_AVCODES_FAST);
                        }
                        editor.commit();
                    }
                }
                isRunning.postValue(false);
                callback.onComplete();
            }
        });
    }

    public MutableLiveData<DataModel> getData() {
        return data;
    }

    public MutableLiveData<Boolean> getIsRunning() {
        return isRunning;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdownNow();
    }
    /*
    public void loadBackup(String fileName, boolean clearData) {
        if(threadDataRestoring != null) {
            return;
        }
        threadDataRestoring = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean success = dataManager.loadBackup(fileName, clearData);
                finish(success);
            }

            private void finish(final boolean success) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if(success)
                            Toast.makeText(dataManager.getSettings().getAppContext(), R.string.success_restore, Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(dataManager.getSettings().getAppContext(), R.string.error_restore, Toast.LENGTH_SHORT).show();
                        dataIsBeingRestored.postValue(false);
                    }
                });
            }
        });
        dataIsBeingRestored.setValue(true);
        this.threadDataRestoring.start();
    }*/
}
