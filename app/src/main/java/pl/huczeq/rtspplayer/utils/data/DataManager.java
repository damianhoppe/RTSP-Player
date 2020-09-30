package pl.huczeq.rtspplayer.utils.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import pl.huczeq.rtspplayer.interfaces.OnDataChanged;
import pl.huczeq.rtspplayer.utils.Settings;
import pl.huczeq.rtspplayer.utils.data.threads.ImageLoadingThread;
import pl.huczeq.rtspplayer.utils.data.threads.ImageSavingThread;


public class DataManager {

    public interface Callback {
        void onDataLoaded();
    }

    private static DataManager instance;

    public static DataManager getInstance(Context context) {
        return getInstance(context, null);
    }

    public static DataManager getInstance(Context context, final Callback callback) {
        if(DataManager.instance == null)
            DataManager.instance = new DataManager(context);
        if(!instance.dataLoaded) {
            if(callback == null) {
                instance.loadData();
            }else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        instance.loadData();
                        instance.dataLoaded = true;
                        if(callback != null) notifyDataLoaded(callback);
                    }
                }).start();
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
    private Context context;
    private boolean dataLoaded;
    private Data data;
    private JSONObject jsonObject;

    public final String JSONCamerasDataArray = "camerasData";

    private ArrayList<OnDataChanged> onDataChangedListeners;

    private ImageLoadingThread imageLoadingThread;
    private ImageSavingThread imageSavingThread;

    private Settings settings;

    public DataManager(Context context) {
        this.context = context;
        this.dataLoaded = false;
        this.onDataChangedListeners = new ArrayList<>();
        this.imageLoadingThread = new ImageLoadingThread(context);
        this.imageSavingThread = new ImageSavingThread(context);
        this.settings = Settings.getInstance(context);

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
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                resetFileData();
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
        synchronized (this) {
            File file = new File(this.context.getFilesDir(), this.fileName);
            FileOutputStream stream;
            try {
                stream = new FileOutputStream(file);
                updateJsonObject();
                stream.write(this.jsonObject.toString().getBytes());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

    private void updateJsonObject() {
        JSONArray cameras = new JSONArray();
        for(Camera camera : this.data.getCameraList()) {
            cameras.put(camera.toJSONObject());
        }
        JSONObject newJsonObject = new JSONObject();
        try {
            newJsonObject.put(JSONCamerasDataArray, cameras);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        this.jsonObject = newJsonObject;
    }

    public Data getData()
    {
        return this.data;
    }

    public void addCamera(Camera camera) {
        this.data.getCameraList().add(camera);
        this.saveData();
        notifyDataChange();
    }

    public void savePreviewImg(final Camera camera, final Bitmap bitmap) {
        Log.d(TAG, "Save preview image");
        final String fileName = camera.getName() + ".png";

        camera.setPreviewImg(fileName);
        CachedImages.addCachedImage(camera, bitmap);
        Log.d(TAG, "Cached preview image");
        camera.notifyCameraPreviewImgChanged();

        Message msg = ImageSavingThread.createMessage(new ImageSavingThread.Data(camera, bitmap, null));
        if(msg == null) return;
        imageSavingThread.sendMessage(msg);
        Log.d(TAG, "Sended message");
    }

    public void deleteCamera(Camera camera) {
        data.getCameraList().remove(camera);
        notifyDataChange();
        this.saveData();
    }

    public void updateCamera(String cameraName, Camera nCamera) {
        Camera camera = getCamera(cameraName);
        if(camera != null) updateCamera(camera, nCamera);
    }

    public void updateCamera(Camera camera, Camera nCamera) {
        camera.setName(nCamera.getName());
        camera.setUrl(nCamera.getUrl());
        camera.setUserName(nCamera.getUserName());
        camera.setPassword(nCamera.getPassword());
        camera.notifyCameraUpdated();
        this.saveData();
    }

    public void loadPreviewImg(Camera camera, ImageLoadingThread.Callback callback) {
        loadPreviewImg(new ImageLoadingThread.Data(camera, callback));
    }
    public void loadPreviewImg(ImageLoadingThread.Data data) {
        Log.d(TAG, "Loading: " + data.getCamera().getPreviewImg());
        Bitmap bitmap = CachedImages.getCachedBitmap(data.getCamera());
        if(bitmap != null) {
            data.getCallback().onImageLoaded(data, bitmap);
            return;
        }
        Message msg = ImageLoadingThread.createMessage(data);
        if(msg == null) return;
        this.imageLoadingThread.sendMessage(ImageLoadingThread.createMessage(data));
    }

    public Camera getCamera(String name) {
        for (Camera camera: this.data.getCameraList()) {
            if(camera.getName().equalsIgnoreCase(name))
                return camera;
        }
        return null;
    }

    public ArrayList<Camera> getCameraList() {
        if(dataLoaded)
            return data.getCameraList();
        return new ArrayList<>();
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
}
