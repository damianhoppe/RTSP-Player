package pl.huczeq.rtspplayer.data.objects;

import androidx.room.Embedded;
import androidx.room.Relation;

import pl.huczeq.rtspplayer.data.expression.Expression;
import pl.huczeq.rtspplayer.data.expression.ExpressionHelper;
import pl.huczeq.rtspplayer.data.expression.ExpressionParser;

public class Camera {

    @Embedded
    private CameraInstance cameraInstance;

    @Relation(
            parentColumn = "patternId",
            entityColumn = "id"
    )
    private CameraPattern cameraPattern;

    public Camera(CameraInstance cameraInstance, CameraPattern cameraPattern) {
        this.cameraInstance = cameraInstance;
        this.cameraPattern = cameraPattern;
    }

    public CameraInstance getCameraInstance() {
        return cameraInstance;
    }

    public void setCameraInstance(CameraInstance cameraInstance) {
        this.cameraInstance = cameraInstance;
    }

    public CameraPattern getCameraPattern() {
        return cameraPattern;
    }

    public void setCameraPattern(CameraPattern cameraPattern) {
        this.cameraPattern = cameraPattern;
    }

    public static CameraPattern instance2Pattern(Camera camera) {
        CameraInstance cameraInstance = camera.getCameraInstance();
        CameraPattern cameraPattern = camera.getCameraPattern();
        CameraPattern newCameraPattern = new CameraPattern();
        newCameraPattern.setName(cameraInstance.getName());
        newCameraPattern.setProducer(cameraPattern.getProducer());
        newCameraPattern.setModel(cameraPattern.getModel());
        if(cameraInstance.getPatternData().size() > 0) {
            newCameraPattern.setUserName(ExpressionParser.loadDataToExpression(cameraPattern.getUserName(), cameraInstance.getPatternData()));
            newCameraPattern.setPassword(cameraPattern.getPassword());
            newCameraPattern.setAddressIp(ExpressionParser.loadDataToExpression(cameraPattern.getAddressIp(), cameraInstance.getPatternData()));
            newCameraPattern.setPort(ExpressionParser.loadDataToExpression(cameraPattern.getPort(), cameraInstance.getPatternData()));
            newCameraPattern.setChannel(ExpressionParser.loadDataToExpression(cameraPattern.getChannel(), cameraInstance.getPatternData()));
            newCameraPattern.setStream(ExpressionParser.loadDataToExpression(cameraPattern.getStream(), cameraInstance.getPatternData()));
            newCameraPattern.setServerUrl(ExpressionParser.loadDataToExpression(cameraPattern.getServerUrl(), cameraInstance.getPatternData()));
        }else {
            newCameraPattern.setUserName(cameraPattern.getUserName());
            newCameraPattern.setPassword(cameraPattern.getPassword());
            newCameraPattern.setAddressIp(cameraPattern.getAddressIp());
            newCameraPattern.setPort(cameraPattern.getPort());
            newCameraPattern.setChannel(cameraPattern.getChannel());
            newCameraPattern.setStream(cameraPattern.getStream());
            newCameraPattern.setServerUrl(cameraPattern.getServerUrl());
        }
        newCameraPattern.setUrl(cameraInstance.getUrl());
        return newCameraPattern;
    }
}
