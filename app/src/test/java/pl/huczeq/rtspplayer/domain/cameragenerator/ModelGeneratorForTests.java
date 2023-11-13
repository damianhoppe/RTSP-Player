package pl.huczeq.rtspplayer.domain.cameragenerator;

import java.util.ArrayList;
import java.util.List;

import pl.huczeq.rtspplayer.data.model.urltemplates.Model;
import pl.huczeq.rtspplayer.data.model.urltemplates.Producer;
import pl.huczeq.rtspplayer.data.model.urltemplates.UrlTemplate;
import pl.huczeq.rtspplayer.domain.model.CameraGroupModel;
import pl.huczeq.rtspplayer.domain.urlgenerator.UrlComponentsProvider;
import pl.huczeq.rtspplayer.domain.urlgenerator.UrlGenerator;

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

    public static CameraGroupModel cameraGroupGeneratorWithUrlTemplate() {
        CameraGroupModel cameraGroupModel = new CameraGroupModel();
        cameraGroupModel.setName("1");
        cameraGroupModel.setProducer(generateProducerWithModel());
        Model model = cameraGroupModel.getProducer().getModelList().get(0);
        cameraGroupModel.setModel(model);
        cameraGroupModel.setUserName("user");
        cameraGroupModel.setPassword("password");
        cameraGroupModel.setAddressIp("127.0.0.1");
        cameraGroupModel.setPort("554");
        cameraGroupModel.setServerUrl("/Stream");
        cameraGroupModel.setChannel("1");
        cameraGroupModel.setStreamType(UrlTemplate.StreamType.MAIN_STREAM);
        cameraGroupModel.setServerUrl("/Stream");
        cameraGroupModel.setUrl(UrlGenerator.generate(model.getUrlTemplate(), UrlComponentsProvider.of(cameraGroupModel)));
        return cameraGroupModel;
    }

    private static Producer generateProducerWithModel() {
        UrlTemplate urlTemplate = new UrlTemplate("rtsp://{user}:{password}@{addressip}:{port}{serverurl}", "rtsp://{addressip}:{port}{serverurl}");
        urlTemplate.addField(UrlTemplate.AdditionalFields.ServerUrl);

        Model model = new Model("M", urlTemplate);
        Producer producer = new Producer("P");
        List<Model> modelList = new ArrayList<>();
        modelList.add(model);
        producer.setModelList(modelList);
        return producer;
    }
}
