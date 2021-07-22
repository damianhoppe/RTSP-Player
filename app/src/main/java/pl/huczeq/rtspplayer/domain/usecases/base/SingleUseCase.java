package pl.huczeq.rtspplayer.domain.usecases.base;

import androidx.annotation.CallSuper;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.observers.DisposableSingleObserver;
import pl.huczeq.rtspplayer.AppExecutors;

public abstract class SingleUseCase<T, Params> extends DisposableUseCase {

    public SingleUseCase(AppExecutors executors) {
        super(executors);
    }

    protected abstract Single<T> buildObservable(Params params);

    private Single<T> buildSingle(Params params) {
        return buildObservable(params)
                .subscribeOn(executor().scheduler())
                .observeOn(postExecutor().scheduler());
    }

    @CallSuper
    public Single<T> execute(Params params, SingleObserver<T> observer) {
        Single<T> observable = buildSingle(params);
        if(observer != null)
            observable.subscribe(observer);
        return observable;
    }

    @CallSuper
    public Single<T> execute(Params params, DisposableSingleObserver<T> observer) {
        Single<T> observable = buildSingle(params);
        if(observer != null)
            disposables.add(observable.subscribeWith(observer));
        return observable;
    }
}
