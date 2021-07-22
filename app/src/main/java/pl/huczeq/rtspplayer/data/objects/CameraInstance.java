package pl.huczeq.rtspplayer.data.objects;

import android.util.Log;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;

import pl.huczeq.rtspplayer.common.Utils;

@Entity
public class CameraInstance {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;
    private String url;
    private String previewImg;
    private long prevImgLastUpdateTime;
    private String jsonPatterData;
    private int patternId;

    @Ignore
    private HashMap<String, Integer> tempPatternData;

    @Ignore
    public CameraInstance() { }

    public CameraInstance(String name, String url, String previewImg, long prevImgLastUpdateTime, String jsonPatterData, int patternId) {
        this.name = name;
        this.url = url;
        this.previewImg = previewImg;
        this.prevImgLastUpdateTime = prevImgLastUpdateTime;
        this.jsonPatterData = jsonPatterData;
        this.patternId = patternId;
    }

    public void update(CameraInstance cameraInstance) {
        this.name = cameraInstance.name;
        this.url = cameraInstance.url;
        this.jsonPatterData = cameraInstance.jsonPatterData;
        this.patternId = cameraInstance.patternId;
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

    public String getPreviewImg() {
        return previewImg;
    }

    public void setPreviewImg(String previewImg) {
        this.previewImg = previewImg;
    }

    public long getPrevImgLastUpdateTime() {
        return prevImgLastUpdateTime;
    }

    public void setPrevImgLastUpdateTime(long prevImgLastUpdateTime) {
        this.prevImgLastUpdateTime = prevImgLastUpdateTime;
    }

    public String getJsonPatterData() {
        return jsonPatterData;
    }

    public void setJsonPatterData(String jsonPatterData) {
        this.jsonPatterData = jsonPatterData;
        this.tempPatternData = null;
    }

    public int getPatternId() {
        return patternId;
    }

    public void setPatternId(int patternId) {
        this.patternId = patternId;
    }

    public HashMap<String, Integer> getPatternData() {
        if(this.tempPatternData == null && this.jsonPatterData != null) {
            JSONArray jsonArray = null;
            try {
                jsonArray = new JSONArray(this.jsonPatterData);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            this.tempPatternData = Utils.jsonArray2HashMap(jsonArray);
        }
        return tempPatternData;
    }

    public void setPatternData(HashMap<String, Integer> patternData) {
        if(patternData != null) {
            this.tempPatternData = patternData;
            this.jsonPatterData = Utils.hashMap2JsonArray(patternData).toString();
        }else {
            this.tempPatternData = null;
            this.jsonPatterData = "";
        }
    }
}