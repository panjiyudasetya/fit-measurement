package nl.sense_os.googlefit.fragments;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import nl.sense_os.googlefit.R;
import nl.sense_os.googlefit.adapters.ContentListAdapter;
import nl.sense_os.googlefit.core.BaseFragment;
import nl.sense_os.googlefit.entities.Content;
import nl.sense_os.googlefit.services.PopulateContentTask;

/**
 * Created by panjiyudasetya on 5/3/17.
 */

public class ContentFragment extends BaseFragment {
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeLayout;
    @BindView(R.id.rv_list_content)
    RecyclerView mRvContent;
    @BindView(R.id.tv_info)
    TextView mTvInfo;

    private int mType;
    private ContentListAdapter mAdapter;

    public static ContentFragment newInstance(@Content.ContentType int contentType) {
        ContentFragment fragment = new ContentFragment();
        fragment.mType = contentType;
        return fragment;
    }

    @Override
    protected int initWithLayout() {
        return R.layout.fragment_content;
    }

    @Override
    protected void initViews() {
        initContentList();
        populateData();
    }

    private void updateViews(@NonNull List<Content> dataSource) {
        if (getView() != null) {
            if (!dataSource.isEmpty()) {
                mTvInfo.setVisibility(View.GONE);
                mAdapter.updateDataSource(dataSource);
            } else mTvInfo.setVisibility(View.VISIBLE);
        }
    }

    private void initContentList() {
        mRvContent.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mActivity);
        mRvContent.setLayoutManager(layoutManager);

        mAdapter = new ContentListAdapter(Collections.<Content>emptyList());
        mRvContent.setAdapter(mAdapter);
    }

    private void populateData() {
        new PopulateContentTask(mType) {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showProgress(true);
            }

            @Override
            protected void onPostExecute(List<Content> contents) {
                super.onPostExecute(contents);
                showProgress(false);

                if (!contents.isEmpty()) updateViews(contents);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void showProgress(final boolean show) {
        mSwipeLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeLayout.setRefreshing(show);
            }
        });
    }
}
