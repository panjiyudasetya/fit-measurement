package nl.sense_os.googlefit.tasks;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import nl.sense_os.googlefit.entities.Content;

/**
 * Created by panjiyudasetya on 5/3/17.
 */

public class PopulateStepCountTask extends AsyncTask<Void, Integer, List<Content>> {
    private static final String TAG = "STEP_COUNT_TASK";
    private static final int START_TIME = 0;
    private static final int END_TIME = 1;

    private GoogleApiClient mClient;
    private DateFormat mDateFormat;

    public PopulateStepCountTask(GoogleApiClient client) {
        this.mClient = client;
        this.mDateFormat = DateFormat.getDateInstance();
    }

    @Override
    protected List<Content> doInBackground(Void... voids) {
        long[] range = getTimeRangeHistory();
        DataReadRequest readRequest = new DataReadRequest.Builder()
                // The data request can specify multiple data types to return, effectively
                // combining multiple data queries into one call.
                // In this example, it's very unlikely that the request is for several hundred
                // data points each consisting of a few steps and a timestamp.  The more likely
                // scenario is wanting to see how many steps were walked per day, for 7 days.
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                // Analogous to a "Group By" in SQL, defines how data should be aggregated.
                // bucketByTime allows for a time span, whereas bucketBySession would allow
                // bucketing by "sessions", which would need to be defined in code.
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(range[START_TIME], range[END_TIME], TimeUnit.MILLISECONDS)
                .build();

        // Invoke the History API to fetch the data with the query and await the result of
        // the read request.
        DataReadResult dataReadResult = Fitness.HistoryApi.readData(mClient, readRequest).await(1, TimeUnit.MINUTES);
        if (dataReadResult.getStatus().isSuccess()) {
            return createContentFromBucket(dataReadResult.getBuckets());
        } else Log.w(TAG, "There was a problem getting the step count.");
        return Collections.emptyList();
    }

    public void run() {
        executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private List<Content> createContentFromBucket(@Nullable List<Bucket> buckets) {
        if (buckets == null || buckets.isEmpty()) return Collections.emptyList();

        List<Content> contents = new ArrayList<>();
        for (Bucket bucket : buckets) {
            List<DataSet> dataSets = bucket.getDataSets();
            int startFormIndex = 0;
            for (DataSet dataSet : dataSets) {
                contents.addAll(startFormIndex, dumpDataSet(dataSet));
                startFormIndex = contents.size();
            }
        }
        return contents;
    }

    private List<Content> dumpDataSet(@Nullable DataSet dataSet) {
        if (dataSet == null) return Collections.emptyList();

        List<Content> contents = new ArrayList<>();
        Log.i(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());

        for (DataPoint dp : dataSet.getDataPoints()) {
            contents.add(new Content(
               Content.STEPS_TYPE,
               new Content.Builder()
                    .dataPointType(dp.getDataType().getName())
                    .startTimeDetected(mDateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)))
                    .endTimeDetected(mDateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)))
                    .fields(extractPairOfFields(dp))
                    .build(),
               mDateFormat.format(dp.getTimestamp(TimeUnit.MILLISECONDS))
            ));
        }
        return contents;
    }

    private long[] getTimeRangeHistory() {
        // Setting a start and end date using a range of 1 week before this moment.
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        Log.i(TAG, "Range Start: " + mDateFormat.format(startTime));
        Log.i(TAG, "Range End: " + mDateFormat.format(endTime));

        return new long[] {startTime, endTime};
    }

    @Nullable
    private Map<String, String> extractPairOfFields(@NonNull DataPoint dataPoint) {
        List<Field> fields = dataPoint.getDataType().getFields();
        if (fields == null || fields.isEmpty()) return null;

        Map<String, String> output = new HashMap<>();
        for (Field field : fields) output.put(field.getName(), dataPoint.getValue(field).toString());

        return output;
    }
}
