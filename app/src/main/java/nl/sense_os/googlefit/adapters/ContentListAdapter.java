package nl.sense_os.googlefit.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import nl.sense_os.googlefit.R;
import nl.sense_os.googlefit.adapters.holders.ContentListViewHolder;
import nl.sense_os.googlefit.entities.Content;

/**
 * Created by panjiyudasetya on 5/3/17.
 */

public class ContentListAdapter extends RecyclerView.Adapter<ContentListViewHolder> {
    private List<Content> mDataSource;

    public ContentListAdapter(@NonNull List<Content> dataSource) {
        mDataSource = dataSource;
    }

    public void updateDataSource(@NonNull List<Content> dataSource) {
        mDataSource = dataSource;
        notifyDataSetChanged();
    }

    @Override
    public ContentListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_content, parent, false);
        return new ContentListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ContentListViewHolder holder, int position) {
        holder.populate(mDataSource.get(position));
    }

    @Override
    public int getItemCount() {
        return mDataSource.size();
    }
}
