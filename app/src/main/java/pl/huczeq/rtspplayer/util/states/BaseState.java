package pl.huczeq.rtspplayer.util.states;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class BaseState {

    @ProcessingStateType
    protected int type;

    public void reset() {
        this.type = ProcessingStateType.IDLE;
    }
}
