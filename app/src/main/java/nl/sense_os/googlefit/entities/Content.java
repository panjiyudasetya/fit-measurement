package nl.sense_os.googlefit.entities;

import android.support.annotation.IntDef;

import com.google.gson.annotations.SerializedName;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Map;

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

    public static class Builder {
        private String dataPointType;
        private String startTimeDetected;
        private String endTimeDetected;
        private Map<String, String> fields;

        public Builder dataPointType(String dataPointType) {
            this.dataPointType = dataPointType;
            return this;
        }

        public Builder startTimeDetected(String startTimeDetected) {
            this.startTimeDetected = startTimeDetected;
            return this;
        }

        public Builder endTimeDetected(String endTimeDetected) {
            this.endTimeDetected = endTimeDetected;
            return this;
        }

        public Builder fields(Map<String, String> fields) {
            this.fields = fields;
            return this;
        }

        public String build() {
            String strContent = "Data point:\n"
                    + "\tType: " + dataPointType + "\n"
                    + "\tStart: " + startTimeDetected  + "\n"
                    + "\tEnd: " + endTimeDetected + "\n";

            String strFields = "";
            if (fields != null && fields.size() > 0) {
                strFields += "\n";
                int i = 0;
                for (Map.Entry<String, String> field : fields.entrySet()) {
                    strFields += (i == 0 ? "" : "\n") + "\tField: "
                            + field.getKey() + ", Value: " + field.getValue();
                    i++;
                }
            }
            return strContent + strFields;
        }
    }
}
