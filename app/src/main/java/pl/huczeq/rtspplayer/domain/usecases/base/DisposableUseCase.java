package pl.huczeq.rtspplayer.domain.usecases.base;

import androidx.annotation.CallSuper;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import pl.huczeq.rtspplayer.AppExecutors;

public abstract class DisposableUseCase extends BaseUseCase {

    protected final CompositeDisposable disposables = new CompositeDisposable();

    public DisposableUseCase(AppExecutors executors) {
        super(executors);
    }

    @CallSuper
    public void dispose() {
        if (!disposables.isDisposed()) {
            disposables.dispose();
        }
    }
}
