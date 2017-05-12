package nl.sense_os.googlefit.fragments;

import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import nl.sense_os.googlefit.R;
import nl.sense_os.googlefit.activities.MainActivity;
import nl.sense_os.googlefit.adapters.ContentListAdapter;
import nl.sense_os.googlefit.core.BaseFragment;
import nl.sense_os.googlefit.entities.Content;
import nl.sense_os.googlefit.eventbus.AwarenessConnReceivedEvent;
import nl.sense_os.googlefit.utils.SnackbarHelper;

/**
 * Created by panjiyudasetya on 5/3/17.
 */

public abstract class ContentListFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "CLF";
    @BindView(R.id.content_container)
    CoordinatorLayout mContainer;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeLayout;
    @BindView(R.id.rv_list_content)
    RecyclerView mRvContent;
    @BindView(R.id.tv_info)
    TextView mTvInfo;

    private ContentListAdapter mAdapter;
    protected abstract void populateData();

    @Override
    protected int initWithLayout() {
        return R.layout.fragment_content;
    }

    @Override
    protected void initViews() {
        initContentList();
        populateData();
    }

    protected void showProgress(final boolean show) {
        if (getView() == null || (show && mSwipeLayout.isRefreshing())) return;

        mSwipeLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeLayout.setRefreshing(show);
            }
        });
    }

    protected void updateViews(@NonNull List<Content> dataSource) {
        if (getView() != null) {
            if (!dataSource.isEmpty()) {
                mTvInfo.setVisibility(View.GONE);
                mAdapter.updateDataSource(dataSource);
            } else mTvInfo.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRefresh() {
        populateData();
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    @SuppressWarnings("unused")//This function being used by EventBus
    public void onAwarenessConnReceivedEvent(AwarenessConnReceivedEvent event) {
        // receiving Awareness connection event
        if (event == null) return;

        AwarenessConnReceivedEvent.Status status = event.getStatus();
        String  message = event.getMessage();
        if (status.equals(AwarenessConnReceivedEvent.Status.CONNECTED)) {
            Log.i(TAG, "Connected!!!");
        } else {
            Log.w(TAG, message);
            SnackbarHelper.show(mContainer, message);
        }
    }

    protected void subscribe() {
        if (MainActivity.class.isInstance(getActivity())) {
            ((MainActivity) getActivity()).subscribe();
        }
    }

    private void initContentList() {
        mRvContent.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mActivity);
        mRvContent.setLayoutManager(layoutManager);

        mAdapter = new ContentListAdapter(Collections.<Content>emptyList());
        mRvContent.setAdapter(mAdapter);

        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorSchemeResources(
                R.color.colorBluePrimary,
                R.color.colorPrimary,
                R.color.colorPrimaryDark);
    }
}
