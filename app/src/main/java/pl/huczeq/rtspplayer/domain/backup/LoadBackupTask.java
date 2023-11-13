package pl.huczeq.rtspplayer.domain.backup;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

import javax.inject.Inject;

public class LoadBackupTask implements Callable<LoadedBackup> {
    private InputStream inputStream;

    @Inject
    public LoadBackupTask() {}

    public void init(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public LoadedBackup call() {
        assert this.inputStream != null;
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(new InputStreamReader(inputStream), JsonObject.class);
        if(!jsonObject.has(Keys.SETTINGS) && !jsonObject.has(Keys.V1.CAMERAS) && !jsonObject.has(Keys.V2.CAMERA_GROUPS))
            throw new RuntimeException("Backup file contains no data!");
        return new LoadedBackup(jsonObject);
    }
}
