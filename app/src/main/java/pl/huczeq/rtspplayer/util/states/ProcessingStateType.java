package pl.huczeq.rtspplayer.util.states;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({ProcessingStateType.IDLE, ProcessingStateType.PROCESSING, ProcessingStateType.DONE})
@Retention(RetentionPolicy.SOURCE)
public @interface ProcessingStateType {
    int IDLE = 0;
    int PROCESSING= 1;
    int DONE = 2;
}