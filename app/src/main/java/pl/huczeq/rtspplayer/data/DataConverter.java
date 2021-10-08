package pl.huczeq.rtspplayer.data;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pl.huczeq.rtspplayer.data.objects.CameraPattern;
import pl.huczeq.rtspplayer.interfaces.IOnDataUpdated;

public class DataConverter {

    public final static String JSONName = "name";
    public final static String JSONUrl = "url";
    public final static String JSONPreviewImg = "previewImg";
    public final static String JSONUserName = "userName";
    public final static String JSONPassword = "password";
    public final static String JSONAddressIP = "addressIp";
    public final static String JSONPort = "port";
    public final static String JSONChannel = "channel";
    public final static String JSONStream = "stream";
    public final static String JSONProducer = "producer";
    public final static String JSONModel = "model";
    public final static String JSONServerUrl = "serverUrl";
    public final static String JSONVariables = "variables";

    public static boolean jsonFile2Database(DataManager dataManager) {
        Context context = dataManager.getSettings().getAppContext();
        File file = new File(context.getFilesDir(), "data.json");
        if(!file.exists())
            return false;
        if(!file.canRead())
            return false;
        StringBuilder jsonText = new StringBuilder();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String line;
            while((line = bufferedReader.readLine()) != null) {
                jsonText.append(line);
                jsonText.append('\n');
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(jsonText.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        List<CameraPattern> cameraPatterns = new ArrayList<>();
        try {
            JSONArray array = jsonObject.getJSONArray("camerasData");
            for(int i = 0; i < array.length(); i++) {
                CameraPattern cameraPattern = json2CameraPattern(array.getJSONObject(i), context);
                if(cameraPattern != null)
                    cameraPatterns.add(cameraPattern);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(!cameraPatterns.isEmpty()) {
            dataManager.addCamerasSync(cameraPatterns);
        }
        file.delete();
        return true;
    }

    public static boolean jsonFile2DatabaseB(DataManager dataManager) {
        Context context = dataManager.getSettings().getAppContext();
        File file = new File(context.getFilesDir(), "");
        if(!file.exists())
            return false;
        if(!file.canRead())
            return false;
        StringBuilder jsonText = new StringBuilder();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String line;
            while((line = bufferedReader.readLine()) != null) {
                jsonText.append(line);
                jsonText.append('\n');
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(jsonText.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        List<CameraPattern> cameraPatterns = new ArrayList<>();
        try {
            JSONArray array = jsonObject.getJSONArray("camerasData");
            for(int i = 0; i < array.length(); i++) {
                CameraPattern cameraPattern = json2CameraPattern(array.getJSONObject(i), context);
                if(cameraPattern != null)
                    cameraPatterns.add(cameraPattern);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(!cameraPatterns.isEmpty()) {
            dataManager.addCamerasSync(cameraPatterns);
        }
        return true;
    }

    public static CameraPattern json2CameraPattern(JSONObject jsonObject, Context context) {
        CameraPattern cameraPattern = new CameraPattern();
        cameraPattern.setName(jsonObject.optString("name"));
        cameraPattern.setUrl(jsonObject.optString("url"));
        cameraPattern.setProducer(jsonObject.optString("producer"));
        cameraPattern.setModel(jsonObject.optString("model"));
        cameraPattern.setUserName(jsonObject.optString("userName"));
        cameraPattern.setPassword(jsonObject.optString("password"));
        cameraPattern.setAddressIp(jsonObject.optString("addressIp"));
        cameraPattern.setPort(jsonObject.optString("port"));
        cameraPattern.setChannel(jsonObject.optString("channel"));
        cameraPattern.setStream(jsonObject.optString("stream"));
        cameraPattern.setServerUrl(jsonObject.optString("serverUrl"));
        String previewImg = jsonObject.optString("previewImg");
        if(!previewImg.isEmpty()) {
            File filePreviewImg = new File(context.getCacheDir(), previewImg);
            if (filePreviewImg.exists())
                filePreviewImg.delete();
        }
        if(cameraPattern.getName().isEmpty() && cameraPattern.getUrl().isEmpty())
            return null;
        return cameraPattern;
    }
}
