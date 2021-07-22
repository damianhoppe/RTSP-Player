package pl.huczeq.rtspplayer.domain.cameragenerator;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import pl.huczeq.rtspplayer.data.model.CameraPattern;
import pl.huczeq.rtspplayer.domain.cameragenerator.CameraPatternMapper;
import pl.huczeq.rtspplayer.domain.model.CameraPatternWithVariables;
import pl.huczeq.rtspplayer.domain.model.CameraGroupModel;

public class CameraPatternMapperTest {

    @Test
    public void toCameraPattern_namesUnnamedValues() {
        String cameraName = "Test";
        String cameraUrl = "rtsp://{127}.0.0.2{1-4}:{port=554}{i}";

        CameraGroupModel model = new CameraGroupModel();
        model.setName(cameraName);
        model.setUrl(cameraUrl);

        CameraPattern result = CameraPatternMapper.toCameraPattern(model);

        assertEquals("rtsp://{&1=127}.0.0.2{&2=1-4}:{port=554}{i}", result.getUrl());
    }

    @Test
    public void toPatternWithVariables() {
        String cameraName = "Test";
        String cameraUrl = "rtsp://{&1=127}.0.0.2{&2=1-4}:{port=554}{i}";

        CameraPattern model = new CameraPattern();
        model.setName(cameraName);
        model.setUrl(cameraUrl);

        CameraPatternWithVariables result = CameraPatternMapper.toPatternWithVariables(model, null);

        assertEquals("rtsp://{&1}.0.0.2{&2}:{port}{i}", result.getUrl());
        assertEquals("127", result.getVariables().get("&1"));
        assertEquals("1-4", result.getVariables().get("&2"));
        assertEquals("554", result.getVariables().get("port"));
    }

    @Test
    public void fillCameraPattern() {
        String cameraName = "Test";
        String cameraUrl = "rtsp:{i}//{ip1=127}.0.0.2{&1=1-4}:554";

        CameraPattern model = new CameraPattern();
        model.setName(cameraName);
        model.setUrl(cameraUrl);

        Map<String, String> variables = new HashMap<>();
        variables.put("i", "1");
        variables.put("ip1", "127");
        variables.put("&1", "1");

        CameraPattern result = CameraPatternMapper.fillCameraPattern(model, variables);

        assertEquals("rtsp:1//127.0.0.21:554", result.getUrl());
    }

    @Test
    public void hideSpecialVariableNames() {
        String cameraName = "Test";
        String cameraUrl = "rtsp:{i}//{ip1=127}.0.0.2{&1=1}:554";

        CameraPattern model = new CameraPattern();
        model.setName(cameraName);
        model.setUrl(cameraUrl);

        CameraPattern result = CameraPatternMapper.hideSpecialVariableNames(model);

        assertEquals("rtsp:{i}//{ip1=127}.0.0.2{1}:554", result.getUrl());
    }

    @Test
    public void toCameraPattern_defineVarInName_useInUrl() {
        String cameraName = "Test{ip4}";
        String cameraUrl = "rtsp:{i}//{ip1=127}.0.0.2{ip4}:554";

        CameraGroupModel model = new CameraGroupModel();
        model.setName(cameraName);
        model.setUrl(cameraUrl);

        CameraPattern result = CameraPatternMapper.toCameraPattern(model);

        assertEquals("rtsp:{i}//{ip1=127}.0.0.2{ip4}:554", result.getUrl());
    }
}
