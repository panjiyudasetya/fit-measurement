package nl.sense_os.googlefit.services;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.hawk.Hawk;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import nl.sense_os.googlefit.constant.Preference;
import nl.sense_os.googlefit.entities.Content;

/**
 * Created by panjiyudasetya on 5/3/17.
 */

public class PopulateContentTask extends AsyncTask<Void, Integer, List<Content>> {
    private int mType;

    public PopulateContentTask(@NonNull @Content.ContentType int type) {
        mType = type;
    }

    @Override
    protected List<Content> doInBackground(Void... voids) {
        String jsonContent;
        switch (mType) {
            case Content.STEPS_TYPE :
                jsonContent = Hawk.get(Preference.STEP_COUNT_CONTENT_KEY, "");
                break;
            case Content.SLEEP_TYPE :
                jsonContent = Hawk.get(Preference.SLEEP_HISTORY_CONTENT_KEY, "");
                break;
            default :
                jsonContent = Hawk.get(Preference.STEP_COUNT_CONTENT_KEY, "");
                break;
        }

        if (!TextUtils.isEmpty(jsonContent)) {
            Type type = new TypeToken<List<List<Content>>>() { }.getType();
            return new Gson().fromJson(jsonContent, type);
        }

        return Collections.emptyList();
    }
}
