package pl.huczeq.rtspplayer.data.sources.assets;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.MutableLiveData;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleObserver;
import pl.huczeq.rtspplayer.AppExecutors;
import pl.huczeq.rtspplayer.data.model.urltemplates.Producer;

@Singleton
public class UrlTemplateAssets {

    private final static String TAG = UrlTemplateAssets.class.getSimpleName();

    private Context context;
    private AppExecutors executors;

    @Inject
    public UrlTemplateAssets(@ApplicationContext Context context, AppExecutors executors) {
        this.context = context;
        this.executors = executors;
    }

    @MainThread
    public void loadProducersInto(SingleObserver<List<Producer>> observer) {
        Single.fromCallable(new Callable<List<Producer>>() {
            @Override
            public List<Producer> call() throws Exception {
                return loadProducersInfo();
            }
        }).observeOn(this.executors.diskIO().scheduler())
                .subscribeOn(this.executors.mainThread().scheduler())
                .subscribe(observer);
    }

    @WorkerThread
    public List<Producer> loadProducersInfo() {
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
        return producers;
    }
}
