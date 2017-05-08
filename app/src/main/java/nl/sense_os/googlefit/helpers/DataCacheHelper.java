package nl.sense_os.googlefit.helpers;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.hawk.Hawk;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import nl.sense_os.googlefit.entities.Content;

/**
 * Created by panjiyudasetya on 5/5/17.
 */

public class DataCacheHelper {
    private static final Gson GSON = new Gson();

    public void save(@NonNull String key, @NonNull List<Content> newCache) {
        Hawk.put(key, GSON.toJson(newCache));
    }

    @NonNull
    public List<Content> load(@NonNull String key) {
        String cache = Hawk.get(key, "");
        if (TextUtils.isEmpty(cache)) return Collections.emptyList();

        Type token = new TypeToken<List<Content>>() { }.getType();
        List<Content> results = GSON.fromJson(cache, token);
        return results == null ? Collections.<Content>emptyList() : results;
    }
}
