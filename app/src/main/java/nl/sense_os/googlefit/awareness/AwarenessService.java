package nl.sense_os.googlefit.awareness;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.orhanobut.hawk.Hawk;

import org.greenrobot.eventbus.EventBus;

import nl.sense_os.googlefit.awareness.apis.AwarenessApiHelper;
import nl.sense_os.googlefit.awareness.apis.LocationUpdateApiHelper;
import nl.sense_os.googlefit.awareness.apis.MonitoringGeofenceApiHelper;
import nl.sense_os.googlefit.constant.ServiceType;
import nl.sense_os.googlefit.entities.Content;
import nl.sense_os.googlefit.eventbus.LocationChangeEvent;
import nl.sense_os.googlefit.helpers.DataCacheHelper;

import static nl.sense_os.googlefit.constant.Preference.LOCATION_UPDATE_CONTENT_KEY;

/**
 * Created by panjiyudasetya on 5/8/17.
 */

@SuppressWarnings("SpellCheckingInspection")
public class AwarenessService extends BaseService implements LocationListener {
    private static final String TAG = "AWARENESS_SERVICE";
    private static final String KEY_ACTIVATED = String.format("%s_IS_ACTIVE", TAG);
    private static final Api[] REQUIRED_APIS = {Awareness.API, ActivityRecognition.API, LocationServices.API};
    private static final DataCacheHelper CACHE = new DataCacheHelper();
    private static int mServiceType;

    private GoogleApiClient mClient;
    private AwarenessApiHelper mAwarenessHelper;
    private MonitoringGeofenceApiHelper mGeofenceHelper;
    private LocationUpdateApiHelper mLocationHelper;

    public static Intent withContext(@NonNull Context context, int type) {
        mServiceType = type;
        return new Intent(context, AwarenessService.class);
    }

    public static boolean isActive() {
        return Hawk.get(KEY_ACTIVATED, false);
    }

    @Override
    protected String tag() {
        return TAG;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Hawk.put(KEY_ACTIVATED, true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Hawk.put(KEY_ACTIVATED, false);
    }

    @Override
    protected Scope[] initWithGoogleClientScopes() {
        return null;
    }

    @Override
    protected Api[] initWithGoogleClientApis() {
        return REQUIRED_APIS;
    }

    @Override
    protected void initComponents() {
        mClient = getApiClient();
        mAwarenessHelper = new AwarenessApiHelper(this, mClient);
        mGeofenceHelper = new MonitoringGeofenceApiHelper(this, mClient);
        mLocationHelper = new LocationUpdateApiHelper(this, mClient);
    }

    @Override
    protected void subscribe() {
        startAwareness();
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

    private void startAwareness() {
        if (mServiceType == ServiceType.Awareness.ACTIVITIES) mAwarenessHelper.requestUpdateActivity();
        else if (mServiceType == ServiceType.Awareness.GEOFENCING) mGeofenceHelper.addSenseHQGeofences();
        else if (mServiceType == ServiceType.Awareness.LOCATION_UPDATES) startLocationUpdates();
        else if (mServiceType == ServiceType.Awareness.ALL) {
            mAwarenessHelper.requestUpdateActivity();
            mGeofenceHelper.addSenseHQGeofences();
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
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
