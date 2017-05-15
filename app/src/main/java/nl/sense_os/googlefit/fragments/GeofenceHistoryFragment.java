package nl.sense_os.googlefit.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import nl.sense_os.googlefit.awareness.AwarenessService;
import nl.sense_os.googlefit.constant.ServiceType;
import nl.sense_os.googlefit.eventbus.GeofenceEvent;

/**
 * Created by panjiyudasetya on 5/8/17.
 */

@SuppressWarnings("SpellCheckingInspection")
public class GeofenceHistoryFragment extends ContentListFragment {
    public static GeofenceHistoryFragment newInstance() {
        return new GeofenceHistoryFragment();
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

    @Subscribe
    @SuppressWarnings("unused")//This function being used by EventBus
    public void onGeofenceEvent(GeofenceEvent event) {
        showProgress(false);
        updateViews(event.getContents());
    }

    @Override
    protected void subscribe() {
        getActivity().startService(
                AwarenessService.withContext(
                        getActivity(),
                        ServiceType.Awareness.GEOFENCING
                )
        );
    }
}