package pl.huczeq.rtspplayer.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import pl.huczeq.rtspplayer.interfaces.OnDataChanged;
import pl.huczeq.rtspplayer.data.objects.Camera;
import pl.huczeq.rtspplayer.data.objects.urls.Model;
import pl.huczeq.rtspplayer.data.objects.urls.Producer;
import pl.huczeq.rtspplayer.data.objects.urls.UrlTemplate;
import pl.huczeq.rtspplayer.data.threads.ImageLoadingThread;
import pl.huczeq.rtspplayer.data.threads.ImageSavingThread;


public class DataManager {

    public interface Callback {
        void onDataLoaded();
    }

    private static DataManager instance;
    private static Thread dataLoader;
    public static DataManager getInstance(Context context) {
        return getInstance(context.getApplicationContext(), null);
    }
    public static synchronized DataManager getInstance(Context context, final Callback callback) {
        if(DataManager.instance == null)
            DataManager.instance = new DataManager(context.getApplicationContext());
        if(!instance.dataLoaded) {
            if(callback == null) {
                instance.loadData();
            }else {
                if(dataLoader == null) {
                    dataLoader = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            instance.loadData();
                            instance.dataLoaded = true;
                            notifyDataLoaded(callback);
                            dataLoader = null;
                        }
                    });
                    dataLoader.start();
                }
            }
        }
        return DataManager.instance;
    }
    private static void notifyDataLoaded(final Callback callback) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (callback != null)
                    callback.onDataLoaded();
            }
        });
    }

    private final String TAG = "DataManager";

    private final String fileName = "data.json";
    private boolean dataLoaded;
    private Context context;
    private Data data;
    private JSONObject jsonObject;

    public final static String JSONCamerasDataArray = "camerasData";
    public final static String JSONSettings = "settings";

    private ArrayList<OnDataChanged> onDataChangedListeners;
    private ImageLoadingThread imageLoadingThread;
    private ImageSavingThread imageSavingThread;
    private Thread urlsTemplatesLoadingThread;
    private Thread saveDataThread;
    private Settings settings;
    private UrlsTemplates urlsTemplates;

    private boolean dataSaved = true;

    private class SaveDataThread extends Thread {

        @Override
        public void run() {
            do {
                saveData();
                dataSaved = true;
            } while(!isDataSavedForThread());
        }
        private void saveData(){
            JSONArray cameras = new JSONArray();
            for (Camera camera : data.getCameraList()) {
                cameras.put(camera.toJSONObject());
            }
            JSONObject newJsonObject = new JSONObject();
            try {
                newJsonObject.put(JSONCamerasDataArray, cameras);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsonObject = newJsonObject;

            File file = new File(context.getFilesDir(), fileName);
            FileOutputStream stream = null;
            try {
                stream = new FileOutputStream(file);
                stream.write(jsonObject.toString().getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            dataSaved = true;
        }
    }

    public DataManager(Context context) {
        this.context = context;
        this.dataLoaded = false;
        this.onDataChangedListeners = new ArrayList<>();
        this.imageLoadingThread = new ImageLoadingThread(context);
        this.imageSavingThread = new ImageSavingThread(context);
        this.settings = Settings.getInstance(context);
        this.urlsTemplates = new UrlsTemplates(context);

        this.imageLoadingThread.start();
        this.imageSavingThread.start();
    }
    private void loadData() {
        this.context.getFilesDir();
        File file = new File(this.context.getFilesDir(), this.fileName);
        if(!file.exists()) {
            Log.d(TAG, "File not exists!");
            resetFileData();
            this.dataLoaded = true;
            return;
        }
        if(!file.canRead()){
            resetFileData();
            Log.d(TAG, "Data can not be loaded");
            return;
        }

        synchronized (this) {
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
                resetFileData();
            }
            try {
                jsonObject = new JSONObject(text.toString());
            } catch (JSONException e) {
                e.printStackTrace();
                resetFileData();
            }

            boolean mustSave = false;
            data = new Data();
            try {
                JSONArray array = jsonObject.getJSONArray(JSONCamerasDataArray);
                for (int i = 0; i < array.length(); i++) {
                    Log.d(TAG, array.get(i).toString());
                    Camera camera = new Camera(array.getJSONObject(i));
                    if(camera.getPreviewImg() != null) {
                        File f = new File(this.settings.getPreviewImagesDir(), camera.getPreviewImg());
                        if(!f.exists()) {
                            camera.setPreviewImg(null);
                            mustSave = true;
                        }
                    }
                    data.getCameraList().add(camera);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                resetFileData();
            }
            Log.d(TAG, "Data loaded");
            this.dataLoaded = true;
            if(mustSave) this.saveData();
        }
    }
    public void saveData() {
        synchronized (SaveDataThread.class) {
            dataSaved = false;
            if (this.saveDataThread == null) {
                this.saveDataThread = new SaveDataThread();
                this.saveDataThread.start();
            }
        }
    }
    private boolean isDataSavedForThread() {
        synchronized (SaveDataThread.class) {
            if(dataSaved) {
                this.saveDataThread.interrupt();
                this.saveDataThread = null;
                return true;
            }
            return false;
        }
    }

    public void updateCamerasList(ArrayList<Camera> list) {
        this.data.updateCamerasList(list);
        this.notifyDataChange();
    }

    public boolean isDataSaved() {
        return this.dataSaved;
    }

    public void resetFileData() {
        this.data = new Data();

        this.jsonObject = getEmptyJsonObject();
        this.saveData();
        notifyDataChange();
    }
    private JSONObject getEmptyJsonObject() {
        JSONObject jsonObject = new JSONObject();
        JSONArray cameras = new JSONArray();
        try {
            jsonObject.put(JSONCamerasDataArray, (Object)cameras);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public ArrayList<Camera> getCameraList() {
        if(dataLoaded)
            return data.getCameraList();
        return new ArrayList<>();
    }
    public void addCamera(Camera camera) {
        this.data.addCamera(camera);
        notifyDataChange();
        this.saveData();
    }
    public void deleteCamera(Camera camera) {
        if(camera.getPreviewImg() != null && !camera.getPreviewImg().isEmpty()) {
            File previewImg = new File(settings.getPreviewImagesDir(), camera.getPreviewImg());
            if(previewImg.isFile() && previewImg.exists())
                previewImg.delete();
        }
        data.deleteCamera(camera);
        notifyDataChange();
        this.saveData();
    }
    public void updateCamera(String cameraName, Camera nCamera) {
        Camera camera = getCamera(cameraName);
        if(camera != null) updateCamera(camera, nCamera);
    }
    public void updateCamera(Camera camera, Camera nCamera) {
        camera.update(nCamera);
        camera.notifyCameraUpdated();
        this.saveData();
    }
    public Camera getCamera(String name) {
        for (Camera camera: this.data.getCameraList()) {
            if(camera.getName().equalsIgnoreCase(name))
                return camera;
        }
        return null;
    }

    public void savePreviewImg(final Camera camera, final Bitmap bitmap) {
        Log.d(TAG, "Save preview image");

        if(camera.getPreviewImg() == null || "".equals(camera.getPreviewImg())) {
            final String fileName = settings.getUniquePreviewImageName();
            camera.setPreviewImg(fileName);
        }

        CachedImages.addCachedImage(camera, bitmap);
        Log.d(TAG, "Cached preview image");
        camera.notifyCameraPreviewImgChanged();

        Message msg = ImageSavingThread.createMessage(new ImageSavingThread.Data(camera, bitmap, null));
        if(msg == null) return;
        imageSavingThread.sendMessage(msg);
        Log.d(TAG, "Sended message");
    }
    public void loadPreviewImg(Camera camera, ImageLoadingThread.Callback callback) {
        loadPreviewImg(new ImageLoadingThread.Data(camera, callback));
    }
    public void loadPreviewImg(ImageLoadingThread.Data data) {
        if(data.getCamera().getPreviewImg() == null || "".equals(data.getCamera().getPreviewImg())) {
            Log.d(TAG, data.getCamera().getName());
            return;
        }
        Log.d(TAG, "Loading: " + data.getCamera().getPreviewImg());
        Bitmap bitmap = CachedImages.getCachedBitmap(data.getCamera());
        if(bitmap != null) {
            data.getCallback().onImageLoaded(data, bitmap);
            return;
        }
        Message msg = ImageLoadingThread.createMessage(data);
        if(msg == null) return;
        this.imageLoadingThread.sendMessage(msg);
    }

    public void addOnDataChangedListener(OnDataChanged onDataChangedListener) {
        this.onDataChangedListeners.add(onDataChangedListener);
    }
    public void removeOnDataChangeListener(OnDataChanged onDataChanged) {
        this.onDataChangedListeners.remove(onDataChanged);
    }
    private void notifyDataChange() {
        for (OnDataChanged listener : this.onDataChangedListeners) {
            if(listener == null) {
                this.onDataChangedListeners.remove(listener);
                continue;
            }
            listener.onDataChanged();
        }
    }
    public boolean isDataLoaded() {
        return this.dataLoaded;
    }

    Callback urlTemplatesLoadedCallback;

    public boolean isUrlsTemplatesLoaded() {
        return (this.urlsTemplates.getProducerList() != null);
    }

    public void loadUrlsTemplates(Callback callback) {
        Log.d(TAG, "loadUrlsTemplates");
        this.urlTemplatesLoadedCallback = callback;
        if(!this.urlsTemplates.isDataLoaded() && urlsTemplatesLoadingThread==null) {
            urlsTemplatesLoadingThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "Started");
                    if(!urlsTemplates.load()) {
                        List<Producer> producers = new ArrayList<>();
                        producers.add(new Producer("Default", new Model("Default", new UrlTemplate("rtsp://{user}:{password}@{addressip}:{port}", "rtsp://{addressip}:{port}"))));
                        urlsTemplates.setProducersList(producers);
                    }
                    Log.d(TAG, "End");
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (urlTemplatesLoadedCallback != null)
                                urlTemplatesLoadedCallback.onDataLoaded();
                            urlsTemplatesLoadingThread=null;
                        }
                    });
                }
            });
            urlsTemplatesLoadingThread.start();
        }
    }

    public List<Producer> getProducersList() {
        return this.urlsTemplates.getProducerList();
    }

    public UrlsTemplates getUrlTemplates() {
        return this.urlsTemplates;
    }
}
