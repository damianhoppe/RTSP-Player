package pl.huczeq.rtspplayer.domain.cameragenerator;

import java.util.List;

import pl.huczeq.rtspplayer.data.model.CameraGroup;
import pl.huczeq.rtspplayer.data.model.CameraInstance;
import pl.huczeq.rtspplayer.data.model.CameraPattern;
import pl.huczeq.rtspplayer.domain.model.CameraPatternWithVariables;
import pl.huczeq.rtspplayer.domain.model.CameraGroupModel;

public class CameraGroupGenerator {


    public static final String TAG = CameraGroupGenerator.class.getSimpleName();

    public CameraGroup generate(CameraGroupModel cameraGroupModel) {
        CameraPattern cameraPattern = CameraPatternMapper.toCameraPattern(cameraGroupModel);
        CameraPatternWithVariables cameraPatternWithVariables = CameraPatternMapper.toPatternWithVariables(cameraPattern, cameraGroupModel.getModel()!=null? cameraGroupModel.getModel().getUrlTemplate() : null);

        CameraGroup cameraGroup = new CameraGroup(cameraPattern);

        CameraInstancesGenerator instancesFactory = new CameraInstancesGenerator(cameraPatternWithVariables);
        List<CameraInstance> instancesGenerated = instancesFactory.build();
        cameraGroup.setCameraInstances(instancesGenerated);
        cameraGroup.getCameraPattern().setNumberOfInstances(instancesGenerated.size());
        return cameraGroup;
    }
}