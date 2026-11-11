package pl.huczeq.rtspplayer.util;

public class Event<T> {

    private final T content;
    private boolean handled = false;

    public Event(T content) {
        this.content = content;
    }

    public synchronized T getContentIfNotHandled() {
        if (handled) {
            return null;
        }

        handled = true;
        return content;
    }
}