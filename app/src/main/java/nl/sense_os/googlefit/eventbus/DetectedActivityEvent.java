package nl.sense_os.googlefit.eventbus;

import nl.sense_os.googlefit.entities.Content;

/**
 * Created by panjiyudasetya on 5/8/17.
 */

public class DetectedActivityEvent {
    private Content content;

    public DetectedActivityEvent(Content content) {
        this.content = content;
    }
}
