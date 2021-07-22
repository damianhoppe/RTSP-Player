package pl.huczeq.rtspplayer.domain.usecases.base;

import androidx.annotation.CallSuper;

import org.jetbrains.annotations.NotNull;

import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import pl.huczeq.rtspplayer.AppExecutors;

public abstract class NoResultUseCase<Params> extends DisposableUseCase {

    public NoResultUseCase(AppExecutors executors) {
        super(executors);
    }

    @NotNull
    public Scheduler getDefaultScheduler() {
        return executors.dbIO().scheduler();
    }

    protected abstract Runnable buildRunnable(Params params);

    @CallSuper
    public void execute(Params params) {
        this.disposables.add(getDefaultScheduler().scheduleDirect(buildRunnable(params)));
    }
}
