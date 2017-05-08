package nl.sense_os.googlefit.eventbus;

import nl.sense_os.googlefit.entities.Content;

/**
 * Created by panjiyudasetya on 5/8/17.
 */

public class GeofenceEvent {
    private Content content;

    public GeofenceEvent(Content content) {
        this.content = content;
    }

    public Content getContent() {
        return content;
    }
}
