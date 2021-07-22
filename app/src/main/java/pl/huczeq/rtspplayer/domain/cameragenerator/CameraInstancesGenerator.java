package pl.huczeq.rtspplayer.domain.cameragenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import pl.huczeq.rtspplayer.data.model.CameraInstance;
import pl.huczeq.rtspplayer.domain.cameragenerator.expression.ExpressionHelper;
import pl.huczeq.rtspplayer.domain.model.CameraPatternWithVariables;
import pl.huczeq.rtspplayer.domain.cameragenerator.expression.Expression;

public class CameraInstancesGenerator {

    private CameraPatternWithVariables cameraPattern;

    public CameraInstancesGenerator(CameraPatternWithVariables cameraPattern) {
        this.cameraPattern = cameraPattern;
    }

    public List<CameraInstance> build() {
        List<CameraInstance> cameraInstances = new ArrayList<>();

        List<String> specialNames = List.of("i");

        Expression expression = new Expression(cameraPattern.getUrl(), cameraPattern.getVariables(), specialNames);
        List<Map<String, String>> variations = expression.generateVariations();
        int index = 1;
        for(Map<String, String> variation : variations) {
            if(Thread.currentThread().isInterrupted())
                throw new RuntimeException("Thread interrupted");
            variation.put("i", String.valueOf(index));

            CameraInstance cameraInstance = new CameraInstance();
            cameraInstance.setName(ExpressionHelper.loadDataToExpression(cameraPattern.getName(), variation, specialNames));
            cameraInstance.setUrl(ExpressionHelper.loadDataToExpression(expression.getPartiallyExpression(), variation));
            cameraInstance.setPatternId(cameraPattern.getId());
            cameraInstance.setVariablesData(variation);
            cameraInstances.add(cameraInstance);
            index++;
        }
        return cameraInstances;
    }
}
