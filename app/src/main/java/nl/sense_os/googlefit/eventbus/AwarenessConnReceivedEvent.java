package nl.sense_os.googlefit.eventbus;

import android.support.annotation.NonNull;

/**
 * Created by panjiyudasetya on 5/9/17.
 */

public class AwarenessConnReceivedEvent {
    private Status status;
    private String message;

    public enum Status {
        CONNECTED,
        CONN_SUSPENDED,
        CONN_FAILED,
        SUCCESSFULLY_SUBSCRIBED,
        ALREADY_SUBSCRIBED,
        FAILURE_TO_SUBSCRIBE
    }

    public AwarenessConnReceivedEvent(@NonNull Status status,
                                      @NonNull String message) {
        this.status = status;
        this.message = message;
    }

    public Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
