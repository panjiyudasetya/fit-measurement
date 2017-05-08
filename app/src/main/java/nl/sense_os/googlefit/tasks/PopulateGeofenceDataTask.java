package nl.sense_os.googlefit.tasks;

import android.os.AsyncTask;

import java.util.List;

import nl.sense_os.googlefit.entities.Content;
import nl.sense_os.googlefit.helpers.DataCacheHelper;

import static nl.sense_os.googlefit.constant.Preference.GEOFENCE_CONTENT_KEY;

/**
 * Created by panjiyudasetya on 5/8/17.
 */

@SuppressWarnings("SpellCheckingInspection")
public class PopulateGeofenceDataTask extends AsyncTask<Void, Integer, List<Content>> {
    private final DataCacheHelper mCache;

    public PopulateGeofenceDataTask() {
        this.mCache = new DataCacheHelper();
    }

    @Override
    protected List<Content> doInBackground(Void... voids) {
        List<Content> contents = mCache.load(GEOFENCE_CONTENT_KEY);
        mCache.save(GEOFENCE_CONTENT_KEY, contents);
        return contents;
    }

    public void run() {
        executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
