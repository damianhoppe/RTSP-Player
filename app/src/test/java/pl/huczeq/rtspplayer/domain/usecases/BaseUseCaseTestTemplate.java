package pl.huczeq.rtspplayer.domain.usecases;

import org.junit.Before;

import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.components.SingletonComponent;
import dagger.hilt.testing.TestInstallIn;
import pl.huczeq.rtspplayer.AppExecutors;
import pl.huczeq.rtspplayer.BaseAndroidTest;
import pl.huczeq.rtspplayer.di.ExecutorsModule;

public abstract class BaseUseCaseTestTemplate extends BaseAndroidTest {

    @Inject
    public AppExecutors appExecutors;

    protected TestAppExecutors testAppExecutor;

    @Before
    public void prepareBaseUseCaseTestTemplate() {
        this.testAppExecutor = ((TestAppExecutors) this.appExecutors);
        this.testAppExecutor.replaceMainThread = true;
    }

    public static class TestAppExecutors extends AppExecutors {

        private boolean replaceMainThread = false;

        private AppExecutor fakeMainThreadExecutor;

        public TestAppExecutors() {
            this.fakeMainThreadExecutor = new AppExecutor(Executors.newSingleThreadExecutor());
        }

        @Override
        public AppExecutor mainThread() {
            if(replaceMainThread)
                return fakeMainThreadExecutor;
            return super.mainThread();
        }
    }

    @Module
    @TestInstallIn(components = SingletonComponent.class, replaces = ExecutorsModule.class)
    public static class TestExecutorsModule {

        @Provides
        @Singleton
        public AppExecutors bindAppExecutors() {
            return new TestAppExecutors();
        }
    }
}