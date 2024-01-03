package pl.huczeq.rtspplayer.domain.cameragenerator;

import static org.junit.Assert.*;

import static pl.huczeq.rtspplayer.domain.cameragenerator.CameraPatternMapper.REGEX_EXPRESSION;

import com.google.common.base.Strings;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pl.huczeq.rtspplayer.data.model.CameraGroup;
import pl.huczeq.rtspplayer.data.model.CameraInstance;
import pl.huczeq.rtspplayer.data.model.CameraPattern;
import pl.huczeq.rtspplayer.domain.model.CameraGroupModel;

@RunWith(RobolectricTestRunner.class)
public class CameraGroupGeneratorTest {

    private void basicCameraGroupGeneratorResultTest(CameraGroup cameraGroup) {
        assertTrue(cameraGroup.getCameraInstances().size() > 0);
        assertEquals(cameraGroup.getCameraInstances().size(), cameraGroup.getCameraPattern().getNumberOfInstances());
    }

    @Test
    public void cameraGroupGeneration_singleWithoutUrlTemplate() {
        CameraGroupModel cameraGroupModel = ModelGeneratorForTests.cameraGroupGeneratorWithoutUrlTemplate();
        CameraGroupGenerator generator = new CameraGroupGenerator();
        CameraGroup cameraGroup = generator.generate(cameraGroupModel);

        basicCameraGroupGeneratorResultTest(cameraGroup);

        assertEquals(1, cameraGroup.getCameraInstances().size());

        CameraInstance cameraInstance = cameraGroup.getCameraInstances().get(0);
        assertEquals(cameraGroupModel.getName(), cameraInstance.getName());
        assertEquals(cameraGroupModel.getUrl(), cameraInstance.getUrl());

        CameraPattern cameraPattern = cameraGroup.getCameraPattern();
        assertEquals(cameraGroupModel.getName(), cameraPattern.getName());
        assertTrue(Strings.isNullOrEmpty(cameraPattern.getProducer()));
        assertTrue(Strings.isNullOrEmpty(cameraPattern.getModel()));
        assertEquals(cameraGroupModel.getUserName(), cameraPattern.getUserName());
        assertEquals(cameraGroupModel.getPassword(), cameraPattern.getPassword());
        assertEquals(cameraGroupModel.getAddressIp(), cameraPattern.getAddressIp());
        assertEquals(cameraGroupModel.getPort(), cameraPattern.getPort());
        assertEquals(cameraGroupModel.getChannel(), cameraPattern.getChannel());

        assertEquals(cameraGroupModel.getServerUrl(), cameraPattern.getServerUrl());
        assertEquals(cameraGroupModel.getUrl(), cameraPattern.getUrl());
    }

    @Test
    public void cameraGroupGeneration_singleWithUrlTemplate() {
        CameraGroupModel cameraGroupModel = ModelGeneratorForTests.cameraGroupGeneratorWithUrlTemplate();
        CameraGroupGenerator generator = new CameraGroupGenerator();
        CameraGroup cameraGroup = generator.generate(cameraGroupModel);

        basicCameraGroupGeneratorResultTest(cameraGroup);

        assertEquals(1, cameraGroup.getCameraInstances().size());

        CameraInstance cameraInstance = cameraGroup.getCameraInstances().get(0);
        assertEquals(cameraGroupModel.getName(), cameraInstance.getName());
        assertEquals(cameraGroupModel.getUrl(), cameraInstance.getUrl());

        CameraPattern cameraPattern = cameraGroup.getCameraPattern();
        assertEquals(cameraGroupModel.getName(), cameraPattern.getName());
        assertFalse(Strings.isNullOrEmpty(cameraPattern.getProducer()));
        assertFalse(Strings.isNullOrEmpty(cameraPattern.getModel()));
        assertEquals(cameraGroupModel.getUserName(), cameraPattern.getUserName());
        assertEquals(cameraGroupModel.getPassword(), cameraPattern.getPassword());
        assertEquals(cameraGroupModel.getAddressIp(), cameraPattern.getAddressIp());
        assertEquals(cameraGroupModel.getPort(), cameraPattern.getPort());
        assertEquals(cameraGroupModel.getChannel(), cameraPattern.getChannel());

        assertEquals(cameraGroupModel.getServerUrl(), cameraPattern.getServerUrl());
        assertEquals(cameraGroupModel.getUrl(), cameraPattern.getUrl());
    }


    @Test
    public void cameraGroupGeneration_many() {
        String cameraName = "Test";
        String cameraUrl = "rtsp://127.0.0.1:{1-5}";

        CameraGroupModel model = new CameraGroupModel();
        model.setName(cameraName);
        model.setUrl(cameraUrl);
        CameraGroupGenerator generator = new CameraGroupGenerator();
        CameraGroup cameraGroup = generator.generate(model);

        basicCameraGroupGeneratorResultTest(cameraGroup);
        assertEquals(5, cameraGroup.getCameraInstances().size());
        assertEquals(cameraName, cameraGroup.getCameraInstances().get(0).getName());
        assertEquals("rtsp://127.0.0.1:1", cameraGroup.getCameraInstances().get(0).getUrl());
        assertEquals("rtsp://127.0.0.1:5", cameraGroup.getCameraInstances().get(4).getUrl());
    }

    @Test
    public void REGEX_EXPRESSION() {
        String text = "rtsp://127.0.0.1:{1-5}";

        Pattern pattern = Pattern.compile(REGEX_EXPRESSION);
        Matcher matcher = pattern.matcher(text);
        if(!matcher.find()) {
            fail();
        }
    }

    @Test
    public void cameraGroupGeneration_many_VariableInIp() {
        String cameraName = "Test";
        String cameraUrl = "rtsp://127.0.0.2{1-4}:554";

        CameraGroupModel model = new CameraGroupModel();
        model.setName(cameraName);
        model.setUrl(cameraUrl);
        CameraGroupGenerator generator = new CameraGroupGenerator();
        CameraGroup cameraGroup = generator.generate(model);

        basicCameraGroupGeneratorResultTest(cameraGroup);
        assertEquals(4, cameraGroup.getCameraInstances().size());
        assertEquals(cameraName, cameraGroup.getCameraInstances().get(0).getName());
        assertEquals("rtsp://127.0.0.21:554", cameraGroup.getCameraInstances().get(0).getUrl());
        assertEquals("rtsp://127.0.0.24:554", cameraGroup.getCameraInstances().get(3).getUrl());
    }

    @Test
    public void cameraGroupGeneration_many_VariableInIp_With_NumberedNamesAtTheEnd() {
        String cameraName = "Camera {i}";
        String cameraUrl = "rtsp://127.0.0.2{1-4}:554";

        CameraGroupModel model = new CameraGroupModel();
        model.setName(cameraName);
        model.setUrl(cameraUrl);
        CameraGroupGenerator generator = new CameraGroupGenerator();
        CameraGroup cameraGroup = generator.generate(model);

        basicCameraGroupGeneratorResultTest(cameraGroup);
        assertEquals(4, cameraGroup.getCameraInstances().size());
        for(int i = 0; i < cameraGroup.getCameraInstances().size(); i++) {
            assertEquals("Camera " + (i + 1), cameraGroup.getCameraInstances().get(i).getName());
            assertEquals("rtsp://127.0.0.2" + (i+1) + ":554", cameraGroup.getCameraInstances().get(i).getUrl());
        }
    }

    @Test
    public void cameraGroupGeneration_many_VariableInIp_With_NumberedNamesAtTheBeginning() {
        String cameraName = "{i}.";
        String cameraUrl = "rtsp://127.0.0.2{1-4}:554";

        CameraGroupModel model = new CameraGroupModel();
        model.setName(cameraName);
        model.setUrl(cameraUrl);
        CameraGroupGenerator generator = new CameraGroupGenerator();
        CameraGroup cameraGroup = generator.generate(model);

        basicCameraGroupGeneratorResultTest(cameraGroup);
        assertEquals(4, cameraGroup.getCameraInstances().size());
        for(int i = 0; i < cameraGroup.getCameraInstances().size(); i++) {
            assertEquals((i + 1) + ".", cameraGroup.getCameraInstances().get(i).getName());
            assertEquals("rtsp://127.0.0.2" + (i+1) + ":554", cameraGroup.getCameraInstances().get(i).getUrl());
        }
    }
}
