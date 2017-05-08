package nl.sense_os.googlefit.adapters.holders;

import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import java.text.DateFormat;

import butterknife.BindView;
import nl.sense_os.googlefit.R;
import nl.sense_os.googlefit.entities.Content;

/**
 * Created by panji on 4/8/2017.
 */

public class ContentListViewHolder extends BaseViewHolder<Content> {
    @BindView(R.id.view_strip)
    View mStripView;
    @BindView(R.id.tv_content)
    TextView mTvContent;
    @BindView(R.id.tv_time_stamp)
    TextView mTvTimeStamp;

    private DateFormat mDateTimeFormat;
    public ContentListViewHolder(@NonNull View itemView) {
        super(itemView);
        this.mDateTimeFormat = DateFormat.getDateInstance();
    }

    @Override
    public void populate(Content item) {
        setStripBackgroundColor(item);
        mTvContent.setText(item.getContent());
        mTvTimeStamp.setText(
            String.format("%s %s",
                    mContext.getString(R.string.lbl_recorded_at),
                    mDateTimeFormat.format(item.getTimeStamp())
            )
        );
    }

    private void setStripBackgroundColor(@NonNull Content item) {
        switch (item.getType()) {
            case Content.STEPS_TYPE :
                mStripView.setBackground(
                        ContextCompat.getDrawable(mContext, R.drawable.bg_steps)
                );
                break;
            case Content.GEOFENCE_TYPE :
                mStripView.setBackground(
                        ContextCompat.getDrawable(mContext, R.drawable.bg_geofence)
                );
                break;
            default :
                mStripView.setBackground(
                        ContextCompat.getDrawable(mContext, R.drawable.bg_activity)
                );
                break;
        }
    }
}
