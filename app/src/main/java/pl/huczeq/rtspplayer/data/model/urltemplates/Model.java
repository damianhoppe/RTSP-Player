package pl.huczeq.rtspplayer.data.model.urltemplates;

import org.json.JSONException;
import org.json.JSONObject;

public class Model {

    private String name;
    private UrlTemplate urlTemplate;

    public Model(String name, UrlTemplate urlTemplate) {
        this.name = name;
        this.urlTemplate = urlTemplate;
    }

    public Model(JSONObject json) {
        this.name = json.optString("name", "-");
        try {
            this.urlTemplate = new UrlTemplate(json.getJSONObject("urlTemplate"));
            if(!this.urlTemplate.isCorrect()) this.name = null;
        } catch (JSONException e) {
            e.printStackTrace();
            this.name = null;
        }
    }

    public boolean isCorrect() {
        return (this.name!=null);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UrlTemplate getUrlTemplate() {
        return urlTemplate;
    }
}