package pl.huczeq.rtspplayer.utils.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

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


public class DataManager {

    private static DataManager instance;

    public static DataManager getInstance(Context context) {
        if(DataManager.instance == null)
            DataManager.instance = new DataManager(context);
        return DataManager.instance;
    }

    private final String TAG = "Data";

    private final String fileName = "data.json";
    private Context context;
    private boolean dataLoaded;
    private Data data;
    private JSONObject jsonObject;

    public final String JSONCamerasDataArray = "camerasData";

    private ArrayList<OnDataChanged> onDataChangedListeners;

    private ImageLoadingThread imageLoadingThread;

    public DataManager(Context context) {
        this.context = context;
        this.dataLoaded = false;
        this.onDataChangedListeners = new ArrayList<>();
        this.imageLoadingThread = new ImageLoadingThread(context);
        this.imageLoadingThread.start();
    }

    public void loadData() {
        this.context.getFilesDir();
        File file = new File(this.context.getFilesDir(), this.fileName);
        if(!file.exists()) {
            Log.d(TAG, "File not exists!");
            resetFileData();
            this.dataLoaded = true;
            return;
        }
        if(!file.canRead()){
            Log.d(TAG, "Data can not be loaded");
            return;
        }

        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while( (line = br.readLine()) != null ) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            jsonObject = new JSONObject(text.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            resetFileData();
        }

        data = new Data();
        try {
            JSONArray array = jsonObject.getJSONArray(JSONCamerasDataArray);
            for(int i = 0; i < array.length(); i++)
            {
                Log.d(TAG, array.get(i).toString());
                Camera camera = new Camera(array.getJSONObject(i));
                data.getCameraList().add(camera);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "Data loaded");
        this.dataLoaded = true;
    }

    public void saveData() {
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
        final Settings settings = Settings.getInstance(context);
        final String fileName = camera.getName() + ".png";
        CachedImages.addCachedImage(camera, bitmap);
        camera.setPreviewImg(camera.getName() + ".png");
        camera.notifyCameraPreviewImgChanged();
        Thread t = new Thread() {
            @Override
            public void run() {
                File f = new File(settings.getPreviewImagesDir(), fileName);
                try {
                    FileOutputStream fouts = new FileOutputStream(f);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fouts);
                    fouts.flush();
                    fouts.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    saveData();
                }
            }
        };
        t.start();
        //notifyDataChange();
    }

    public void loadPreviewImg(Camera camera, ImageLoadingThread.Callback callback) {
        loadPreviewImg(new ImageLoadingThread.Data(camera, callback));
    }
    public void loadPreviewImg(ImageLoadingThread.Data data) {
        if(data.getCamera() == null || data.getCallback() == null || data.getCamera().getPreviewImg() == null) return;

        Bitmap bitmap = CachedImages.getCachedBitmap(data.getCamera());
        if(bitmap != null) {
            data.getCallback().onImageLoaded(data, bitmap);
            return;
        }
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
}
