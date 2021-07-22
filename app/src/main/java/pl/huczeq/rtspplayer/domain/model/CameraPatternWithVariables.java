package pl.huczeq.rtspplayer.domain.model;

import androidx.room.Ignore;

import java.util.Map;

import lombok.ToString;
import pl.huczeq.rtspplayer.data.model.CameraPattern;

@ToString
public class CameraPatternWithVariables extends CameraPattern {

    @Ignore
    private Map<String, String> variables;

    public Map<String, String> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }

}
