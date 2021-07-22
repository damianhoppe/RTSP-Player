package pl.huczeq.rtspplayer.data.objects.urltemplates;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import pl.huczeq.rtspplayer.data.repositories.UrlTemplatesRepository;

public class UrlTemplate {

    private String urlTemplateAuth;
    private String urlTemplateNoneAuth;
    private String mainStream = "0";
    private String subStream = "1";
    List<Integer> additionalFields;

    public UrlTemplate(String urlAuth, String urlNoneAuth) {
        this.urlTemplateAuth = urlAuth;
        this.urlTemplateNoneAuth = urlNoneAuth;
        this.additionalFields = new ArrayList<>();
    }

    public UrlTemplate(JSONObject json) {
        this(json.optString("urlAuth", ""), json.optString("urlNoneAuth", ""));
        try {
            JSONArray array = json.getJSONArray("additionalFields");
            for(int i = 0; i < array.length(); i++) {
                this.additionalFields.add(array.getInt(i));
            }
            this.mainStream = json.optString("mainStream", this.mainStream);
            this.subStream = json.optString("subStream", this.subStream);
        } catch (JSONException e) {
            e.printStackTrace();
            this.urlTemplateAuth = null;
            this.urlTemplateNoneAuth = null;
        }
    }

    public boolean isCorrect() {
        return (!this.urlTemplateAuth.equals("") && !this.urlTemplateNoneAuth.equals(""));
    }

    public String getFullUrl(UrlTemplatesRepository.FormCallback callback) {
        if(callback.isEmptyAuth()) {
            return parseTemplateUrl(this.urlTemplateNoneAuth, callback);
        }else {
            return parseTemplateUrl(this.urlTemplateAuth, callback);
        }
    }

    private String parseTemplateUrl(String url, UrlTemplatesRepository.FormCallback callback) {
        StringBuilder fullUrl = new StringBuilder();
        String varName;
        int posS, posE;
        while((posS = url.indexOf("{")) >= 0) {
            posE = url.indexOf("}");
            if(posE < 0)
                return fullUrl.toString();
            fullUrl.append(url.substring(0, posS));
            varName = url.substring(posS+1, posE);
            fullUrl.append(parseVar(varName, callback));
            url = url.substring(posE+1);
        }
        fullUrl.append(url);
        return fullUrl.toString();
    }

    private String parseVar(String variable, UrlTemplatesRepository.FormCallback callback) {
        if(variable.equalsIgnoreCase("user")) {
            return callback.getUser();
        }else if(variable.equalsIgnoreCase("password")) {
            return callback.getPassword();
        }else if(variable.equalsIgnoreCase("addressip")) {
            return callback.getAddressIp();
        }else if(variable.equalsIgnoreCase("port")) {
            return callback.getPort();
        }else if(variable.equalsIgnoreCase("channel")) {
            return callback.getChannel();
        }else if(variable.equalsIgnoreCase("stream")) {
            if(callback.getStream() == 0)
                return this.mainStream;
            return this.subStream;
        }else if(variable.equalsIgnoreCase("serverurl")) {
            return callback.getServerUrl();
        }else {
            return "";
        }
    }

    public boolean useField(int field) {
        return this.additionalFields.contains(field);
    }

    public String getUrlTemplateAuth() {
        return urlTemplateAuth;
    }

    public void setUrlTemplateAuth(String urlTemplateAuth) {
        this.urlTemplateAuth = urlTemplateAuth;
    }

    public String getUrlTemplateNoneAuth() {
        return urlTemplateNoneAuth;
    }

    public void setUrlTemplateNoneAuth(String urlTemplateNoneAuth) {
        this.urlTemplateNoneAuth = urlTemplateNoneAuth;
    }

    public List<Integer> getAdditionalFields() {
        return additionalFields;
    }

    public void setAdditionalFields(List<Integer> additionalFields) {
        this.additionalFields = additionalFields;
    }
}