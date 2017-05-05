package nl.sense_os.googlefit.tasks;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.google.android.gms.common.api.GoogleApiClient;

import java.util.List;

import nl.sense_os.googlefit.entities.Content;
import nl.sense_os.googlefit.models.StepCountHelper;

/**
 * Created by panjiyudasetya on 5/3/17.
 */

public class PopulateStepCountTask extends AsyncTask<Void, Integer, List<Content>> {
    private StepCountHelper mModel;

    public PopulateStepCountTask(@NonNull GoogleApiClient client) {
        this.mModel = new StepCountHelper(client);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        onPostExecute(mModel.loadCache());
    }

    @Override
    protected List<Content> doInBackground(Void... voids) {
        List<Content> newContent = mModel.getAllStepCountHistory();
        mModel.saveCache(newContent);
        return newContent;
    }

    public void run() {
        executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
