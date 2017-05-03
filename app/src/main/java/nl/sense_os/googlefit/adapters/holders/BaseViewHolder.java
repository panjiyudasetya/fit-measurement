package nl.sense_os.googlefit.adapters.holders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import butterknife.ButterKnife;

/**
 * Created by panjiyudasetya on 3/29/17.
 */

public abstract class BaseViewHolder<T> extends RecyclerView.ViewHolder {
    protected Context mContext;
    public BaseViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        mContext = itemView.getContext();
    }

    public abstract void populate(T item);
}
