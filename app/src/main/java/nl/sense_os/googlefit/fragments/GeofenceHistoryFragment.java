package nl.sense_os.googlefit.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import nl.sense_os.googlefit.entities.Content;
import nl.sense_os.googlefit.entities.GeofenceLocation;
import nl.sense_os.googlefit.eventbus.GeofenceEvent;
import nl.sense_os.googlefit.helpers.awareness.AwarenessApiHelper;
import nl.sense_os.googlefit.tasks.PopulateGeofenceDataTask;
import nl.sense_os.googlefit.utils.SnackbarHelper;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/**
 * Created by panjiyudasetya on 5/8/17.
 */

@SuppressWarnings("SpellCheckingInspection")
public class GeofenceHistoryFragment extends ContentListFragment {
    private static final int CLIENT_MANAGING_ID = 1;
    private static final int PERMISSIONS_REQ_CODE = 102;

    private static final String TAG = "GHF";
    private static final String[] PERMISSIONS = {
            ACCESS_FINE_LOCATION,
            ACCESS_COARSE_LOCATION,
            "com.google.android.gms.permission.ACTIVITY_RECOGNITION"
    };

    private GoogleApiClient mClient;
    private AwarenessApiHelper mAwarenessApiHelper;
    private boolean mIsConnSuspended, mIsConnFailed;

    public static GeofenceHistoryFragment newInstance() {
        return new GeofenceHistoryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isAllPermissionGranted(PERMISSIONS)) buildAwarenessClient();
        else ActivityCompat.requestPermissions(getActivity(), PERMISSIONS, PERMISSIONS_REQ_CODE);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mClient.isConnected() || mIsConnSuspended || mIsConnFailed) releaseAwarenessClient();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    @SuppressWarnings("unused")//This function being used by EventBus
    public void onGeofenceEvent(GeofenceEvent event) {
        // receiving new geofence event data
        if (event != null) populateData();
    }

    @Override
    protected void populateData() {
        new PopulateGeofenceDataTask() {
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
        initGeofenceLocation();
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
        mIsConnSuspended = true;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG, "Google Play services connection failed. Cause: "
                + connectionResult.toString());
        SnackbarHelper.show(mContainer,
                "Exception while connecting to Google Play services: "
                        + connectionResult.getErrorMessage());
        mIsConnFailed = true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQ_CODE:
                if (!isAllPermissionGranted(PERMISSIONS)) showPermissionsDeniedMessageDialog();
                else buildAwarenessClient();
                break;
        }
    }

    /**
     * Build a {@link GoogleApiClient} to authenticate the user and allow the application
     * to connect to the Fitness APIs. The included scopes should match the scopes needed
     * by your app (see the documentation for details).
     * Use the {@link GoogleApiClient.OnConnectionFailedListener}
     * to resolve authentication failures (for example, the user has not signed in
     * before, or has multiple accounts and must specify which account to use).
     */
    private void buildAwarenessClient() {
        // Create the Google API Client
        mClient = new GoogleApiClient.Builder(getActivity())
                .addApi(Awareness.API)
                .addConnectionCallbacks(this)
                .enableAutoManage(getActivity(), CLIENT_MANAGING_ID, this)
                .build();
        mClient.connect();

        mAwarenessApiHelper = new AwarenessApiHelper(getActivity(), mClient);
    }

    private void releaseAwarenessClient() {
        mClient.stopAutoManage(getActivity());
        mClient.disconnect();
    }

    private void initGeofenceLocation() {
        mAwarenessApiHelper.addOnFootFence();
        mAwarenessApiHelper.addGeofence(GeofenceLocation.SENSE_ID_HQ_LOCATION);
    }
}