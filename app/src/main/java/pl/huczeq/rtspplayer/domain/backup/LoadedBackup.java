package pl.huczeq.rtspplayer.domain.backup;

import com.google.gson.JsonObject;

public class LoadedBackup {

    public final JsonObject jsonObject;
    public final boolean containsSettings;
    public final boolean containsCameras;

    public LoadedBackup(JsonObject jsonObject) {
        this(jsonObject, jsonObject.has(Keys.SETTINGS), jsonObject.has(Keys.V1.CAMERAS) || jsonObject.has(Keys.V2.CAMERA_GROUPS));
    }

    public LoadedBackup(JsonObject jsonObject, boolean containsSettings, boolean containsCameras) {
        this.jsonObject = jsonObject;
        this.containsSettings = containsSettings;
        this.containsCameras = containsCameras;
    }
}
