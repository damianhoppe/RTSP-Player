package pl.huczeq.rtspplayer.data.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Map;

import lombok.ToString;

@Entity
@ToString
public class CameraInstance {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String name;
    private String url;
    private String previewImg;
    private long patternId;

    private Map<String, String> variablesData;

    @Ignore
    public CameraInstance() { }

    public CameraInstance(String name, String url, String previewImg, long patternId) {
        this.name = name;
        this.url = url;
        this.previewImg = previewImg;
        this.patternId = patternId;
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

    public String getPreviewImg() {
        return previewImg;
    }

    public void setPreviewImg(String previewImg) {
        this.previewImg = previewImg;
    }

    public long getPatternId() {
        return patternId;
    }

    public void setPatternId(long patternId) {
        this.patternId = patternId;
    }

    public Map<String, String> getVariablesData() {
        return variablesData;
    }

    public void setVariablesData(Map<String, String> variablesData) {
        this.variablesData = variablesData;
    }
}