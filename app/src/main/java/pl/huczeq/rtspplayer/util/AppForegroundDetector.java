package pl.huczeq.rtspplayer.util;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AppForegroundDetector implements Application.ActivityLifecycleCallbacks {
    private int startedActivities = 0;
    private int activitiesStartedAfterBackground = 0;

    private boolean appInBackground = false;
    private boolean returnedFromBackground = false;

    @Inject
    public AppForegroundDetector() {}

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        startedActivities++;

        if (appInBackground) {
            appInBackground = false;
            returnedFromBackground = true;
            activitiesStartedAfterBackground = 1;
        } else if (returnedFromBackground) {
            activitiesStartedAfterBackground++;
        }
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        startedActivities--;

        if (startedActivities == 0) {
            appInBackground = true;
        }
    }

    public boolean isReturnedFromBackground() {
        return returnedFromBackground && activitiesStartedAfterBackground == 1;
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {}

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {}

    @Override
    public void onActivityPaused(@NonNull Activity activity) {}

    @Override
    public void onActivityResumed(@NonNull Activity activity) {}

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {}
}
