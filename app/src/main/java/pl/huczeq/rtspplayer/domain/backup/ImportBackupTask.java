package pl.huczeq.rtspplayer.domain.backup;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import pl.huczeq.rtspplayer.Settings;
import pl.huczeq.rtspplayer.data.model.Camera;
import pl.huczeq.rtspplayer.data.model.CameraGroup;
import pl.huczeq.rtspplayer.data.model.CameraInstance;
import pl.huczeq.rtspplayer.data.model.CameraPattern;
import pl.huczeq.rtspplayer.data.repositories.base.CameraRepository;

public class ImportBackupTask implements Runnable {

    private CameraRepository cameraRepository;
    private Settings settings;


    private LoadedBackup backup;
    private boolean shouldImportCameras;
    private boolean shouldClearCameras;
    private boolean shouldImportSettings;

    @Inject
    public ImportBackupTask(CameraRepository cameraRepository, Settings settings) {
        this.cameraRepository = cameraRepository;
        this.settings = settings;
    }

    public void init(LoadedBackup backup, boolean importCameras, boolean shouldClearCameras, boolean importSettings) {
        this.backup = backup;
        this.shouldImportCameras = importCameras;
        this.shouldClearCameras = shouldClearCameras;
        this.shouldImportSettings = importSettings;
    }

    @Override
    public void run() {
        if(shouldImportCameras)
            importCameras(backup);
        if(shouldImportSettings)
            importSettings(backup);
    }

    private void importSettings(LoadedBackup backup) {
        if(!backup.jsonObject.has(Keys.SETTINGS))
            return;
        JsonObject jsonSettings = backup.jsonObject.getAsJsonObject(Keys.SETTINGS);
        if(jsonSettings != null && jsonSettings.isJsonNull())
            return;
        tryRestoreAppStartCameraIdFromIndex(jsonSettings);
        Settings.tryLoadPreferences(settings, jsonSettings);
    }

    private void tryRestoreAppStartCameraIdFromIndex(JsonObject jsonSettings) {
        Long cameraId = convertAppStartCameraIndexToId(jsonSettings, cameraRepository.getAllCameras());
        if(cameraId == null) {
            jsonSettings.addProperty(Settings.KEY.APP_START_CAMERA_ENABLED, false);
            jsonSettings.addProperty(Settings.KEY.APP_START_CAMERA_ID, false);
        }else {
            jsonSettings.addProperty(Settings.KEY.APP_START_CAMERA_ENABLED, true);
            jsonSettings.addProperty(Settings.KEY.APP_START_CAMERA_ID, cameraId);
        }
    }

    private Long convertAppStartCameraIndexToId(JsonObject jsonSettings, List<Camera> cameras) {
        JsonElement appStartCameraIndex = jsonSettings.get(Keys.V2.Settings.APP_START_CAMERA_INDEX);
        if(appStartCameraIndex == null || appStartCameraIndex.isJsonNull())
            return null;
        int index = appStartCameraIndex.getAsInt();
        if(index < 0 || index >= cameras.size())
            return null;
        return cameras.get(index).getCameraInstance().getId();
    }

    private void importCameras(LoadedBackup backup) {
        List<CameraGroup> cameraGroups = new ArrayList<>();
        if(backup.jsonObject.has(Keys.V1.CAMERAS)) {
            JsonArray camerasJson = backup.jsonObject.getAsJsonArray(Keys.V1.CAMERAS);
            if(camerasJson != null && !camerasJson.isJsonNull())
                loadCameras(camerasJson, cameraGroups);
        }
        if(backup.jsonObject.has(Keys.V2.CAMERA_GROUPS)) {
            JsonArray cameraGroupsJson = backup.jsonObject.getAsJsonArray(Keys.V2.CAMERA_GROUPS);
            if(cameraGroupsJson != null && !cameraGroupsJson.isJsonNull())
                loadCameraGroups(cameraGroupsJson, cameraGroups);
        }
        if(shouldClearCameras) {
            cameraRepository.clearAndInsertCameraGroups(cameraGroups);
        }else {
            cameraRepository.insertCameraGroupsSync(cameraGroups);
        }
    }

    private void loadCameras(JsonArray json, List<CameraGroup> cameraGroups) {
        for(int i = 0; i < json.size(); i++) {
            JsonObject cameraJson = json.get(i).getAsJsonObject();

            CameraInstance cameraInstance = new CameraInstance();
            cameraInstance.setName(cameraJson.get(Keys.V2.Camera.NAME).getAsString());
            cameraInstance.setUrl(cameraJson.get(Keys.V2.Camera.URL).getAsString());

            CameraPattern cameraPattern = new CameraPattern();
            cameraPattern.setName(getAsStringOrNull(cameraJson.get(Keys.V1.Camera.NAME)));
            cameraPattern.setUrl(getAsStringOrNull(cameraJson.get(Keys.V1.Camera.URL)));
            cameraPattern.setUserName(getAsStringOrNull(cameraJson.get(Keys.V1.Camera.USERNAME)));
            cameraPattern.setPassword(getAsStringOrNull(cameraJson.get(Keys.V1.Camera.PASSWORD)));
            cameraPattern.setAddressIp(getAsStringOrNull(cameraJson.get(Keys.V1.Camera.ADDRESS_IP)));
            cameraPattern.setPort(getAsStringOrNull(cameraJson.get(Keys.V1.Camera.PORT)));
            cameraPattern.setChannel(getAsStringOrNull(cameraJson.get(Keys.V1.Camera.CHANNEL)));
            cameraPattern.setStream(getAsStringOrNull(cameraJson.get(Keys.V1.Camera.STREAM)));
            cameraPattern.setProducer(getAsStringOrNull(cameraJson.get(Keys.V1.Camera.PRODUCER)));
            cameraPattern.setModel(getAsStringOrNull(cameraJson.get(Keys.V1.Camera.MODEL)));
            cameraPattern.setServerUrl(getAsStringOrNull(cameraJson.get(Keys.V1.Camera.SERVER_URL)));

            List<CameraInstance> cameraInstances = new ArrayList<>(1);
            cameraInstances.add(cameraInstance);
            cameraGroups.add(new CameraGroup(cameraPattern, cameraInstances));
        }
    }

    private void loadCameraGroups(JsonArray json, List<CameraGroup> cameraGroups) {
        for(int i = 0; i < json.size(); i++) {
            JsonObject cameraGroupJson = json.get(i).getAsJsonObject();
            CameraPattern cameraPattern = loadCameraPattern(cameraGroupJson.getAsJsonObject(Keys.V2.CameraGroup.CAMERA_PATTERN));
            List<CameraInstance> cameraInstances = loadCameraInstances(cameraGroupJson.getAsJsonArray(Keys.V2.CameraGroup.CAMERA_INSTANCES));
            cameraGroups.add(new CameraGroup(cameraPattern, cameraInstances));
        }
    }

    private CameraPattern loadCameraPattern(JsonObject cameraJson) {
        CameraPattern cameraPattern = new CameraPattern();
        cameraPattern.setName(cameraJson.get(Keys.V2.Camera.NAME).getAsString());
        cameraPattern.setUrl(cameraJson.get(Keys.V2.Camera.URL).getAsString());
        cameraPattern.setUserName(getAsStringOrNull(cameraJson.get(Keys.V2.CameraPattern.USERNAME)));
        cameraPattern.setPassword(getAsStringOrNull(cameraJson.get(Keys.V2.CameraPattern.PASSWORD)));
        cameraPattern.setAddressIp(getAsStringOrNull(cameraJson.get(Keys.V2.CameraPattern.IP_ADDRESS)));
        cameraPattern.setPort(getAsStringOrNull(cameraJson.get(Keys.V2.CameraPattern.PORT)));
        cameraPattern.setChannel(getAsStringOrNull(cameraJson.get(Keys.V2.CameraPattern.CHANNEL)));
        cameraPattern.setStream(getAsStringOrNull(cameraJson.get(Keys.V2.CameraPattern.STREAM)));
        cameraPattern.setProducer(getAsStringOrNull(cameraJson.get(Keys.V2.CameraPattern.PRODUCER)));
        cameraPattern.setModel(getAsStringOrNull(cameraJson.get(Keys.V2.CameraPattern.MODEL)));
        cameraPattern.setServerUrl(getAsStringOrNull(cameraJson.get(Keys.V2.CameraPattern.SERVER_URL)));
        return cameraPattern;
    }

    private String getAsStringOrNull(JsonElement json) {
        if(json == null || json.isJsonNull())
            return null;
        return json.getAsString();
    }

    private List<CameraInstance> loadCameraInstances(JsonArray cameraInstancesJson) {
        List<CameraInstance> cameraInstances = new ArrayList<>(cameraInstancesJson.size());
        for(int i = 0; i < cameraInstancesJson.size(); i++) {
            JsonObject cameraInstanceJson = cameraInstancesJson.get(i).getAsJsonObject();

            CameraInstance cameraInstance = new CameraInstance();
            cameraInstance.setName(cameraInstanceJson.get(Keys.V2.Camera.NAME).getAsString());
            cameraInstance.setUrl(cameraInstanceJson.get(Keys.V2.Camera.URL).getAsString());
            cameraInstance.setVariablesData(loadVariablesData(cameraInstanceJson.get(Keys.V2.CameraInstance.VARIABLES_DATA)));
            cameraInstances.add(cameraInstance);
        }
        return cameraInstances;
    }

    private Map<String, String> loadVariablesData(JsonElement json) {
        if(json.isJsonNull())
            return null;
        JsonObject jsonObject = json.getAsJsonObject();
        Map<String, String> variablesData = new HashMap<>();
        for(Map.Entry<String, JsonElement> variableDataJson : jsonObject.entrySet()) {
            String value = variableDataJson.getValue().getAsString();
            if(value.trim().isEmpty())
                continue;
            variablesData.put(variableDataJson.getKey(), value);
        }
        return variablesData;
    }
}
