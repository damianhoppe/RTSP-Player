package pl.huczeq.rtspplayer.util.states;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

import lombok.Getter;

@Getter
public class CompletableState extends BaseState {

    @Nullable
    protected Throwable exception;

    public CompletableState() {
        super(ProcessingStateType.IDLE);
    }

    public CompletableState(@ProcessingStateType int type) {
        super(type);
    }

    public CompletableState(@NotNull Throwable exception) {
        super(ProcessingStateType.DONE);
        this.exception = exception;
    }

    public boolean isCompletedSuccessfully() {
        return exception == null && type == ProcessingStateType.DONE;
    }

    public boolean isProcessing() {
        return this.type == ProcessingStateType.PROCESSING;
    }

    public boolean isCompleted() {
        return this.type == ProcessingStateType.DONE;
    }

    public static class Builder {

        public static CompletableState IDLE() {
            return new CompletableState();
        }

        public static CompletableState PROCESSING() {
            return new CompletableState(ProcessingStateType.PROCESSING);
        }

        public static CompletableState SUCCESSFULLY() {
            return new CompletableState(ProcessingStateType.DONE);
        }

        public static CompletableState FAILED(@NotNull Throwable exception) {
            return new CompletableState(exception);
        }
    }
}
