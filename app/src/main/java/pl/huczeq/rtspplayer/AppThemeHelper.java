package pl.huczeq.rtspplayer;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.color.DynamicColors;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class AppThemeHelper {

    private final Settings settings;

    private final ArrayList<Activity> activities = new ArrayList<>();

    @Inject
    public AppThemeHelper(@ApplicationContext Context context, Settings settings) {
        this.settings = settings;
        RtspPlayerApp app = RtspPlayerApp.get(context);
        app.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
                activities.add(activity);
                if(settings.dynamicColorsEnabled())
                    DynamicColors.applyToActivityIfAvailable(activity);
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
                activities.remove(activity);
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {}
            @Override
            public void onActivityResumed(@NonNull Activity activity) {}
            @Override
            public void onActivityPaused(@NonNull Activity activity) {}
            @Override
            public void onActivityStopped(@NonNull Activity activity) {}
            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {}
        });
    }

    public void applyDarkLightTheme() {
        int newNightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        switch (settings.getTheme()) {
            case Settings.Theme.DARK:
                newNightMode = AppCompatDelegate.MODE_NIGHT_YES;
                break;
            case Settings.Theme.LIGHT:
                newNightMode = AppCompatDelegate.MODE_NIGHT_NO;
                break;
            case Settings.Theme.FOLLOW_SYSTEM:
                break;
        }
        if(newNightMode != AppCompatDelegate.getDefaultNightMode())
            AppCompatDelegate.setDefaultNightMode(newNightMode);
    }

    public void updateActivitiesTheme() {
        for(Activity activity : activities) {
            activity.setTheme(R.style.AppTheme);
            activity.recreate();
            if(settings.dynamicColorsEnabled())
                DynamicColors.applyToActivityIfAvailable(activity);
        }
    }
}