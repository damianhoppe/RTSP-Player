package pl.huczeq.rtspplayer.data.repositories;

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

import pl.huczeq.rtspplayer.data.objects.urltemplates.Producer;
import pl.huczeq.rtspplayer.data.utils.DataState;

public class UrlTemplatesRepository extends StateDataRepository {

    private static final String TAG = "UrlTemplatesRepository";

    private List<Producer> producerList;
    private Context context;

    public UrlTemplatesRepository(Context context) {
        super();
        this.context = context.getApplicationContext();
    }

    public void loadData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                state.postValue(DataState.LOADING);
                List<Producer> producers = loadUrlTemplates();
                if(producers.size() == 0) {
                    producerList = null;
                }
                producerList = producers;
                state.postValue(DataState.LOADED);
            }
        }).start();
    }

    private List<Producer> loadUrlTemplates() {
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
            for(int i = 0; i < array.length(); i++) {
                Producer producer = new Producer(array.getJSONObject(i));
                if(producer.isCorrect()) producers.add(producer);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "Loaded " + String.valueOf(producers.size()) + " producers");
        return producers;
    }

    public List<Producer> getProducerList() {
        return this.producerList;
    }

    public interface FormCallback {
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
