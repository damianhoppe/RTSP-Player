package pl.huczeq.rtspplayer.data.objects;

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
    private String addressIp;
    private String port;
    private String channel;
    private String stream;
    private String producer;
    private String model;
    private String serverUrl;

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

    private final String emptyString = "";

    private ArrayList<OnCameraChanged> onCameraChangedListeners = new ArrayList<>();

    public Camera(Camera camera) {
        this.name = camera.name;
        this.url = camera.url;
        this.producer = camera.producer;
        this.model = camera.model;
        this.userName = camera.userName;
        this.password = camera.password;
        this.addressIp = camera.addressIp;
        this.port = camera.port;
        this.channel = camera.channel;
        this.stream = camera.stream;
        this.previewImg = camera.previewImg;
        this.serverUrl = camera.serverUrl;
    }

    public Camera(String name, String url) {
        this(name, url, null, null);
    }

    public Camera(String name, String url, String producer, String model) {
        this(name, url, producer, model,  null, null, null, null, null, null, null,null);
    }

    public Camera(String name, String url, String producer, String model, String userName, String password, String addressIp, String port, String channel, String stream, String previewImg, String serverUrl) {
        this.name = name;
        this.url = url;
        this.producer = producer;
        this.model = model;
        this.userName = userName;
        this.password = password;
        this.addressIp = addressIp;
        this.port = port;
        this.channel = channel;
        this.stream = stream;
        this.previewImg = previewImg;
        this.serverUrl = serverUrl;
    }

    public Camera(JSONObject jsonObject) {
        this.name = jsonObject.optString(JSONName, emptyString);
        this.url = jsonObject.optString(JSONUrl, emptyString);
        this.producer = jsonObject.optString(JSONProducer, emptyString);
        this.model = jsonObject.optString(JSONModel, emptyString);
        this.userName = jsonObject.optString(JSONUserName, emptyString);
        this.password = jsonObject.optString(JSONPassword, emptyString);
        this.addressIp = jsonObject.optString(JSONAddressIP, emptyString);
        this.port = jsonObject.optString(JSONPort, emptyString);
        this.channel = jsonObject.optString(JSONChannel, emptyString);
        this.stream = jsonObject.optString(JSONStream, emptyString);
        this.previewImg = jsonObject.optString(JSONPreviewImg, emptyString);
        this.serverUrl = jsonObject.optString(JSONServerUrl, emptyString);
    }

    public JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            if(!this.emptyString.equals(name)) jsonObject.put(JSONName, this.name);
            if(!this.emptyString.equals(url)) jsonObject.put(JSONUrl, this.url);
            if(!this.emptyString.equals(producer))jsonObject.put(JSONProducer, this.producer);
            if(!this.emptyString.equals(model)) jsonObject.put(JSONModel, this.model);
            if(!this.emptyString.equals(userName)) jsonObject.put(JSONUserName, this.userName);
            if(!this.emptyString.equals(password)) jsonObject.put(JSONPassword, this.password);
            if(!this.emptyString.equals(addressIp)) jsonObject.put(JSONAddressIP, this.addressIp);
            if(!this.emptyString.equals(port)) jsonObject.put(JSONPort, this.port);
            if(!this.emptyString.equals(channel)) jsonObject.put(JSONChannel, this.channel);
            if(!this.emptyString.equals(stream)) jsonObject.put(JSONStream, this.stream);
            if(!this.emptyString.equals(previewImg)) jsonObject.put(JSONPreviewImg, this.previewImg);
            if(!this.emptyString.equals(serverUrl)) jsonObject.put(JSONServerUrl, this.serverUrl);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public void update(Camera camera) {
        if(camera == null) return;
        this.name = camera.name;
        this.url = camera.url;
        this.producer = camera.producer;
        this.model = camera.model;
        this.userName = camera.userName;
        this.password = camera.password;
        this.addressIp = camera.addressIp;
        this.port = camera.port;
        this.channel = camera.channel;
        this.stream = camera.stream;
        this.serverUrl = camera.serverUrl;
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

    public String getAddressIp() {
        return addressIp;
    }

    public void setAddressIp(String addressIp) {
        this.addressIp = addressIp;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getStream() {
        return stream;
    }

    public void setStream(String stream) {
        this.stream = stream;
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
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

    public void notifyCameraUpdated() {
        for (OnCameraChanged listener : this.onCameraChangedListeners) {
            if(listener == null) {
                this.onCameraChangedListeners.remove(listener);
                continue;
            }
            listener.onCameraUpdated();
        }
    }
}