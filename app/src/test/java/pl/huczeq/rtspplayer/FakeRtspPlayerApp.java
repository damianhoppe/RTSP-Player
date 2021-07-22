package pl.huczeq.rtspplayer;

import android.content.Context;

import javax.inject.Inject;

import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import pl.huczeq.rtspplayer.data.sources.cache.ThumbnailCache;
import pl.huczeq.rtspplayer.domain.CameraThumbnailsIntegrityHelper;
import pl.huczeq.rtspplayer.ui.start.DataMigrationViewModel;

public class FakeRtspPlayerApp extends RtspPlayerApp {

    public static RtspPlayerApp get(Context context) {
        return (FakeRtspPlayerApp) context.getApplicationContext();
    }

    @Inject
    public Settings settings;
    @Inject
    public ThumbnailCache thumbnailCache;
    @Inject
    public DataMigrationViewModel dataMigrationManager;
    @Inject
    public AppThemeHelper appThemeHelper;
    @Inject
    public CameraThumbnailsIntegrityHelper cameraThumbnailsIntegrityHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        RxJavaPlugins.setErrorHandler(Throwable::printStackTrace);
        this.appThemeHelper.applyDarkLightTheme();
        this.dataMigrationManager.startProcessing();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        cleanMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        cleanMemory();
    }

    public void cleanMemory() {
        this.thumbnailCache.cleanUp();
    }
}
