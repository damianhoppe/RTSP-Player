package pl.huczeq.rtspplayer.domain.urlgenerator;

import java.util.Objects;

import pl.huczeq.rtspplayer.data.model.CameraPattern;
import pl.huczeq.rtspplayer.data.model.urltemplates.UrlTemplate;
import pl.huczeq.rtspplayer.domain.model.CameraGroupModel;

public interface UrlComponentsProvider {

    static UrlComponentsProvider of(CameraPattern cameraPattern) {
        return new UrlComponentsProvider() {
            @Override
            public boolean usesAuthorization() {
                if(cameraPattern.getUserName() == null)
                    return false;
                return cameraPattern.getUserName().replaceAll(" ", "").length() > 0;
            }

            @Override
            public String getUserName() {
                if(cameraPattern.getUserName() == null)
                    return "";
                return cameraPattern.getUserName().replaceAll(" ", "");
            }

            @Override
            public String getPassword() {
                return Objects.requireNonNullElse(cameraPattern.getPassword(), "");
            }

            @Override
            public String getAddressIp() {
                return cameraPattern.getAddressIp();
            }

            @Override
            public String getPort() {
                return cameraPattern.getPort();
            }

            @Override
            public String getChannel() {
                return cameraPattern.getChannel();
            }

            @Override
            public String getServerUrl() {
                return Objects.requireNonNullElse(cameraPattern.getServerUrl(), "");
            }

            @Override
            public @UrlTemplate.StreamType int getStream() {
                return Objects.equals(String.valueOf(UrlTemplate.StreamType.SUB_STREAM), cameraPattern.getStream())? UrlTemplate.StreamType.SUB_STREAM : UrlTemplate.StreamType.MAIN_STREAM;
            }
        };
    }

    static UrlComponentsProvider of(CameraGroupModel cameraGroupModel) {
        return new UrlComponentsProvider() {
            @Override
            public boolean usesAuthorization() {
                if(cameraGroupModel.getName() == null)
                    return false;
                return cameraGroupModel.getName().replaceAll(" ", "").length() > 0;
            }

            @Override
            public String getUserName() {
                if(cameraGroupModel.getUserName() == null)
                    return "";
                return cameraGroupModel.getUserName().replaceAll(" ", "");
            }

            @Override
            public String getPassword() {
                return Objects.requireNonNullElse(cameraGroupModel.getPassword(), "");
            }

            @Override
            public String getAddressIp() {
                return cameraGroupModel.getAddressIp();
            }

            @Override
            public String getPort() {
                return cameraGroupModel.getPort();
            }

            @Override
            public String getChannel() {
                return cameraGroupModel.getChannel();
            }

            @Override
            public String getServerUrl() {
                return Objects.requireNonNullElse(cameraGroupModel.getServerUrl(), "");
            }

            @Override
            public @UrlTemplate.StreamType int getStream() {
                return cameraGroupModel.getStreamType();
            }
        };
    }

    boolean usesAuthorization();
    String getUserName();
    String getPassword();
    String getAddressIp();
    String getPort();
    String getChannel();
    String getServerUrl();
    @UrlTemplate.StreamType int getStream();
}
