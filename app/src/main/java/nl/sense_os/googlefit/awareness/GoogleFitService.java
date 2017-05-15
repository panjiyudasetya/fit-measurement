package nl.sense_os.googlefit.awareness;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.data.DataType;
import com.orhanobut.hawk.Hawk;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import nl.sense_os.googlefit.constant.ServiceType;
import nl.sense_os.googlefit.entities.Content;
import nl.sense_os.googlefit.eventbus.DetectedStepsCountEvent;
import nl.sense_os.googlefit.eventbus.GAClientConnReceivedEvent;
import nl.sense_os.googlefit.tasks.PopulateStepsCountDataTask;

import static nl.sense_os.googlefit.eventbus.GAClientConnReceivedEvent.Status;

/**
 * Created by panjiyudasetya on 5/8/17.
 */

@SuppressWarnings("SpellCheckingInspection")
public class GoogleFitService extends BaseService {
    private static final String TAG = "FIT_SERVICE";
    private static final String KEY_ACTIVATED = String.format("%s_IS_ACTIVE", TAG);
    private static final Api[] REQUIRED_APIS = {Fitness.RECORDING_API, Fitness.HISTORY_API, Awareness.API};
    private static final Scope[] REQUIRED_SCOPES = {new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE)};
    private static int mServiceType;

    private GoogleApiClient mClient;
    private boolean mIsFitnessApiSubscribed;

    public static Intent withContext(@NonNull Context context, int type) {
        mServiceType = type;
        return new Intent(context, GoogleFitService.class);
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
        return REQUIRED_SCOPES;
    }

    @Override
    protected Api[] initWithGoogleClientApis() {
        return REQUIRED_APIS;
    }

    @Override
    protected void initComponents() {
        mClient = getApiClient();
    }

    @Override
    protected void subscribe() {
        if (!mIsFitnessApiSubscribed) subscribeFitnessApi();
        else consumeFitnessDataHistory();
    }

    /**
     * Record step data by requesting a subscription to background step data.
     */
    private void subscribeFitnessApi() {
        // To create a subscription, invoke the Recording API. As soon as the subscription is
        // active, fitness data will start recording.
        Fitness.RecordingApi
                .subscribe(mClient, DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .setResultCallback(new ResultCallback<com.google.android.gms.common.api.Status>() {
                    @Override
                    public void onResult(com.google.android.gms.common.api.Status status) {
                        mIsFitnessApiSubscribed = status.isSuccess();
                        if (status.isSuccess()) {
                            if (status.getStatusCode()
                                    == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                                EventBus.getDefault()
                                        .post(new GAClientConnReceivedEvent(
                                                Status.ALREADY_SUBSCRIBED,
                                                "Existing subscription for activity detected."
                                        ));
                            } else {
                                EventBus.getDefault()
                                        .post(new GAClientConnReceivedEvent(
                                                Status.SUCCESSFULLY_SUBSCRIBED,
                                                "Successfully subscribed!"
                                        ));
                            }

                            consumeFitnessDataHistory();
                        } else {
                            EventBus.getDefault()
                                    .post(new GAClientConnReceivedEvent(
                                            Status.FAILURE_TO_SUBSCRIBE,
                                            "There was a problem subscribing."
                                    ));
                        }
                    }
                });
    }

    private void consumeFitnessDataHistory() {
        if (mServiceType == ServiceType.Fitness.STEPS_COUNT) {
            consumeStepsCountHistory();
        } else if (mServiceType == ServiceType.Fitness.ALL) {
            //TODO: On this point probably we do more action to fetch some data on Fitness API
            consumeStepsCountHistory();
        }
    }

    private void consumeStepsCountHistory() {
        new PopulateStepsCountDataTask(mClient) {
            @Override
            protected void onPostExecute(List<Content> contents) {
                super.onPostExecute(contents);
                if (contents != null) postContents(contents);
            }
        }.run();
    }

    private void postContents(@NonNull List<Content> contents) {
        EventBus.getDefault()
                .post(new DetectedStepsCountEvent(contents));
    }
}