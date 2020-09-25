package pl.huczeq.rtspplayer.utils.data;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import pl.huczeq.rtspplayer.interfaces.OnCameraChanged;

public class Camera {
    private String name;
    private String url;
    private String previewImg;

    private String userName;
    private String password;

    private final String JSONName = "name";
    private final String JSONUrl = "url";
    private final String JSONPreviewImg = "previewImg";
    private final String JSONUserName = "userName";
    private final String JSONPassword = "password";

    private ArrayList<OnCameraChanged> onCameraChangedListeners = new ArrayList<>();

    public Camera(String name, String url) {
        this(name, url, "", "",  "");
    }

    public Camera(String name, String url, String userName, String password) {
        this(name, url, userName, password,  null);
    }

    public Camera(String name, String url, String userName, String password, String previewImg) {
        this.name = name;
        this.url = url;
        this.userName = userName;
        this.password = password;
        this.previewImg = previewImg;
    }

    public Camera(JSONObject jsonObject) {
        try {
            this.name = jsonObject.getString(JSONName);
            this.url = jsonObject.getString(JSONUrl);
            this.previewImg = jsonObject.getString(JSONPreviewImg);
            this.userName = jsonObject.getString(JSONUrl);
            this.password = jsonObject.getString(JSONUrl);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(JSONName, this.name);
            jsonObject.put(JSONUrl, this.url);
            jsonObject.put(JSONPreviewImg, this.previewImg);
            jsonObject.put(JSONUserName, this.userName);
            jsonObject.put(JSONPassword, this.password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public Camera(String name)
    {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPreviewImg() {
        return previewImg;
    }

    public void setPreviewImg(String previewImg) {
        this.previewImg = previewImg;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void addOnCameraChangedListener(OnCameraChanged onCameraChanged) {
        this.onCameraChangedListeners.add(onCameraChanged);
    }

    public void removeOnCameraChangedListener(OnCameraChanged onCameraChanged) {
        this.onCameraChangedListeners.remove(onCameraChanged);
    }

    public void notifyCameraPreviewImgChanged() {
        for (OnCameraChanged listener : this.onCameraChangedListeners) {
            if(listener == null) {
                this.onCameraChangedListeners.remove(listener);
                continue;
            }
            listener.onCameraPrevImgChanged();
        }
    }
}