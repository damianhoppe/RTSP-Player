package pl.huczeq.rtspplayer.util.states;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

import lombok.Getter;

@Getter
public class ResultState<T> extends CompletableState {

    @Nullable
    private T result;

    public ResultState() {
        super(ProcessingStateType.IDLE);
    }

    public ResultState(@ProcessingStateType int type) {
        super(type);
    }

    public ResultState(@NotNull Throwable exception) {
        super(exception);
    }

    public ResultState(@NotNull T result) {
        super(ProcessingStateType.DONE);
        this.result = result;
    }

    @Nullable
    public T getResult() {
        return result;
    }

    public static class Builder {

        public static <T> ResultState<T> idleState() {
            return new ResultState<T>();
        }

        public static <T> ResultState<T> processingState() {
            return new ResultState<T>(ProcessingStateType.PROCESSING);
        }

        public static <T> ResultState<T> successfully(@NotNull T result) {
            return new ResultState<T>(result);
        }

        public static <T> ResultState<T> failed(@NotNull Throwable exception) {
            return new ResultState<T>(exception);
        }
    }
}
