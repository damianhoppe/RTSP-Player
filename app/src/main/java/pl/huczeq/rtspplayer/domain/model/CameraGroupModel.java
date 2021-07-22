package pl.huczeq.rtspplayer.domain.model;

import lombok.Data;
import pl.huczeq.rtspplayer.data.model.urltemplates.Model;
import pl.huczeq.rtspplayer.data.model.urltemplates.Producer;

@Data
public class CameraGroupModel {

    private String name;
    private Producer producer;
    private Model model;
    private String userName;
    private String password;
    private String addressIp;
    private String port;
    private String serverUrl;
    private String channel;
    private int streamType;
    private String url;
}
