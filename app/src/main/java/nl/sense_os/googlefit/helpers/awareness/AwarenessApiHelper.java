package nl.sense_os.googlefit.helpers.awareness;


import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.DetectedActivityFence;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.awareness.fence.LocationFence;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.orhanobut.hawk.Hawk;

import nl.sense_os.googlefit.constant.Preference;
import nl.sense_os.googlefit.entities.GeofenceLocation;

import static nl.sense_os.googlefit.constant.FenceKeys.ENTER_LOCATION;
import static nl.sense_os.googlefit.constant.FenceKeys.EXIT_LOCATION;
import static nl.sense_os.googlefit.constant.FenceKeys.ON_FOOT;

public class AwarenessApiHelper {

    private static final String TAG = "AwarenessApiHelper";

    private final Context context;
    private final GoogleApiClient googleApiClient;
    private final PendingIntent pendingIntent;

    public AwarenessApiHelper(Context context, GoogleApiClient googleApiClient) {
        this.context = context;
        this.googleApiClient = googleApiClient;
        Intent onFootFenceIntent = new Intent(context, AwarenessReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(context, 0, onFootFenceIntent, 0);
    }

    public void addOnFootFence() {
        final AwarenessFence onFootFence = DetectedActivityFence.during(DetectedActivityFence.ON_FOOT);
        Awareness.FenceApi.updateFences(
                googleApiClient,
                new FenceUpdateRequest.Builder().addFence(ON_FOOT, onFootFence, pendingIntent).build()
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (status.isSuccess()) {
                    Log.i(TAG, "OnFoot Fence was successfully registered.");
                } else {
                    Log.e(TAG, "OnFoot Fence could not be registered: " + status);
                }
            }
        });
    }

    @SuppressWarnings("SpellCheckingInspection")
    public void addGeofence(final GeofenceLocation location) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (location == GeofenceLocation.SENSE_ID_HQ_LOCATION
                && Hawk.get(Preference.IS_SENSE_HQ_ALREADY_REGISTERED_KEY, false))
            return;

        final AwarenessFence enterLocationFence = LocationFence.in(
                location.latitude,
                location.longitude,
                location.radius,
                location.getLoiteringDelay()
        );
        final AwarenessFence exitLocationFence = AwarenessFence.not(enterLocationFence);

        final FenceUpdateRequest request = new FenceUpdateRequest.Builder()
                .addFence(ENTER_LOCATION, enterLocationFence, pendingIntent)
                .addFence(EXIT_LOCATION, exitLocationFence, pendingIntent)
                .build();
        Awareness.FenceApi.updateFences(googleApiClient, request).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (status.isSuccess()) {
                    if (location == GeofenceLocation.SENSE_ID_HQ_LOCATION)
                        Hawk.put(Preference.IS_SENSE_HQ_ALREADY_REGISTERED_KEY, true);
                    Log.i(TAG, "Location Fence was successfully registered.");
                } else {
                    Log.e(TAG, "Location Fence could not be registered: " + status);
                }
            }
        });
    }

    public void requestUpdateActivity() {
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                googleApiClient,
                3000,
                pendingIntent
        );
    }
}
