package pl.huczeq.rtspplayer.di;

import android.content.Context;

import androidx.room.Room;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import pl.huczeq.rtspplayer.data.sources.local.database.AppDatabase;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {

    @Provides
    @Singleton
    public AppDatabase appDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(context, AppDatabase.class, "database").build();
    }
}
