package nl.sense_os.googlefit.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import nl.sense_os.googlefit.eventbus.DetectedStepsCountEvent;

/**
 * Created by panjiyudasetya on 5/3/17.
 */

public class StepCountFragment extends ContentListFragment {
    private static final String TAG = "SCF";

    public static StepCountFragment newInstance() {
        return new StepCountFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        subscribe();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void populateData() {
        showProgress(true);
        subscribe();
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    @SuppressWarnings("unused")//This function being used by EventBus
    public void onDetectedStepsCountEvent(@Nullable DetectedStepsCountEvent event) {
        showProgress(false);
        updateViews(event.getContents());
    }
}
