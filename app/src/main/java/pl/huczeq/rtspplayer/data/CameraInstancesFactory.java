package pl.huczeq.rtspplayer.data;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pl.huczeq.rtspplayer.data.objects.CameraInstance;
import pl.huczeq.rtspplayer.data.objects.CameraPattern;
import pl.huczeq.rtspplayer.data.expression.Expression;
import pl.huczeq.rtspplayer.data.expression.ExpressionParser;

public class CameraInstancesFactory {

    private boolean onlyOneInsance;
    private CameraPattern cameraPattern;

    public CameraInstancesFactory(CameraPattern cameraPattern) {
        this.cameraPattern = cameraPattern;
        this.onlyOneInsance = false;
    }

    public CameraInstancesFactory setOnlyOneInsance(boolean onlyOneInsance) {
        this.onlyOneInsance = onlyOneInsance;
        return this;
    }

    public List<CameraInstance> build() {
        List<CameraInstance> cameraInstances = new ArrayList<>();
        if(this.onlyOneInsance) {
            //TODO
        }else {
            Expression expression = new Expression(cameraPattern.getUrl(), cameraPattern.getVariables());
            List<HashMap<String, Integer>> variations = expression.generatePatternDataList();
            int index = 1;
            for(HashMap<String, Integer> variation : variations) {
                variation.put("i", index);
                variation.put("index", index);
                CameraInstance cameraInstance = new CameraInstance();
                cameraInstance.setName(ExpressionParser.loadDataToExpression(cameraPattern.getName(), variation));
                Log.d("Test", "1: " + cameraPattern.getName() + ", 2: " + cameraInstance.getName() + "/");
                cameraInstance.setUrl(ExpressionParser.loadDataToExpression(expression.getPartialyExpression(), variation));
                cameraInstance.setPatternId(cameraPattern.getId());
                cameraInstance.setPrevImgLastUpdateTime(0);
                cameraInstance.setPatternData(variation);
                cameraInstances.add(cameraInstance);
                index++;
            }
        }
        //cameraInstances.add(new CameraInstance(cameraPattern.getName(), cameraPattern.getUrl(), "", 0, "", -1));
        return cameraInstances;
    }
}
