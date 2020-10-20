package pl.huczeq.rtspplayer;

import android.app.Application;

import pl.huczeq.rtspplayer.data.Settings;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Settings.getInstance(this).setTheme();
    }
}
