package nl.sense_os.googlefit.eventbus;

import android.support.annotation.NonNull;

import java.util.List;

import nl.sense_os.googlefit.entities.Content;

/**
 * Created by panjiyudasetya on 5/8/17.
 */

public class DetectedStepsCountEvent {
    private List<Content> contents;

    public DetectedStepsCountEvent(@NonNull List<Content> contents) {
        this.contents = contents;
    }

    public List<Content> getContents() {
        return contents;
    }
}
