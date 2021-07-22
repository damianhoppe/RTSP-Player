package pl.huczeq.rtspplayer.di;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import pl.huczeq.rtspplayer.AppExecutors;

@Module
@InstallIn(SingletonComponent.class)
public class ExecutorsModule {

    @Provides
    @Singleton
    public AppExecutors executors() {
        return new AppExecutors();
    }
}
