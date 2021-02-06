package pl.huczeq.rtspplayer.data.objects.urls;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Producer {

    private final static String TAG = "Producer";
    private String name;
    List<Model> modelList;

    public Producer(String name) {
        this.name = name;
        this.modelList = new ArrayList<>();
    }

    public Producer(String name, Model... models) {
        this(name);
        Collections.addAll(this.modelList, models);
    }

    public Producer(JSONObject json) {
        this(json.optString("name", "-"));
        try {
            JSONArray array = json.getJSONArray("models");
            Log.d(TAG, "JSONArray array.length : " + array.length());
            for(int i = 0; i < array.length(); i++) {
                Model model = new Model(array.getJSONObject(i));
                if(model.isCorrect()) this.modelList.add(model);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean isCorrect() {
        Log.d(TAG, "Producer("+getName()+")->modelList.size : " + this.modelList.size());
        return this.modelList.size()>0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Model> getModelList() {
        return modelList;
    }

    public void setModelList(List<Model> modelList) {
        this.modelList = modelList;
    }

    public int getModelIndex(String name) {
        for(int i = 0; i<this.modelList.size(); i++) {
            if(this.modelList.get(i).getName().equals(name)) return i;
        }
        return -1;
    }
}