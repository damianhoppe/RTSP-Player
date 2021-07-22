package pl.huczeq.rtspplayer.data.model.urltemplates;

import static java.lang.annotation.RetentionPolicy.SOURCE;

import androidx.annotation.IntDef;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.annotation.Retention;
import java.util.HashSet;
import java.util.Set;

public class UrlTemplate {

    @IntDef
    @Retention(SOURCE)
    public @interface StreamType {
        int MAIN_STREAM = 0;
        int SUB_STREAM = 1;
    }

    @IntDef
    @Retention(SOURCE)
    public @interface AdditionalFields {
        int Channel = 1;
        int Stream = 2;
        int ServerUrl = 3;
    }

    private String urlTemplateAuth;
    private String urlTemplateNoneAuth;
    Set<Integer> additionalFields;
    private String mainStreamValue = String.valueOf(StreamType.MAIN_STREAM);
    private String subStreamValue = String.valueOf(StreamType.SUB_STREAM);

    public UrlTemplate(String urlAuth, String urlNoneAuth) {
        this.urlTemplateAuth = urlAuth;
        this.urlTemplateNoneAuth = urlNoneAuth;
        this.additionalFields = new HashSet<>();
    }

    public UrlTemplate(JSONObject json) {
        this(json.optString("urlAuth", ""), json.optString("urlNoneAuth", ""));
        try {
            JSONArray array = json.getJSONArray("additionalFields");
            for(int i = 0; i < array.length(); i++) {
                this.additionalFields.add(array.getInt(i));
            }
            this.mainStreamValue = json.optString("mainStream", this.mainStreamValue);
            this.subStreamValue = json.optString("subStream", this.subStreamValue);
        } catch (JSONException e) {
            e.printStackTrace();
            this.urlTemplateAuth = null;
            this.urlTemplateNoneAuth = null;
        }
    }

    public boolean isCorrect() {
        return (!this.urlTemplateAuth.equals("") && !this.urlTemplateNoneAuth.equals(""));
    }

    public boolean useField(int field) {
        return this.additionalFields.contains(field);
    }

    public String getUrlTemplateAuth() {
        return urlTemplateAuth;
    }

    public String getUrlTemplateNoneAuth() {
        return urlTemplateNoneAuth;
    }

    public String getMainStreamValue() {
        return mainStreamValue;
    }

    public String getSubStreamValue() {
        return subStreamValue;
    }

    public void addField(@AdditionalFields int fieldId) {
        this.additionalFields.add(fieldId);
    }
}