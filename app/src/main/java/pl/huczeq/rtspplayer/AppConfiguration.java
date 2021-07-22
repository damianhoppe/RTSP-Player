package pl.huczeq.rtspplayer;

import android.content.Context;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class AppConfiguration {

    public static String getFullAppVersion() {
        return BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ") - " + BuildConfig.BUILD_TIME;
    }

    private final Context appContext;

    @Inject
    public AppConfiguration(@ApplicationContext Context context) {
        this.appContext = context.getApplicationContext();
    }

    public File getThumbnailDirectory() {
        return this.appContext.getCacheDir();
    }
}
