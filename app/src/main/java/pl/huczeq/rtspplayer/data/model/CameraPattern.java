package pl.huczeq.rtspplayer.data.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Map;

import lombok.ToString;

@Entity
@ToString
public class CameraPattern {

    @PrimaryKey(autoGenerate = true)
    private long id;
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

    private int numberOfInstances;

    @Ignore
    public CameraPattern() { }

    public CameraPattern(long id, String name, String url, String userName, String password, String addressIp, String port, String channel, String stream, String producer, String model, String serverUrl, int numberOfInstances) {
        this.id = id;
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
        this.numberOfInstances = numberOfInstances;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public int getNumberOfInstances() {
        return numberOfInstances;
    }

    public void setNumberOfInstances(int numberOfInstances) {
        this.numberOfInstances = numberOfInstances;
    }
}