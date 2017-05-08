package nl.sense_os.googlefit.helpers.awareness;


import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;

import com.google.android.gms.awareness.fence.FenceState;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.googlefit.R;
import nl.sense_os.googlefit.activities.MainActivity;
import nl.sense_os.googlefit.constant.Preference;
import nl.sense_os.googlefit.entities.Content;
import nl.sense_os.googlefit.eventbus.DetectedActivityEvent;
import nl.sense_os.googlefit.eventbus.GeofenceEvent;
import nl.sense_os.googlefit.helpers.DataCacheHelper;

import static com.google.android.gms.awareness.fence.FenceState.TRUE;
import static nl.sense_os.googlefit.constant.FenceKeys.ENTER_LOCATION;
import static nl.sense_os.googlefit.constant.FenceKeys.EXIT_LOCATION;
import static nl.sense_os.googlefit.constant.FenceKeys.ON_FOOT;
import static nl.sense_os.googlefit.utils.CollectionUtils.sortDesc;
import static nl.sense_os.googlefit.entities.GeofenceLocation.SENSE_ID_HQ_LOCATION;

@SuppressWarnings("SpellCheckingInspection")
public class AwarenessReceiver extends BroadcastReceiver {
    private static final DataCacheHelper CACHE = new DataCacheHelper();
    private static final String GEOFENCE_EVENT_TITLE = "Geofence Event Triggered";
    private static final int GEOFENCE_EVENT_NOTIFICATION_ID = 101;
    private static final String ACTIVITY_RECOGNITION_EVENT_TITLE = "Activity Recognition";
    private static final int ACTIVITY_RECOGNITION_EVENT_NOTIFICATION_ID = 101;
    private static final int ACCEPTANCE_DETECTED_ACT_PERCENTAGE = 75;

    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        if (ActivityRecognitionResult.hasResult(intent)) handleActivityRecognition(intent);
        else handleGeofence(intent);
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

            // Only saving detected activity with confidence value above 75%
            if (confidence >= ACCEPTANCE_DETECTED_ACT_PERCENTAGE) {
                saveActivityEvent(detectedActivity, confidence, notificationId);
            }
        }
    }

    private void handleGeofence(@NonNull Intent intent) {
        FenceState fenceState = FenceState.extract(intent);
        final String fenceKey = fenceState.getFenceKey();
        final int currentState = fenceState.getCurrentState();
        final int previousState = fenceState.getPreviousState();

        if (TextUtils.equals(ON_FOOT, fenceKey)) {
            if (currentState == TRUE && previousState != TRUE) {
                logOnFootEvent();
            }
        } else if (TextUtils.equals(ENTER_LOCATION, fenceKey)) {
            if (currentState == TRUE && previousState != TRUE) {
                logEnterLocationEvent();
            }
        } else if (TextUtils.equals(EXIT_LOCATION, fenceKey)) {
            if (currentState == TRUE && previousState != TRUE) {
                logExitLocationEvent();
            }
        }
    }

    private void logEnterLocationEvent() {
        saveGeofenceEvent(new Content(
                Content.GEOFENCE_TYPE,
                "User entered " + SENSE_ID_HQ_LOCATION.name,
                System.currentTimeMillis()
        ));
    }

    private void logExitLocationEvent() {
        saveGeofenceEvent(new Content(
                Content.GEOFENCE_TYPE,
                "User exited location " + SENSE_ID_HQ_LOCATION.name,
                System.currentTimeMillis()
        ));
    }

    private void logOnFootEvent() {
        saveGeofenceEvent(new Content(
                Content.GEOFENCE_TYPE,
                "User is on foot (walking or running)",
                System.currentTimeMillis()
        ));
    }

    private void saveEvent(@NonNull Content content, @NonNull String key) {
        // Save geofence event
        List<Content> contents = CACHE.load(key);
        if (contents.isEmpty()) contents = new ArrayList<>();
        contents.add(content);
        sortDesc(contents);
        CACHE.save(key, contents);
    }

    private void createNotification(@NonNull String title,
                                    @NonNull String content,
                                    int notificationId,
                                    boolean redirectToMainApp) {
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(mContext)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(content);
        if (redirectToMainApp) {
            // Creates an explicit intent for an Activity in your app
            Intent resultIntent = new Intent(mContext, MainActivity.class);

            // The stack builder object will contain an artificial back stack for the
            // started Activity.
            // This ensures that navigating backward from the Activity leads out of
            // your application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
            // Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(MainActivity.class);
            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            notificationBuilder.setContentIntent(resultPendingIntent);
        }
        NotificationManagerCompat.from(mContext).notify(notificationId, notificationBuilder.build());
    }

    private void saveGeofenceEvent(@NonNull Content content) {
        // Save geofence event
        saveEvent(content, Preference.GEOFENCE_CONTENT_KEY);

        // Broadcast geofence event
        EventBus.getDefault().post(new GeofenceEvent(content));

        // Create notification
        createNotification(GEOFENCE_EVENT_TITLE, content.getContent(), GEOFENCE_EVENT_NOTIFICATION_ID, true);
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
        saveEvent(content, Preference.DETECTED_ACTIVITY_CONTENT_KEY);

        // Broadcast geofence event
        EventBus.getDefault().post(new DetectedActivityEvent(content));

        // Create notification
        createNotification(
                ACTIVITY_RECOGNITION_EVENT_TITLE,
                String.format("Are you %s?", activity.toLowerCase()),
                notificationId,
                false
        );
    }
}
