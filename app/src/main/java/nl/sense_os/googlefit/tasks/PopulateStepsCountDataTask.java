package nl.sense_os.googlefit.tasks;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.google.android.gms.common.api.GoogleApiClient;

import java.util.List;

import nl.sense_os.googlefit.entities.Content;
import nl.sense_os.googlefit.entities.StepsCountResponse;
import nl.sense_os.googlefit.helpers.DataCacheHelper;
import nl.sense_os.googlefit.helpers.StepCountHelper;

import static nl.sense_os.googlefit.constant.Preference.STEP_COUNT_CONTENT_KEY;
/**
 * Created by panjiyudasetya on 5/15/17.
 */

public class PopulateStepsCountDataTask extends AsyncTask<Void, Integer, List<Content>> {
    private static final DataCacheHelper CACHE = new DataCacheHelper();
    private final StepCountHelper mModel;

    public PopulateStepsCountDataTask(@NonNull GoogleApiClient client) {
        this.mModel = new StepCountHelper(client);
    }

    @Override
    protected List<Content> doInBackground(Void... voids) {
        List<Content> cacheContents = CACHE.load(STEP_COUNT_CONTENT_KEY);
        StepsCountResponse response = mModel.getAllStepCountHistory();
        if (response.isQueryOk()) {
            List<Content> contents = response.getContents();
            CACHE.save(STEP_COUNT_CONTENT_KEY, contents);
            return contents;
        } else return cacheContents;
    }

    public void run() {
        executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
