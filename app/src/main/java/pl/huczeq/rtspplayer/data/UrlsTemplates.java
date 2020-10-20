package pl.huczeq.rtspplayer.data;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import pl.huczeq.rtspplayer.data.objects.urls.Producer;

public class UrlsTemplates {

    private final String TAG = "UrlsTemplates";
    List<Producer> producerList;
    Context context;

    public UrlsTemplates(Context context) {
        this.context = context;
    }

    public List<Producer> getProducerList() {
        return this.producerList;
    }

    public void setProducersList(List<Producer> producers) {
        this.producerList = producers;
    }

    public boolean load() {
        Log.d(TAG, "Start");
        List<Producer> producers = new ArrayList<>();
        AssetManager assetManager = context.getAssets();
        BufferedReader reader;
        StringBuilder string = new StringBuilder();
        try {
            InputStream is = assetManager.open("urlsTemplates.json");
            reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                string.append(line);
            }
            reader.close();
            is.close();

            JSONObject jsonObject = new JSONObject(string.toString());
            JSONArray array = jsonObject.getJSONArray("producers");
            Log.d(TAG, "JSONArray->length : " + array.length());
            for(int i = 0; i < array.length(); i++) {
                Producer producer = new Producer(array.getJSONObject(i));
                if(producer.isCorrect()) producers.add(producer);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "Exception");
            return false;
        }
        Log.d(TAG, String.valueOf(producers.size()));
        if(producers.size() == 0) {
            this.producerList = null;
            return false;
        }
        this.producerList = producers;
        return true;
    }

    public int getProducerIndex(String name) {
        for(int i = 0; i<this.producerList.size(); i++) {
            if(this.producerList.get(i).getName().equals(name)) return i;
        }
        return -1;
    }

    public boolean isDataLoaded() {
        return (this.producerList!=null);
    }

    public interface Callback {
        boolean isEmptyAuth();
        String getUser();
        String getPassword();
        String getAddressIp();
        String getPort();
        String getChannel();
        String getServerUrl();
        int getStream();
    }
    public static class AdditionalField {
        public static final int Channel = 1;
        public static final int Stream = 2;
        public static final int ServerUrl = 3;
    }
}
