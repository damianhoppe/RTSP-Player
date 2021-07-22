package pl.huczeq.rtspplayer.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pl.huczeq.rtspplayer.data.database.CamerasStats;
import pl.huczeq.rtspplayer.data.objects.Camera;
import pl.huczeq.rtspplayer.data.objects.CameraInstance;
import pl.huczeq.rtspplayer.data.objects.CameraPattern;
import pl.huczeq.rtspplayer.data.repositories.CamerasRepository;
import pl.huczeq.rtspplayer.data.repositories.ThumbnailsRepository;
import pl.huczeq.rtspplayer.data.repositories.UrlTemplatesRepository;
import pl.huczeq.rtspplayer.data.threads.ImageLoadingThread;
import pl.huczeq.rtspplayer.data.utils.DataState;
import pl.huczeq.rtspplayer.interfaces.ICameraInstancesService;
import pl.huczeq.rtspplayer.interfaces.IGetCameraCallback;
import pl.huczeq.rtspplayer.data.objects.urltemplates.Producer;
import pl.huczeq.rtspplayer.interfaces.IOnDataUpdated;


public class DataManager implements ICameraInstancesService {

    private static DataManager instance;
    public static DataManager getInstance(Context context) {
        if(instance == null)
            instance = new DataManager(context.getApplicationContext());
        return instance;
    }

    private final String TAG = "DataManager";

    private Settings settings;
    private CamerasRepository camerasRepository;
    private ThumbnailsRepository thumbnailsRepository;
    private UrlTemplatesRepository urlTemplatesRepository;

    public DataManager(Context context) {
        Context appContext = context.getApplicationContext();
        this.settings = Settings.getInstance(appContext);
        this.camerasRepository = new CamerasRepository(appContext);
        this.thumbnailsRepository = new ThumbnailsRepository(appContext);
        this.urlTemplatesRepository = new UrlTemplatesRepository(appContext);
    }

    public Settings getSettings() {
        return settings;
    }

    public LiveData<List<Camera>> getAllCameras() {
        return this.camerasRepository.getAllCameras();
    }

    public List<Camera> getAllCamerasSync() {
        return this.camerasRepository.getCameraList();
    }

    public List<CameraPattern> getCameraPatternListSync() {
        return this.camerasRepository.getCameraPatterns();
    }

    public void getCameraById(int id, IGetCameraCallback callback) {
        this.camerasRepository.getCameraById(id, callback);
    }

    public void addCamera(CameraPattern cameraPattern, IOnDataUpdated callback) {
        camerasRepository.addCamera(cameraPattern, this, callback);
    }

    public void addCamerasSync(List<CameraPattern> list) {
        camerasRepository.addCamerasSync(list, this, false);
    }

    public void updateCamera(CameraPattern cameraPattern, IOnDataUpdated callback) {
        this.camerasRepository.updateCamera(cameraPattern, this, callback);
    }

    public void deleteCameraPattern(CameraPattern cameraPattern) {
        this.camerasRepository.deleteCameraPattern(cameraPattern);
    }

    public List<Producer> getUrlTemplates() {
        return this.urlTemplatesRepository.getProducerList();
    }

    public MutableLiveData<DataState> getUrlTemplatesRepositoryState() {
        return this.urlTemplatesRepository.getState();
    }

    public void loadCameraPreviewImg(CameraInstance cameraInstance, ImageLoadingThread.Callback callback) {
        this.thumbnailsRepository.loadPreviewImg(cameraInstance, callback);
    }

    public void saveCameraPreviewImg(CameraInstance cameraInstance, Bitmap bitmap) {
        this.thumbnailsRepository.savePreviewImg(cameraInstance, bitmap);
        camerasRepository.updateCameraInstance(cameraInstance, null);
    }

    public List<CameraInstance> generateCameraInstances(CameraPattern cameraPattern) {
        CameraInstancesFactory instancesFactory = new CameraInstancesFactory(cameraPattern);
        return instancesFactory.build();
    }

    @Override
    public void updateCameraPattern(CameraPattern cameraPattern, List<CameraInstance> currentCameraInstaces, List<CameraInstance> newCameraInstances, List<CameraInstance> cameraInstancesToDelete) {
        if(currentCameraInstaces.size() != newCameraInstances.size()) {
            if(currentCameraInstaces.size() < newCameraInstances.size()) {
                for(int i = currentCameraInstaces.size(); i < newCameraInstances.size(); i++) {
                    CameraInstance cameraInstance = new CameraInstance();
                    cameraInstance.setPatternId(cameraPattern.getId());
                    currentCameraInstaces.add(cameraInstance);
                }
            }else {
                while(newCameraInstances.size() < currentCameraInstaces.size()) {
                    CameraInstance cameraInstanceToDelete = currentCameraInstaces.get(currentCameraInstaces.size() - 1);
                    cameraInstancesToDelete.add(cameraInstanceToDelete);
                    currentCameraInstaces.remove(cameraInstanceToDelete);
                }
            }
        }
        for(int i = 0; i < currentCameraInstaces.size(); i++) {
            currentCameraInstaces.get(i).update(newCameraInstances.get(i));
        }
    }

    public LiveData<CamerasStats> getCamerasStats() {
        return this.camerasRepository.getStats();
    }

    public void loadUrlTemplates() {
        this.urlTemplatesRepository.loadData();
    }

    public void finishImageLoadingTask() {
        this.thumbnailsRepository.finishImageLoadingTask();
    }

    public boolean loadBackup(String fileName, boolean clearData) {
        File file = new File(getSettings().getBackupsDir(), fileName);
        if(!file.exists()) {
            return false;
        }
        if(!file.canRead()){
            return false;
        }

        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(text.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

        try {
            ArrayList<CameraPattern> cameraPatterns = new ArrayList<>();
            JSONArray array = jsonObject.getJSONArray("cameras");
            for (int i = 0; i < array.length(); i++) {
                CameraPattern cameraPattern;
                try {
                    cameraPattern = DataConverter.json2CameraPattern(array.getJSONObject(i), getSettings().getAppContext());
                } catch (JSONException e) {
                    e.printStackTrace();
                    continue;
                }
                cameraPatterns.add(cameraPattern);
                if(!cameraPatterns.isEmpty()) {
                    this.camerasRepository.addCamerasSync(cameraPatterns, this, clearData);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject jsonSettings = null;
        try {
            jsonSettings = jsonObject.getJSONObject("settings");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(jsonSettings != null) {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getSettings().getAppContext());
            SharedPreferences.Editor editor = pref.edit();
            if (jsonSettings.has(Settings.KEY_THEME)) editor.putString(Settings.KEY_THEME, jsonSettings.optString(Settings.KEY_THEME, "0"));
            if (jsonSettings.has(Settings.KEY_DEFAULT_ORIENTATION)) editor.putString(Settings.KEY_DEFAULT_ORIENTATION, jsonSettings.optString(Settings.KEY_DEFAULT_ORIENTATION, "0"));
            if (jsonSettings.has(Settings.KEY_ORIENTATION_MODE)) editor.putString(Settings.KEY_ORIENTATION_MODE, jsonSettings.optString(Settings.KEY_ORIENTATION_MODE, "0"));
            if (jsonSettings.has(Settings.KEY_PLAYER_SURFACE)) editor.putString(Settings.KEY_PLAYER_SURFACE, jsonSettings.optString(Settings.KEY_PLAYER_SURFACE, "0"));
            if (jsonSettings.has(Settings.KEY_GENERATE_CAMERA_TUMBNAIL_ONCE)) editor.putBoolean(Settings.KEY_GENERATE_CAMERA_TUMBNAIL_ONCE, jsonSettings.optBoolean(Settings.KEY_GENERATE_CAMERA_TUMBNAIL_ONCE, true));
            if (jsonSettings.has(Settings.KEY_CACHING_BUFFER_SIZE)) editor.putInt(Settings.KEY_CACHING_BUFFER_SIZE, jsonSettings.optInt(Settings.KEY_CACHING_BUFFER_SIZE, 10));
            if (jsonSettings.has(Settings.KEY_HARDWARE_ACCELERATION)) editor.putBoolean(Settings.KEY_HARDWARE_ACCELERATION, jsonSettings.optBoolean(Settings.KEY_HARDWARE_ACCELERATION, true));
            if (jsonSettings.has(Settings.KEY_AVCODES_FAST)) editor.putBoolean(Settings.KEY_AVCODES_FAST, jsonSettings.optBoolean(Settings.KEY_AVCODES_FAST, false));
            editor.commit();
            settings.callListener(Settings.KEY_THEME);
            settings.callListener(Settings.KEY_PLAYER_SURFACE);
            settings.callListener(Settings.KEY_GENERATE_CAMERA_TUMBNAIL_ONCE);
            settings.callListener(Settings.KEY_CACHING_BUFFER_SIZE);
            settings.callListener(Settings.KEY_HARDWARE_ACCELERATION);
            settings.callListener(Settings.KEY_AVCODES_FAST);
        }
        return true;
    }
//
//    /*
//    private static Thread dataLoader;
//
//
//
//
//    public interface Callback {
//        void onDataLoaded();
//    }
//
//    /*
//    public static DataManager getInstance(Context context) {
//        return getInstance(context.getApplicationContext(), null);
//    }
//    public static synchronized DataManager getInstance(Context context, final Callback callback) {
//        if(DataManager.instance == null)
//            DataManager.instance = new DataManager(context.getApplicationContext());
//        if(!instance.dataLoaded) {
//            if(callback == null) {
//                instance.loadData();
//            }else {
//                if(dataLoader == null) {
//                    dataLoader = new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            instance.loadData();
//                            instance.dataLoaded = true;
//                            notifyDataLoaded(callback);
//                            dataLoader = null;
//                        }
//                    });
//                    dataLoader.start();
//                }
//            }
//        }
//        return DataManager.instance;
//    }*/
//
//
//    private static void notifyDataLoaded(final Callback callback) {
//        new Handler(Looper.getMainLooper()).post(new Runnable() {
//            @Override
//            public void run() {
//                if (callback != null)
//                    callback.onDataLoaded();
//            }
//        });
//    }
//
//
//    private final String fileName = "data.json";
//    private Context context;
//    private JSONObject jsonObject;
//
//    public final static String JSONCamerasDataArray = "camerasData";//
//    public final static String JSONCameraInstancesDataArray = "cameraInstancesData";
//    public final static String JSONCameraPatternsDataArray = "cameraPatternsData";
//    public final static String JSONSettings = "settings";
//
//    private ArrayList<OnDataChanged> onDataChangedListeners;
//    private Thread urlsTemplatesLoadingThread;
//    private Thread saveDataThread;
//    private Settings settings;
//    private UrlsTemplates urlsTemplates;
//
//    private boolean dataSaved = true;
//
//    private class SaveDataThread extends Thread {
//
//        @Override
//        public void run() {
//            do {
//                saveData();
//                dataSaved = true;
//            } while(!isDataSavedForThread());
//        }
//        private void saveData(){
//            JSONArray jsonCameraInstances = new JSONArray();
//            JSONArray jsonCameraPatterns = new JSONArray();
//            for (CameraInstance cameraInstance : data.getCameraInstaceList()) {
//                jsonCameraInstances.put(cameraInstance.toJSONObject());
//            }
//            for (CameraPattern cameraPattern : data.getCameraPatternList()) {
//                Log.d(TAG, "Save-Pattern: " + cameraPattern.getName() + " / " + cameraPattern.getId());
//                jsonCameraPatterns.put(cameraPattern.toJSONObject());
//            }
//
//            JSONObject newJsonObject = new JSONObject();
//            try {
//                newJsonObject.put(JSONCameraInstancesDataArray, jsonCameraInstances);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            try {
//                newJsonObject.put(JSONCameraPatternsDataArray, jsonCameraPatterns);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            jsonObject = newJsonObject;
//
//            File file = new File(context.getFilesDir(), fileName);
//            FileOutputStream stream = null;
//            try {
//                stream = new FileOutputStream(file);
//                stream.write(jsonObject.toString().getBytes());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            if(stream != null) {
//                try {
//                    stream.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            dataSaved = true;
//        }
//    }/*
//
//    private AppDatabase db;
//    public DataManager(Context context) {
//        this.context = context;
//        this.dataLoaded = false;
//        this.onDataChangedListeners = new ArrayList<>();
//        this.imageLoadingThread = new ImageLoadingThread(context);
//        this.imageSavingThread = new ImageSavingThread(context);
//        this.settings = Settings.getInstance(context);
//        this.urlsTemplates = new UrlsTemplates(context);
//
//        this.imageLoadingThread.start();
//        this.imageSavingThread.start();
//
//        db = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "database").allowMainThreadQueries().build();
//    }*/
//
//
//
//    public void updateCamerasList(List<CameraInstance> cameras, List<CameraPattern> patterns) {
//        this.data.updateCamerasList(cameras, patterns);
//        this.notifyDataChange();
//    }
//
//    public boolean isDataSaved() {
//        return this.dataSaved;
//    }
//
//    public void resetFileData() {
//        this.data = new Data();
//
//        this.jsonObject = getEmptyJsonObject();
//        this.saveData();
//        notifyDataChange();
//    }
//
//    public ArrayList<CameraInstance> getCameraList() {
//        if(dataLoaded)
//            return data.getCameraInstaceList();
//        return new ArrayList<>();
//    }
//    public void addCamera(CameraPattern cameraPattern) {
//
//        /*
//        cameraPattern.setId(data.generateNextCameraPatternId());
//        Log.d(TAG, "New pattern id" + cameraPattern.getId() + ", data.lastPId: " + data.getLastCameraPatternId());
//
//        VariationsGenerator generator = new VariationsGenerator();
//        generator.init(cameraPattern.getUrl());
//        cameraPattern.setUrl(generator.getExpression().toString());
//        Log.d(TAG, "Url: " + cameraPattern.getUrl());
//        Log.d(TAG, "Size: " + generator.getVariations().size());
//
//        cameraPattern.setNumberOfInstances(generator.getVariations().size());
//        if(cameraPattern.getNumberOfInstances() == 0)
//            return;
//        for(int i = 0; i < generator.getVariations().size(); i++) {
//            String url = generator.getExpression().parse(generator.getVariations().get(i));
//            String cameraName = this.parseCameraName(cameraPattern.getName(), cameraPattern, i+1);
//            CameraInstance cameraInstance = new CameraInstance(cameraName, url, cameraPattern);
//            cameraInstance.setId(data.generateNextCameraInstanceId());
//            cameraInstance.setPatternId(cameraPattern.getId());
//            cameraInstance.setPatternData(generator.getVariations().get(i));
//            cameraInstance.getPatternData().put("i", i);
//            for(Map.Entry<String, String> entry : cameraInstance.getPatternData().entrySet()) {
//                Log.d(TAG, "E: " + entry.getKey() + " : " + entry.getValue());
//            }
//            this.data.addCameraInstance(cameraInstance);
//        }
//        data.getCameraPatternList().add(cameraPattern);
//        notifyDataChange();
//        this.saveData();*/
//    }
//
//    public void deleteCamera(CameraInstance cameraInstance) {
//        CameraPattern cameraPattern = cameraInstance.getPattern();
//        if(cameraPattern.getNumberOfInstances() > 1) {
//            Log.d(TAG, "Deleting: " + cameraInstance.getName() + " - this is group of cameras: " + cameraInstance.getPattern().getNumberOfInstances() + " cameras.");
//        }
//        deletePreviewImg(cameraInstance);
//        //
//        cameraPattern.setNumberOfInstances(cameraPattern.getNumberOfInstances()-1);
//        if(cameraPattern.getNumberOfInstances() == 0)
//            data.deleteCameraPattern(cameraPattern);
//        data.deleteCameraInstance(cameraInstance);
//        notifyDataChange();
//        this.saveData();
//    }
//
//    public void deletePreviewImg(CameraInstance cameraInstance) {
//        if(cameraInstance.getPreviewImg() != null && !cameraInstance.getPreviewImg().isEmpty()) {
//            File previewImg = new File(settings.getPreviewImagesDir(), cameraInstance.getPreviewImg());
//            if(previewImg.isFile() && previewImg.exists())
//                previewImg.delete();
//        }
//    }
//
//    private String parseCameraName(String value, CameraPattern cameraPattern, int index) {
//        String result = value;
//        result = result.replaceAll("\\{index\\}", String.valueOf(index));
//        result = result.replaceAll("\\{i\\}", String.valueOf(index));
//        result = result.replaceAll("\\{producer\\}", String.valueOf(cameraPattern.getProducer()));
//        result = result.replaceAll("\\{model\\}", String.valueOf(cameraPattern.getModel()));
//        return result;
//    }
//
//    public void deleteCameraPatternWithInstances(CameraPattern cameraPattern) {
//        List<CameraInstance> cameraInstancesToDelete = new ArrayList<>();
//        for(CameraInstance cameraInstance : data.getCameraInstaceList()) {
//            if(cameraInstance.getPatternId() == cameraPattern.getId())
//                cameraInstancesToDelete.add(cameraInstance);
//        }
//        for(CameraInstance cameraInstance : cameraInstancesToDelete) {
//            data.deleteCameraInstance(cameraInstance);
//        }
//        data.deleteCameraPattern(cameraPattern);
//        notifyDataChange();
//        this.saveData();
//    }
//
//    public void updateCamera(int patternId, CameraPattern cameraPattern) {
//        CameraPattern currentCameraPattern = getCameraPattern(patternId);
//        Log.d(TAG, "CameraPattern is null? " + (currentCameraPattern == null) + " id: " + patternId);
//        Log.d(TAG, currentCameraPattern.getUrl());
//        Log.d(TAG, cameraPattern.getUrl());
//        if(currentCameraPattern != null) updateCamera(currentCameraPattern, cameraPattern);
//    }
//    public void updateCamera(CameraPattern cameraPattern, CameraPattern newCameraPattern) {
//        //cameraPattern.update(newCameraPattern);
//        List<CameraInstance> currentCameraInstances = data.getCameraInstancesListByPatternId(cameraPattern.getId());
//
//
//        VariationsGenerator generator = new VariationsGenerator();
//        generator.init(cameraPattern.getUrl());
//        Log.d(TAG, "" + currentCameraInstances.size() + " / " + generator.size());
//        cameraPattern.setNumberOfInstances(generator.size());
//        cameraPattern.setUrl(generator.getExpression().toString());
//        Log.d(TAG, "New expression: " + generator.getExpression().toString());
//        Log.d(TAG, "New expression: " + cameraPattern.getUrl());
//        if(currentCameraInstances.size() < generator.size()) {
//            for(int i = currentCameraInstances.size(); i < generator.size(); i++) {
//                /*CameraInstance cameraInstance = new CameraInstance(data.generateNextCameraInstanceId());
//                cameraInstance.setPattern(cameraPattern);
//                cameraInstance.setPatternId(cameraPattern.getId());
//                currentCameraInstances.add(cameraInstance);
//                data.addCameraInstance(cameraInstance);*/
//            }
//        }else if(currentCameraInstances.size() > generator.size()) {
//            while(generator.size() < currentCameraInstances.size()) {
//                CameraInstance cameraInstanceToDelete = currentCameraInstances.get(currentCameraInstances.size() - 1);
//                deletePreviewImg(cameraInstanceToDelete);
//                data.deleteCameraInstance(cameraInstanceToDelete);
//                currentCameraInstances.remove(cameraInstanceToDelete);
//            }
//        }
//        for(int i = 0; i < currentCameraInstances.size(); i++) {
//            String url = generator.getExpression().parse(generator.getVariations().get(i));
//            String cameraName = this.parseCameraName(cameraPattern.getName(), cameraPattern, i+1);
//            CameraInstance cameraInstance = currentCameraInstances.get(i);
//            cameraInstance.setName(cameraName);
//            cameraInstance.setUrl(url);
//            if(cameraInstance.getPattern() != cameraPattern) {
//                Log.e(TAG, "??????? error");
//            }
//            cameraInstance.setPattern(cameraPattern);
//            cameraInstance.setPatternData(generator.getVariations().get(i));
//           // cameraInstance.getPatternData().put("i", i);
//        }
//        notifyDataChange();
//        this.saveData();
//    }
//
//    public CameraInstance getCameraInstance(int id) {
//        for (CameraInstance cameraInstance : this.data.getCameraInstaceList()) {
//            if(cameraInstance.getId() == id)
//                return cameraInstance;
//        }
//        return null;
//    }
//
//    public CameraPattern getCameraPattern(int id) {
//        for (CameraPattern cameraPattern : this.data.getCameraPatternList()) {
//            if(cameraPattern.getId() == id)
//                return cameraPattern;
//        }
//        return null;
//    }
//
//    public void addOnDataChangedListener(OnDataChanged onDataChangedListener) {
//        this.onDataChangedListeners.add(onDataChangedListener);
//    }
//    public void removeOnDataChangeListener(OnDataChanged onDataChanged) {
//        this.onDataChangedListeners.remove(onDataChanged);
//    }
//    private void notifyDataChange() {
//        for (OnDataChanged listener : this.onDataChangedListeners) {
//            if(listener == null) {
//                this.onDataChangedListeners.remove(listener);
//                continue;
//            }
//            listener.onDataChanged();
//        }
//    }
//    public boolean isDataLoaded() {
//        return this.dataLoaded;
//    }
//
//    Callback urlTemplatesLoadedCallback;
//
//    public boolean isUrlsTemplatesLoaded() {
//        return (this.urlsTemplates.getProducerList() != null);
//    }
//
//    public void loadUrlsTemplates(Callback callback) {
//        Log.d(TAG, "loadUrlsTemplates");
//        this.urlTemplatesLoadedCallback = callback;
//        if(!this.urlsTemplates.isDataLoaded() && urlsTemplatesLoadingThread==null) {
//            urlsTemplatesLoadingThread = new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    Log.d(TAG, "Started");
//                    if(!urlsTemplates.load()) {
//                        List<Producer> producers = new ArrayList<>();
//                        producers.add(new Producer("Default", new Model("Default", new UrlTemplate("rtsp://{user}:{password}@{addressip}:{port}", "rtsp://{addressip}:{port}"))));
//                        urlsTemplates.setProducersList(producers);
//                    }
//                    Log.d(TAG, "End");
//                    new Handler(Looper.getMainLooper()).post(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (urlTemplatesLoadedCallback != null)
//                                urlTemplatesLoadedCallback.onDataLoaded();
//                            urlsTemplatesLoadingThread=null;
//                        }
//                    });
//                }
//            });
//            urlsTemplatesLoadingThread.start();
//        }
//    }
}
