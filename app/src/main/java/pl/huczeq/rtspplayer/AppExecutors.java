package pl.huczeq.rtspplayer;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.CallSuper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AppExecutors {

    protected final AppExecutor diskIO;
    protected final AppExecutor dbIO;
    protected final AppExecutor bgThread;
    protected final AppExecutor mainThread;

    public AppExecutors() {
        this.diskIO = new AppExecutor(Executors.newFixedThreadPool(1));
        this.dbIO = new AppExecutor(Executors.newFixedThreadPool(1));
        this.bgThread = new AppExecutor(Executors.newFixedThreadPool(1));
        this.mainThread = new AppExecutor(new MainThreadExecutor());
    }
    
    protected AppExecutors(Executor mainThreadExecutor) {
        this.diskIO = new AppExecutor(Executors.newFixedThreadPool(1));
        this.dbIO = new AppExecutor(Executors.newFixedThreadPool(1));
        this.bgThread = new AppExecutor(Executors.newFixedThreadPool(1));
        this.mainThread = new AppExecutor(mainThreadExecutor);
    }

    public AppExecutor diskIO() {
        return diskIO;
    }

    public AppExecutor dbIO() {
        return dbIO;
    }

    public AppExecutor bgThread() {
        return bgThread;
    }

    public AppExecutor mainThread() {
        return mainThread;
    }

    private static class MainThreadExecutor implements Executor {
        private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(Runnable runnable) {
            mainThreadHandler.post(runnable);
        }
    }

    public static class AppExecutor implements Executor{
        private Executor executor;
        private Scheduler scheduler;

        public AppExecutor(Executor executor) {
            this.executor = executor;
            this.scheduler = Schedulers.from(executor);
        }

        public Executor executor() {
            return this.executor;
        }

        public Scheduler scheduler() {
            return this.scheduler;
        }

        @Override
        public void execute(Runnable runnable) {
            this.executor.execute(runnable);
        }
    }
}
