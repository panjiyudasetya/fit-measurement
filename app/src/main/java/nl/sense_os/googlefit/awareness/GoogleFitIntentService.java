package nl.sense_os.googlefit.awareness;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.data.DataType;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import nl.sense_os.googlefit.constant.Preference;
import nl.sense_os.googlefit.entities.Content;
import nl.sense_os.googlefit.entities.StepsCountResponse;
import nl.sense_os.googlefit.eventbus.AwarenessConnReceivedEvent;
import nl.sense_os.googlefit.eventbus.DetectedStepsCountEvent;
import nl.sense_os.googlefit.helpers.DataCacheHelper;
import nl.sense_os.googlefit.helpers.StepCountHelper;

import static nl.sense_os.googlefit.eventbus.AwarenessConnReceivedEvent.Status;

/**
 * Created by panjiyudasetya on 5/8/17.
 */

@SuppressWarnings("SpellCheckingInspection")
public class GoogleFitIntentService extends IntentService
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "AWARENESS_SERVICE";
    private static final DataCacheHelper CACHE = new DataCacheHelper();

    private GoogleApiClient mClient;
    private StepCountHelper mStepCountHelper;

    public static Intent withContext(@NonNull Context context) {
        return new Intent(context, AwarenessIntentService.class);
    }

    public GoogleFitIntentService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        buildFitnessClient();
        mStepCountHelper = new StepCountHelper(mClient);
    }

    @Override
    protected void onHandleIntent(Intent intent) {  }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!mClient.isConnecting() || !mClient.isConnected()) mClient.connect();
        else if (mClient.isConnected()) getStepsHistory();
        return START_STICKY_COMPATIBILITY;
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
        subscribe();
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
                    // Fitness API
                    .addApi(Fitness.RECORDING_API)
                    .addApi(Fitness.HISTORY_API)
                    .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                    // Geofence & Others Awareness API
                    .addApi(Awareness.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            mClient.connect();
        }
    }

    private void releaseFitnessClient() {
        mClient.disconnect();
        mClient = null;
    }

    /**
     * Record step data by requesting a subscription to background step data.
     */
    private void subscribe() {
        // To create a subscription, invoke the Recording API. As soon as the subscription is
        // active, fitness data will start recording.
        Fitness.RecordingApi
                .subscribe(mClient, DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .setResultCallback(new ResultCallback<com.google.android.gms.common.api.Status>() {
                    @Override
                    public void onResult(com.google.android.gms.common.api.Status status) {
                        if (status.isSuccess()) {
                            if (status.getStatusCode()
                                    == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                                EventBus.getDefault()
                                        .post(new AwarenessConnReceivedEvent(
                                                Status.ALREADY_SUBSCRIBED,
                                                "Existing subscription for activity detected."
                                        ));
                            } else {
                                EventBus.getDefault()
                                        .post(new AwarenessConnReceivedEvent(
                                                Status.SUCCESSFULLY_SUBSCRIBED,
                                                "Successfully subscribed!"
                                        ));
                            }
                        } else {
                            EventBus.getDefault()
                                    .post(new AwarenessConnReceivedEvent(
                                            Status.FAILURE_TO_SUBSCRIBE,
                                            "There was a problem subscribing."
                                    ));
                        }
                    }
                });
    }

    private void getStepsHistory() {
        List<Content> contents = CACHE.load(Preference.STEP_COUNT_CONTENT_KEY);
        postContents(contents);
        StepsCountResponse response = mStepCountHelper.getAllStepCountHistory();
        if (response.isQueryOk()) {
            CACHE.save(Preference.STEP_COUNT_CONTENT_KEY, contents);
            postContents(contents);
        }
    }

    private void postContents(@NonNull List<Content> contents) {
        EventBus.getDefault()
                .post(new DetectedStepsCountEvent(contents));

    }
}