package pl.huczeq.rtspplayer.domain.usecases.base;

import pl.huczeq.rtspplayer.AppExecutors;

public abstract class BaseUseCase {

    protected AppExecutors executors;

    public BaseUseCase(AppExecutors executors) {
        this.executors = executors;
    }

    public AppExecutors.AppExecutor postExecutor() {
        return this.executors.mainThread();
    }

    public AppExecutors.AppExecutor executor() {
        return this.executors.bgThread();
    }
}
