package pl.huczeq.rtspplayer.domain.cameragenerator;

import pl.huczeq.rtspplayer.data.model.urltemplates.UrlTemplate;
import pl.huczeq.rtspplayer.domain.model.CameraGroupModel;

public class ModelGeneratorForTests {

    public static CameraGroupModel cameraGroupGeneratorWithoutUrlTemplate() {
        CameraGroupModel cameraGroupModel = new CameraGroupModel();
        cameraGroupModel.setName("1");
        cameraGroupModel.setProducer(null);
        cameraGroupModel.setProducer(null);
        cameraGroupModel.setUserName("user");
        cameraGroupModel.setPassword("password");
        cameraGroupModel.setAddressIp("127.0.0.1");
        cameraGroupModel.setPort("554");
        cameraGroupModel.setServerUrl("/Stream");
        cameraGroupModel.setChannel("1");
        cameraGroupModel.setStreamType(UrlTemplate.StreamType.MAIN_STREAM);
        cameraGroupModel.setServerUrl("/Stream");
        cameraGroupModel.setUrl("rtsp://user:password@127.0.0.1:554/Stream");
        return cameraGroupModel;
    }
}
