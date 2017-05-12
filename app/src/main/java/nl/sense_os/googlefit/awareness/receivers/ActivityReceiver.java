package nl.sense_os.googlefit.awareness.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import nl.sense_os.googlefit.awareness.AwarenessIntentService;
import nl.sense_os.googlefit.constant.Preference;
import nl.sense_os.googlefit.entities.Content;
import nl.sense_os.googlefit.eventbus.DetectedActivityEvent;
import nl.sense_os.googlefit.helpers.DataCacheHelper;

import static nl.sense_os.googlefit.helpers.NotificationHelper.createNotification;

/**
 * Created by panjiyudasetya on 5/12/17.
 */

public class ActivityReceiver extends BroadcastReceiver {
    private static final DataCacheHelper CACHE = new DataCacheHelper();
    private static final String ACTIVITY_RECOGNITION_EVENT_TITLE = "Activity Recognition";
    private static final int ACTIVITY_RECOGNITION_EVENT_NOTIFICATION_ID = 101;
    private static final int ACCEPTANCE_DETECTED_ACT_PERCENTAGE = 80;

    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        if (ActivityRecognitionResult.hasResult(intent)) handleActivityRecognition(intent);
        mContext.startService(AwarenessIntentService.withContext(context));
    }

    private void handleActivityRecognition(@NonNull Intent intent) {
        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
        handleDetectedActivities(result.getProbableActivities());
    }

    private void handleDetectedActivities(List<DetectedActivity> probableActivities) {
        for (DetectedActivity activity : probableActivities) {
            String detectedActivity;
            int confidence = activity.getConfidence();
            int notificationId = ACTIVITY_RECOGNITION_EVENT_NOTIFICATION_ID;
            switch (activity.getType()) {
                case DetectedActivity.IN_VEHICLE:
                    detectedActivity = "In Vehicle";
                    notificationId += 1;
                    break;
                case DetectedActivity.ON_BICYCLE:
                    detectedActivity = "On Bicycle";
                    notificationId += 2;
                    break;
                case DetectedActivity.ON_FOOT:
                    detectedActivity = "On Foot";
                    notificationId += 3;
                    break;
                case DetectedActivity.RUNNING:
                    detectedActivity = "Running";
                    notificationId += 4;
                    break;
                case DetectedActivity.STILL:
                    detectedActivity = "Still";
                    notificationId += 5;
                    break;
                case DetectedActivity.TILTING:
                    detectedActivity = "Tilting";
                    notificationId += 6;
                    break;
                case DetectedActivity.WALKING:
                    detectedActivity = "Walking";
                    notificationId += 7;
                    break;
                case DetectedActivity.UNKNOWN:
                default:
                    detectedActivity = "Unknown";
                    break;
            }

            // Only saving detected activity with confidence value above 80%
            if (confidence >= ACCEPTANCE_DETECTED_ACT_PERCENTAGE) {
                saveActivityEvent(detectedActivity, confidence, notificationId);
            }
        }
    }

    private void saveActivityEvent(@NonNull String activity, int confidence, int notificationId) {
        Content content = new Content(
                Content.ACTIVITY_TYPE,
                new Content.ActivityBuilder()
                        .activity(activity)
                        .confidence(confidence)
                        .recordedTime(System.currentTimeMillis())
                        .build(),
                System.currentTimeMillis()
        );

        // Save activity event
        CACHE.save(Preference.DETECTED_ACTIVITY_CONTENT_KEY, content);

        // Broadcast detected activity event
        EventBus.getDefault()
                .post(new DetectedActivityEvent(
                        CACHE.load(Preference.DETECTED_ACTIVITY_CONTENT_KEY)
                )
        );

        // Create notification
        createNotification(
                mContext,
                ACTIVITY_RECOGNITION_EVENT_TITLE,
                String.format("Are you %s?", activity.toLowerCase()),
                notificationId,
                false
        );
    }
}