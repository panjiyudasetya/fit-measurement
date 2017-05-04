package nl.sense_os.googlefit.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.data.DataType;

import java.util.List;

import nl.sense_os.googlefit.entities.Content;
import nl.sense_os.googlefit.tasks.PopulateStepCountTask;
import nl.sense_os.googlefit.utils.SnackbarHelper;

/**
 * Created by panjiyudasetya on 5/3/17.
 */

public class StepCountFragment extends ContentListFragment {

    private static final String TAG = "SCF";
    private GoogleApiClient mClient;

    public static StepCountFragment newInstance() {
        return new StepCountFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildFitnessClient();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseFitnessClient();
    }

    @Override
    protected void populateData() {
        new PopulateStepCountTask(mClient) {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showProgress(true);
            }

            @Override
            protected void onPostExecute(List<Content> contents) {
                super.onPostExecute(contents);
                showProgress(false);
                updateViews(contents);
            }
        }.run();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "Connected!!!");
        // Now you can make calls to the Fitness APIs.  What to do?
        // Subscribe to some data sources!
        subscribe();
    }

    @Override
    public void onConnectionSuspended(int clientCode) {
        String message;
        // If your connection to the sensor gets lost at some point,
        // you'll be able to determine the reason and react to it here.
        if (clientCode == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
            message = "Connection lost.  Cause: Network Lost.";
            Log.w(TAG, message);
            SnackbarHelper.show(mContainer, message);
        } else if (clientCode == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
            message = "Connection lost.  Reason: Service Disconnected";
            Log.w(TAG, message);
            SnackbarHelper.show(mContainer, message);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG, "Google Play services connection failed. Cause: "
                + connectionResult.toString());
        SnackbarHelper.show(mContainer,
                "Exception while connecting to Google Play services: "
                        + connectionResult.getErrorMessage());
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
        mClient = new GoogleApiClient.Builder(getActivity())
                .addApi(Fitness.RECORDING_API)
                .addApi(Fitness.HISTORY_API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addConnectionCallbacks(this)
                .enableAutoManage(getActivity(), 0, this)
                .build();
        mClient.connect();
    }

    private void releaseFitnessClient() {
        mClient.stopAutoManage(getActivity());
        mClient.disconnect();
    }

    /**
     * Record step data by requesting a subscription to background step data.
     */
    private void subscribe() {
        // To create a subscription, invoke the Recording API. As soon as the subscription is
        // active, fitness data will start recording.
        Fitness.RecordingApi
                .subscribe(mClient, DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            if (status.getStatusCode()
                                    == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                                SnackbarHelper.show(mContainer, "Existing subscription for activity detected.");
                            } else {
                                SnackbarHelper.show(mContainer, "Successfully subscribed!");
                            }
                        } else {
                            SnackbarHelper.show(mContainer, "There was a problem subscribing.");
                        }
                    }
                });
    }
}
