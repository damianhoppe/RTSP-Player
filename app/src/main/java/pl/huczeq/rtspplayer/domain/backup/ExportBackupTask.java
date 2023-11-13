package pl.huczeq.rtspplayer.domain.backup;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import pl.huczeq.rtspplayer.Settings;
import pl.huczeq.rtspplayer.data.model.Camera;
import pl.huczeq.rtspplayer.data.model.CameraGroup;
import pl.huczeq.rtspplayer.data.model.CameraInstance;
import pl.huczeq.rtspplayer.data.model.CameraPattern;
import pl.huczeq.rtspplayer.data.repositories.base.CameraRepository;

public class ExportBackupTask implements Runnable {

    private CameraRepository cameraRepository;
    private Settings settings;

    private OutputStream outputStream;
    private boolean exportCameras;
    private boolean exportSettings;

    @Inject
    public ExportBackupTask(CameraRepository cameraRepository, Settings settings) {
        this.cameraRepository = cameraRepository;
        this.settings = settings;
    }

    public void init(OutputStream outputStream, boolean exportCameras, boolean exportSettings) {
        this.outputStream = outputStream;
        this.exportCameras = exportCameras;
        this.exportSettings = exportSettings;
    }

    @Override
    public void run() {
        assert this.outputStream != null;
        JsonObject backup = new JsonObject();
        backup.addProperty(Keys.VERSION, 2);
        if(this.exportCameras) {
            backup.addProperty(Keys.V2.CAMERA_GROUPS_VERSION, 1);
            backup.add(Keys.V2.CAMERA_GROUPS, cameraGroupsToJson());
        }
        if(this.exportSettings) {
            backup.add(Keys.SETTINGS, settingsToJson());
        }
        PrintWriter writer = new PrintWriter(this.outputStream);
        writer.write(backup.toString());
        writer.flush();
        writer.close();
    }

    private JsonElement settingsToJson() {
        JsonObject jsonSettings = new JsonObject();
        if(exportCameras && settings.isAppStartCameraEnabled()) {
            int cameraIndex = -1;
            List<Camera> cameras = cameraRepository.getAllCameras();
            for(int i = 0; i < cameras.size(); i++) {
                if(cameras.get(i).getCameraInstance().getId() != settings.getAppStartCameraId())
                    continue;
                cameraIndex = i;
                break;
            }
            if(cameraIndex != -1)
                jsonSettings.addProperty(Keys.V2.Settings.APP_START_CAMERA_INDEX, cameraIndex);
        }
        jsonSettings.addProperty(Settings.KEY.APP_START_CAMERA_ENABLED, settings.isAppStartCameraEnabled());
        jsonSettings.addProperty(Settings.KEY.APP_START_CAMERA_ID, settings.getAppStartCameraId());

        jsonSettings.addProperty(Settings.KEY.THEME, settings.getTheme());
        jsonSettings.addProperty(Settings.KEY.DYNAMIC_COLORS, settings.dynamicColorsEnabled());
        jsonSettings.addProperty(Settings.KEY.INITIAL_ORIENTATION, settings.getInitialOrientation());
        jsonSettings.addProperty(Settings.KEY.ORIENTATION_MODE, settings.getOrientationMode());
        jsonSettings.addProperty(Settings.KEY.PLAYBACK_LIBRARY, settings.getPlaybackLibrary());
        jsonSettings.addProperty(Settings.KEY.CACHING_BUFFER_SIZE, settings.getCachingBufferSize());
        jsonSettings.addProperty(Settings.KEY.HARDWARE_ACCELERATION_ENABLED, settings.isEnabledHardwareAcceleration());
        jsonSettings.addProperty(Settings.KEY.MUTE_AUDIO_DEFAULT_ENABLED, settings.isMuteAudioDefaultEnabled());
        jsonSettings.addProperty(Settings.KEY.FORCE_USE_RTSP_TCP_ENABLED, settings.isForceUseRtspTcpEnabled());
        jsonSettings.addProperty(Settings.KEY.AUTO_ENTER_PIP_MODE_ENABLED, settings.autoEnterPipModeEnabled());

        return jsonSettings;
    }

    private JsonElement cameraGroupsToJson() {
        JsonArray jsonCameraGroups = new JsonArray();
        for(CameraGroup cameraGroup : cameraRepository.getAllCameraGroups()) {
            jsonCameraGroups.add(cameraGroupToJson(cameraGroup));
        }
        return jsonCameraGroups;
    }

    private JsonElement cameraGroupToJson(CameraGroup camera) {
        JsonObject cameraJson = new JsonObject();
        cameraJson.add(Keys.V2.CameraGroup.CAMERA_PATTERN, cameraPatternToJson(camera.getCameraPattern()));
        cameraJson.add(Keys.V2.CameraGroup.CAMERA_INSTANCES, cameraInstancesToJson(camera.getCameraInstances()));
        return cameraJson;
    }

    private JsonElement cameraPatternToJson(CameraPattern cameraPattern) {
        JsonObject cameraPatternJson = new JsonObject();
        cameraPatternJson.addProperty(Keys.V2.Camera.NAME, cameraPattern.getName());
        cameraPatternJson.addProperty(Keys.V2.Camera.URL, cameraPattern.getUrl());
        cameraPatternJson.addProperty(Keys.V2.CameraPattern.USERNAME, cameraPattern.getUserName());
        cameraPatternJson.addProperty(Keys.V2.CameraPattern.PASSWORD, cameraPattern.getPassword());
        cameraPatternJson.addProperty(Keys.V2.CameraPattern.IP_ADDRESS, cameraPattern.getAddressIp());
        cameraPatternJson.addProperty(Keys.V2.CameraPattern.PORT, cameraPattern.getPort());
        cameraPatternJson.addProperty(Keys.V2.CameraPattern.CHANNEL, cameraPattern.getChannel());
        cameraPatternJson.addProperty(Keys.V2.CameraPattern.STREAM, cameraPattern.getStream());
        cameraPatternJson.addProperty(Keys.V2.CameraPattern.PRODUCER, cameraPattern.getProducer());
        cameraPatternJson.addProperty(Keys.V2.CameraPattern.MODEL, cameraPattern.getModel());
        cameraPatternJson.addProperty(Keys.V2.CameraPattern.SERVER_URL, cameraPattern.getServerUrl());
        return cameraPatternJson;
    }

    private JsonElement cameraInstancesToJson(List<CameraInstance> cameraInstances) {
        JsonArray cameraInstancesJson = new JsonArray();
        for(CameraInstance cameraInstance : cameraInstances) {
            JsonObject cameraInstanceJson = new JsonObject();
            cameraInstanceJson.addProperty(Keys.V2.Camera.NAME, cameraInstance.getName());
            cameraInstanceJson.addProperty(Keys.V2.Camera.URL, cameraInstance.getUrl());
            cameraInstanceJson.add(Keys.V2.CameraInstance.VARIABLES_DATA, variablesDataToJson(cameraInstance.getVariablesData()));
            cameraInstancesJson.add(cameraInstanceJson);
        }
        return cameraInstancesJson;
    }

    private JsonObject variablesDataToJson(Map<String, String> variablesData) {
        JsonObject dataJson = new JsonObject();
        if(variablesData != null)
            for(Map.Entry<String, String> variable : variablesData.entrySet()) {
                dataJson.addProperty(variable.getKey(), variable.getValue());
            }
        return dataJson;
    }
}
