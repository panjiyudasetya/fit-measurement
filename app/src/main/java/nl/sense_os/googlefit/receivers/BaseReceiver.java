package nl.sense_os.googlefit.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import nl.sense_os.googlefit.awareness.AwarenessService;
import nl.sense_os.googlefit.awareness.GoogleFitService;
import nl.sense_os.googlefit.constant.ServiceType;
import nl.sense_os.googlefit.helpers.AlarmHelper;

/**
 * Created by panjiyudasetya on 5/15/17.
 */

public abstract class BaseReceiver extends BroadcastReceiver {
    protected Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        wakingUpServices();
    }

    protected void scheduleAlarms() {
        AlarmHelper alarmHelper = new AlarmHelper(mContext);
        alarmHelper.setNextSelfSchedulingAlarm();
        alarmHelper.startRepeatingAlarm();
    }

    protected void wakingUpServices() {
        if (!AwarenessService.isActive()) wakingUpAwarenessService();
        if (!GoogleFitService.isActive()) wakingUpFitnessService();
    }

    private void wakingUpAwarenessService() {
        mContext.startService(
                AwarenessService.withContext(
                        mContext,
                        ServiceType.Awareness.ALL
                )
        );
    }

    private void wakingUpFitnessService() {
        mContext.startService(
                GoogleFitService.withContext(
                        mContext,
                        ServiceType.Awareness.ALL
                )
        );
    }
}