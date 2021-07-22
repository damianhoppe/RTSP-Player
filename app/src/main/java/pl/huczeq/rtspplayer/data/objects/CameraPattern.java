package pl.huczeq.rtspplayer.data.objects;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Collection;
import java.util.HashMap;

import pl.huczeq.rtspplayer.common.Utils;
import pl.huczeq.rtspplayer.data.expression.Variable;

@Entity
public class CameraPattern {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String url;

    private String userName;
    private String password;
    private String addressIp;
    private String port;
    private String channel;
    private String stream;
    private String producer;
    private String model;
    private String serverUrl;

    private String jsonVariables;
    private int numberOfInstances;

    @Ignore
    private HashMap<String, String> tempVariables;

    @Ignore
    public CameraPattern() { }

    public CameraPattern(String name, String url, String userName, String password, String addressIp, String port, String channel, String stream, String producer, String model, String serverUrl, String jsonVariables, int numberOfInstances) {
        this.name = name;
        this.url = url;
        this.userName = userName;
        this.password = password;
        this.addressIp = addressIp;
        this.port = port;
        this.channel = channel;
        this.stream = stream;
        this.producer = producer;
        this.model = model;
        this.serverUrl = serverUrl;
        this.jsonVariables = jsonVariables;
        this.numberOfInstances = numberOfInstances;
    }

    public void update(CameraPattern cameraPattern) {
        this.name = cameraPattern.name;
        this.url = cameraPattern.url;
        this.userName = cameraPattern.userName;
        this.password = cameraPattern.password;
        this.addressIp = cameraPattern.addressIp;
        this.port = cameraPattern.port;
        this.channel = cameraPattern.channel;
        this.stream = cameraPattern.stream;
        this.producer = cameraPattern.producer;
        this.model = cameraPattern.model;
        this.serverUrl = cameraPattern.serverUrl;
        this.jsonVariables = cameraPattern.jsonVariables;
        this.numberOfInstances = cameraPattern.numberOfInstances;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getJsonVariables() {
        return jsonVariables;
    }

    public void setJsonVariables(String jsonVariables) {
        this.jsonVariables = jsonVariables;
        this.tempVariables = null;
    }

    public int getNumberOfInstances() {
        return numberOfInstances;
    }

    public void setNumberOfInstances(int numberOfInstances) {
        this.numberOfInstances = numberOfInstances;
    }

    public HashMap<String, String> getVariables() {
        if(this.tempVariables == null && this.jsonVariables != null) {
            JSONArray jsonArray = null;
            try {
                jsonArray = new JSONArray(this.jsonVariables);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            this.tempVariables = Utils.jsonArray2HashMap(jsonArray);
        }
        return tempVariables;
    }

    public void setVariables(Collection<Variable> variables) {
        this.tempVariables = new HashMap<>();
        for(Variable variable : variables) {
            tempVariables.put(variable.getName(), variable.getValue());
        }
        this.jsonVariables = Utils.hashMap2JsonArray(this.tempVariables).toString();
    }

    public void setVariables(HashMap<String, String> variables) {
        this.tempVariables = new HashMap<>(variables);
        this.jsonVariables = Utils.hashMap2JsonArray(this.tempVariables).toString();
    }
}
