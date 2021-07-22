package pl.huczeq.rtspplayer.domain.usecases.base;

import androidx.annotation.CallSuper;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Cancellable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
import io.reactivex.rxjava3.observers.DisposableSingleObserver;
import pl.huczeq.rtspplayer.AppExecutors;

public abstract class CompletableUseCase<Params> extends DisposableUseCase {

    public CompletableUseCase(AppExecutors executors) {
        super(executors);
    }

    protected abstract Completable buildObservable(Params params);

    private Completable buildCompletable(Params params) {
        return buildObservable(params)
                .subscribeOn(executor().scheduler())
                .observeOn(postExecutor().scheduler());
    }

    @CallSuper
    public Completable execute(Params params, CompletableObserver observer) {
        Completable observable = buildCompletable(params);
        if(observer != null)
            observable.subscribe(observer);
        return observable;
    }

    @CallSuper
    public Completable execute(Params params, DisposableCompletableObserver observer) {
        Completable observable = buildCompletable(params);
        if(observer != null)
            disposables.add(observable.subscribeWith(observer));
        return observable;
    }
}
