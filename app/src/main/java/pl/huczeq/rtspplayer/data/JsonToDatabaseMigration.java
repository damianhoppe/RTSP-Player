package pl.huczeq.rtspplayer.data;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import pl.huczeq.rtspplayer.data.model.CameraGroup;
import pl.huczeq.rtspplayer.data.model.CameraInstance;
import pl.huczeq.rtspplayer.data.model.CameraPattern;
import pl.huczeq.rtspplayer.data.repositories.base.CameraRepository;
import pl.huczeq.rtspplayer.data.repositories.base.UrlTemplateRepository;
import pl.huczeq.rtspplayer.data.sources.local.JsonDataFileLoader;

public class JsonToDatabaseMigration {

    public static void tryRestoreData(File file, CameraRepository cameraRepository, UrlTemplateRepository urlTemplateRepository) {
        JsonDataFileLoader jsonDataManager = new JsonDataFileLoader(file, urlTemplateRepository);
        if(!jsonDataManager.exists()) {
            return;
        }
        try {
            jsonDataManager.loadData();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return;
        }
        ArrayList<JsonDataFileLoader.Camera> cameraList = jsonDataManager.getCameraList();
        if(!cameraList.isEmpty()) {
            List<CameraGroup> cameraGroups = new LinkedList<>();
            for (JsonDataFileLoader.Camera camera : cameraList) {
                CameraGroup cameraGroup = jsonCamera2CameraGroup(camera);
                cameraGroups.add(cameraGroup);
            }
            cameraRepository.insertCameraGroups(cameraGroups);
        }
        if(jsonDataManager.isDataLoaded())
            jsonDataManager.delete();
    }

    private static CameraGroup jsonCamera2CameraGroup(JsonDataFileLoader.Camera camera) {
        CameraGroup cameraGroup = new CameraGroup();

        CameraPattern cameraPattern = new CameraPattern();
        cameraPattern.setNumberOfInstances(1);
        cameraPattern.setName(camera.getName());
        cameraPattern.setUserName(camera.getUserName());
        cameraPattern.setPassword(camera.getPassword());
        cameraPattern.setAddressIp(camera.getAddressIp());
        cameraPattern.setPort(camera.getPort());
        cameraPattern.setProducer(camera.getProducer());
        cameraPattern.setModel(camera.getModel());
        cameraPattern.setServerUrl(camera.getServerUrl());
        cameraPattern.setChannel(camera.getChannel());
        cameraPattern.setStream(camera.getStream());
        cameraPattern.setUrl(camera.getUrl());

        CameraInstance cameraInstance = new CameraInstance();
        cameraInstance.setName(camera.getName());
        cameraInstance.setUrl(camera.getUrl());
        cameraInstance.setPreviewImg(camera.getPreviewImg());

        cameraGroup.setCameraInstances(List.of(cameraInstance));
        cameraGroup.setCameraPattern(cameraPattern);

        return cameraGroup;
    }
}
