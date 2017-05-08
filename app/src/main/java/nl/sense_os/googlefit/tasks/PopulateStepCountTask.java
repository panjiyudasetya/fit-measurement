package nl.sense_os.googlefit.tasks;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.google.android.gms.common.api.GoogleApiClient;

import java.util.List;

import nl.sense_os.googlefit.entities.Content;
import nl.sense_os.googlefit.helpers.DataCacheHelper;
import nl.sense_os.googlefit.helpers.StepCountHelper;

import static nl.sense_os.googlefit.constant.Preference.STEP_COUNT_CONTENT_KEY;
import static nl.sense_os.googlefit.utils.CollectionUtils.sortDesc;

/**
 * Created by panjiyudasetya on 5/3/17.
 */

public class PopulateStepCountTask extends AsyncTask<Void, Integer, List<Content>> {
    private final StepCountHelper mModel;
    private final DataCacheHelper mCache;

    public PopulateStepCountTask(@NonNull GoogleApiClient client) {
        this.mModel = new StepCountHelper(client);
        this.mCache = new DataCacheHelper();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        List<Content> contents = mCache.load(STEP_COUNT_CONTENT_KEY);
        sortDesc(contents);
        onPostExecute(contents);
    }

    @Override
    protected List<Content> doInBackground(Void... voids) {
        List<Content> contents = mModel.getAllStepCountHistory();
        mCache.save(STEP_COUNT_CONTENT_KEY, contents);
        sortDesc(contents);
        return contents;
    }

    public void run() {
        executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
