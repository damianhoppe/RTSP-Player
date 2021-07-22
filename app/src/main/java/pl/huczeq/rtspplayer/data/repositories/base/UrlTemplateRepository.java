package pl.huczeq.rtspplayer.data.repositories.base;

import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;

import java.util.List;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleObserver;
import pl.huczeq.rtspplayer.data.model.urltemplates.Producer;

public interface UrlTemplateRepository {

    LiveData<List<Producer>> getAllProducers();

    @WorkerThread
    List<Producer> loadAllProducersAndGet();
}
