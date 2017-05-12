package nl.sense_os.googlefit.awareness.apis;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.googlefit.awareness.receivers.GeofenceReceiver;

import static nl.sense_os.googlefit.constant.Preference.IS_SENSE_HQ_ALREADY_REGISTERED_KEY;
import static nl.sense_os.googlefit.entities.GeofenceLocation.SENSE_ID_HQ_LOCATION;
import static nl.sense_os.googlefit.entities.GeofenceLocation.SENSE_NL_HQ_LOCATION;

/**
 * Created by panjiyudasetya on 5/12/17.
 */

public class MonitoringGeofenceApiHelper {
    @SuppressWarnings("SpellCheckingInspection")
    private static final float GEOFENCE_RADIUS_IN_METERS = 100;
    private static final List<Geofence> SENSE_HQ_GEO_LOCATIONS = new ArrayList<>(2);
    @SuppressWarnings("SpellCheckingInspection")
    private final PendingIntent geofencePendingIntent;
    private final GoogleApiClient googleApiClient;
    private final GeofencingRequest geofencingRequest;
    private Context context;

    public MonitoringGeofenceApiHelper(@NonNull Context context,
                                       @NonNull GoogleApiClient googleApiClient) {
        registerSenseHQLocation();
        this.googleApiClient = googleApiClient;
        this.context = context;
        this.geofencePendingIntent = createGeofenceIntentReceiver();
        this.geofencingRequest = createGeofencingRequest();
    }

    @SuppressWarnings("SpellCheckingInspection")
    private void registerSenseHQLocation() {
        SENSE_HQ_GEO_LOCATIONS.add(
                new Geofence.Builder()
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        // Set the request ID of the geofence. This is a string to identify this
                        // geofence.
                        .setRequestId(SENSE_ID_HQ_LOCATION.name)
                        // Set the circular region of this geofence.
                        .setCircularRegion(
                                SENSE_ID_HQ_LOCATION.latitude,
                                SENSE_ID_HQ_LOCATION.longitude,
                                GEOFENCE_RADIUS_IN_METERS
                        )
                        // Set the transition types of interest. Alerts are only generated for these
                        // transition. We track entry and exit transitions in this sample.
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                                Geofence.GEOFENCE_TRANSITION_EXIT)
                        .setLoiteringDelay((int) SENSE_ID_HQ_LOCATION.getLoiteringDelay())
                        .build()
        );
        SENSE_HQ_GEO_LOCATIONS.add(
                new Geofence.Builder()
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .setRequestId(SENSE_NL_HQ_LOCATION.name)
                        // Set the request ID of the geofence. This is a string to identify this
                        // geofence.
                        .setRequestId(SENSE_NL_HQ_LOCATION.name)
                        // Set the circular region of this geofence.
                        .setCircularRegion(
                                SENSE_NL_HQ_LOCATION.latitude,
                                SENSE_NL_HQ_LOCATION.longitude,
                                GEOFENCE_RADIUS_IN_METERS
                        )
                        // Set the transition types of interest. Alerts are only generated for these
                        // transition. We track entry and exit transitions in this sample.
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                                Geofence.GEOFENCE_TRANSITION_EXIT)
                        .setLoiteringDelay((int) SENSE_NL_HQ_LOCATION.getLoiteringDelay())
                        .build()
        );
    }

    /**
     * Adds geofences, which sets alerts to be notified when the device enters or exits one of the
     * specified geofences. Handles the success or failure results returned by addGeofences().
     */
    @SuppressWarnings({"SpellCheckingInspection", "MissingPermission"})
    public void addSenseHQGeofences() throws SecurityException {
        if (!googleApiClient.isConnected()) return;
        if (Hawk.get(IS_SENSE_HQ_ALREADY_REGISTERED_KEY, false)) return;

        LocationServices.GeofencingApi.addGeofences(
                googleApiClient,
                // The GeofenceRequest object.
                geofencingRequest,
                // A pending intent that that is reused when calling removeGeofences(). This
                // pending intent is used to generate an intent when a matched geofence
                // transition is observed.
                geofencePendingIntent
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (status.isSuccess()) Hawk.put(IS_SENSE_HQ_ALREADY_REGISTERED_KEY, true);
            }
        }); // Result processed in onResult().
    }

    @SuppressWarnings("SpellCheckingInspection")
    private PendingIntent createGeofenceIntentReceiver() {
        Intent geofencingIntentReceiver = new Intent(context, GeofenceReceiver.class);
        return PendingIntent.getBroadcast(context, 0, geofencingIntentReceiver, 0);
    }
    /**
     * Builds and returns a GeofencingRequest. Specifies the list of geofences to be monitored.
     * Also specifies how the geofence notifications are initially triggered.
     */
    @SuppressWarnings("SpellCheckingInspection")
    private GeofencingRequest createGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        // Add the geofences to be monitored by geofencing service.
        builder.addGeofences(SENSE_HQ_GEO_LOCATIONS);

        // Return a GeofencingRequest.
        return builder.build();
    }

}