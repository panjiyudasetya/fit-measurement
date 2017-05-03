package nl.sense_os.googlefit;

import android.app.Application;

import com.orhanobut.hawk.Hawk;

/**
 * Created by panjiyudasetya on 5/3/17.
 */

public class CoreApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Hawk.init(this).build();
    }
}
