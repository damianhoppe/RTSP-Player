package pl.huczeq.rtspplayer.data.sources.local;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import pl.huczeq.rtspplayer.data.repositories.base.UrlTemplateRepository;

public class JsonDataFileLoader {

    public static final String FILE_NAME = "data.json";

    public interface Callback {
        void onDataLoaded();
    }

    public static class Camera {
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

        public Camera(){}

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
    }

    public static class Data {
        ArrayList<Camera> cameraList = new ArrayList<>();

        public ArrayList<Camera> getCameraList() {
            return cameraList;
        }
    }

    private final File file;
    private boolean dataLoaded;
    private Data data;
    private JSONObject jsonObject;

    public final static String JSONCamerasDataArray = "camerasData";
    private UrlTemplateRepository urlTemplateRepository;

    public JsonDataFileLoader(File file, UrlTemplateRepository urlTemplateRepository) {
        this.file = file;
        this.dataLoaded = false;
        this.urlTemplateRepository = urlTemplateRepository;
    }

    public boolean exists() {
        return file.exists();
    }

    public void delete() {
        file.delete();
    }

    public void loadData() throws IOException, JSONException {
        if(!file.exists()) {
            this.dataLoaded = true;
            return;
        }
        if(!file.canRead()){
            this.dataLoaded = false;
            return;
        }

        StringBuilder text = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while ((line = br.readLine()) != null) {
            text.append(line);
            text.append('\n');
        }
        br.close();
        jsonObject = new JSONObject(text.toString());

        data = new Data();

        JSONArray array = jsonObject.getJSONArray(JSONCamerasDataArray);
        if(array.length() == 0) {
            dataLoaded = true;
            return;
        }
        for (int i = 0; i < array.length(); i++) {
            Camera camera = new Camera(array.getJSONObject(i));
            data.getCameraList().add(camera);
        }
        this.dataLoaded = true;
    }

    public ArrayList<Camera> getCameraList() {
        if(data != null)
            return data.getCameraList();
        return new ArrayList<>();
    }

    public boolean isDataLoaded() {
        return this.dataLoaded;
    }
}
