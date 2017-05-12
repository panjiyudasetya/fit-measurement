package nl.sense_os.googlefit.awareness;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;

import org.greenrobot.eventbus.EventBus;

import nl.sense_os.googlefit.entities.Content;
import nl.sense_os.googlefit.eventbus.AwarenessConnReceivedEvent;
import nl.sense_os.googlefit.eventbus.LocationChangeEvent;
import nl.sense_os.googlefit.helpers.DataCacheHelper;
import nl.sense_os.googlefit.awareness.apis.AwarenessApiHelper;
import nl.sense_os.googlefit.awareness.apis.LocationUpdateApiHelper;
import nl.sense_os.googlefit.awareness.apis.MonitoringGeofenceApiHelper;

import static nl.sense_os.googlefit.eventbus.AwarenessConnReceivedEvent.Status;
import static nl.sense_os.googlefit.constant.Preference.LOCATION_UPDATE_CONTENT_KEY;

/**
 * Created by panjiyudasetya on 5/8/17.
 */

@SuppressWarnings("SpellCheckingInspection")
public class AwarenessIntentService extends IntentService
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static final String TAG = "AWARENESS_SERVICE";
    private static final DataCacheHelper CACHE = new DataCacheHelper();

    private GoogleApiClient mClient;
    private AwarenessApiHelper mAwarenessHelper;
    private MonitoringGeofenceApiHelper mGeofenceHelper;
    private LocationUpdateApiHelper mLocationHelper;

    public static Intent withContext(@NonNull Context context) {
        return new Intent(context, AwarenessIntentService.class);
    }

    public AwarenessIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) { }


    @Override
    public void onCreate() {
        super.onCreate();
        buildFitnessClient();
        mAwarenessHelper = new AwarenessApiHelper(this, mClient);
        mGeofenceHelper = new MonitoringGeofenceApiHelper(this, mClient);
        mLocationHelper = new LocationUpdateApiHelper(this, mClient);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        EventBus.getDefault()
                .post(new AwarenessConnReceivedEvent(
                        Status.CONNECTED,
                        "Connected!")
                );
        if (!mClient.isConnecting() || !mClient.isConnected()) mClient.connect();
        else if (mClient.isConnected()) startAwarenessSensing();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseFitnessClient();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        EventBus.getDefault()
                .post(new AwarenessConnReceivedEvent(
                        Status.CONNECTED,
                        "Connected!")
                );
        startAwarenessSensing();
    }

    @Override
    public void onConnectionSuspended(int clientCode) {
        String message = null;
        // If your connection to the sensor gets lost at some point,
        // you'll be able to determine the reason and react to it here.
        if (clientCode == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
            message = "Connection lost.  Cause: Network Lost.";
            Log.w(TAG, message);
        } else if (clientCode == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
            message = "Connection lost.  Reason: Service Disconnected";
            Log.w(TAG, message);
        }

        if (!TextUtils.isEmpty(message)) {
            EventBus.getDefault()
                    .post(new AwarenessConnReceivedEvent(
                            Status.CONN_SUSPENDED,
                            message)
                    );
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG, "Google Play services connection failed. Cause: "
                + connectionResult.toString());

        EventBus.getDefault()
                .post(new AwarenessConnReceivedEvent(
                        Status.CONN_FAILED,
                        "Exception while connecting to Google Play services: "
                                + connectionResult.getErrorMessage())
                );
    }

    @Override
    public void onLocationChanged(Location location) {
        Content content = new Content(
                Content.LOCATION_UPDATE_TYPE,
                new Content.LocationUpdateBuilder(location).build(),
                System.currentTimeMillis()
        );

        CACHE.save(LOCATION_UPDATE_CONTENT_KEY, content);
        EventBus.getDefault()
                .post(new LocationChangeEvent(
                        CACHE.load(LOCATION_UPDATE_CONTENT_KEY)
                ));
    }

    /**
     * Build a {@link GoogleApiClient} to authenticate the user and allow the application
     * to connect to the Fitness APIs. The included scopes should match the scopes needed
     * by your app (see the documentation for details).
     * Use the {@link GoogleApiClient.OnConnectionFailedListener}
     * to resolve authentication failures (for example, the user has not signed in
     * before, or has multiple accounts and must specify which account to use).
     */
    private void buildFitnessClient() {
        // Create the Google API Client
        if (mClient == null) {
            mClient = new GoogleApiClient.Builder(this)
                    .addApi(Awareness.API)
                    .addApi(ActivityRecognition.API)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
    }

    private void releaseFitnessClient() {
        mLocationHelper.stopLocationUpdates();
        mClient.disconnect();
        mClient = null;
    }

    private void startAwarenessSensing() {
        mAwarenessHelper.requestUpdateActivity();
        mGeofenceHelper.addSenseHQGeofences();
        mLocationHelper.startLocationUpdates(new LocationUpdateApiHelper.OnLocationUpdateFailureListener() {
            @Override
            public void onPermissionNeeded(String message) {
                EventBus.getDefault().post(new LocationChangeEvent(
                        false, message, null
                ));
            }

            @Override
            public void onSettingChangeUnavailable(String message) {
                EventBus.getDefault().post(new LocationChangeEvent(
                        false, message, null
                ));
            }
        });
    }
}
