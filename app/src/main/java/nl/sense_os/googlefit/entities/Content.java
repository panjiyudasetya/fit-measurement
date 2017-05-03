package nl.sense_os.googlefit.entities;

import android.support.annotation.IntDef;

import com.google.gson.annotations.SerializedName;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by panjiyudasetya on 5/3/17.
 */

public class Content {
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ STEPS_TYPE, SLEEP_TYPE })
    public @interface ContentType {}
    public static final int STEPS_TYPE = 0;
    public static final int SLEEP_TYPE = 1;

    @SerializedName("type")
    private int type;
    @SerializedName("content")
    private String content;
    @SerializedName("time_stamp")
    private String timeStamp;

    public Content(@ContentType int type, String content, String timeStamp) {
        this.type = type;
        this.content = content;
        this.timeStamp = timeStamp;
    }

    public int getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public String getTimeStamp() {
        return timeStamp;
    }
}
