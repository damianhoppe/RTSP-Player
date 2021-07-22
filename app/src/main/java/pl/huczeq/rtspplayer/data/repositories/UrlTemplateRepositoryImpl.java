package pl.huczeq.rtspplayer.data.repositories;

import static org.awaitility.Awaitility.await;

import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import pl.huczeq.rtspplayer.AppExecutors;
import pl.huczeq.rtspplayer.data.model.urltemplates.Model;
import pl.huczeq.rtspplayer.data.model.urltemplates.Producer;
import pl.huczeq.rtspplayer.data.model.urltemplates.UrlTemplate;
import pl.huczeq.rtspplayer.data.repositories.base.UrlTemplateRepository;
import pl.huczeq.rtspplayer.data.sources.assets.UrlTemplateAssets;
import pl.huczeq.rtspplayer.util.states.ProcessingStateType;

@Singleton
public class UrlTemplateRepositoryImpl implements UrlTemplateRepository {

    private static final String TAG = "UrlTemplateRepository";

    private MutableLiveData<List<Producer>> producerList;
    private UrlTemplateAssets urlTemplateAssets;
    private @ProcessingStateType int stateType = ProcessingStateType.IDLE;
    private AppExecutors appExecutors;
    private List<Producer> producers;

    @Inject
    public UrlTemplateRepositoryImpl(UrlTemplateAssets urlTemplateAssets, AppExecutors appExecutors) {
        this.producerList = new MutableLiveData<>(null);
        this.urlTemplateAssets = urlTemplateAssets;
        this.appExecutors = appExecutors;
    }

    @Override
    public LiveData<List<Producer>> getAllProducers() {
        if(producerList.getValue() == null)
            requestToLoadProducers();
        return this.producerList;
    }

    protected synchronized void requestToLoadProducers() {
        if(stateType == ProcessingStateType.PROCESSING)
            return;
        stateType = ProcessingStateType.PROCESSING;
        this.urlTemplateAssets.loadProducersInto(new SingleObserver<List<Producer>>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {}

            @Override
            public void onSuccess(@NonNull List<Producer> producers) {
                UrlTemplateRepositoryImpl.this.producers = producers;
                producerList.postValue(producers);
                synchronized (UrlTemplateRepositoryImpl.this) {
                    stateType = ProcessingStateType.DONE;
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {
                e.printStackTrace();
                producerList.postValue(List.of(DefaultUrlTemplateBuilder.buildProducer()));
                synchronized (UrlTemplateRepositoryImpl.this) {
                    stateType = ProcessingStateType.IDLE;
                }
            }
        });
    }

    @Override
    @WorkerThread
    @Deprecated
    public List<Producer> loadAllProducersAndGet() {
        boolean loadingInProgress = false;
        synchronized (this) {
            if(stateType == ProcessingStateType.PROCESSING) {
                loadingInProgress = true;
            }else {
                stateType = ProcessingStateType.PROCESSING;
            }
        }
        if(loadingInProgress) {
            await().until(() -> stateType != ProcessingStateType.DONE);
            return producers;
        }
        producers = this.urlTemplateAssets.loadProducersInfo();
        this.producerList.postValue(producers);
        return producers;
    }

    public static class DefaultUrlTemplateBuilder {

        public static final String NAME = "Default";

        public static UrlTemplate buildUrlTemplate() {
            UrlTemplate urlTemplate = new UrlTemplate("rtsp://{user}:{password}@{addressip}:{port}{serverurl}","rtsp://{addressip}:{port}{serverurl}");
            urlTemplate.addField(UrlTemplate.AdditionalFields.ServerUrl);
            return urlTemplate;
        }

        private static Producer buildProducer() {
            Producer producer = new Producer(NAME);
            producer.setModelList(List.of(new Model(NAME, buildUrlTemplate())));
            return producer;
        }
    }
}
