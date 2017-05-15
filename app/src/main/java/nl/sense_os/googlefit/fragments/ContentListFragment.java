package nl.sense_os.googlefit.fragments;

import android.content.IntentSender;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;

import org.greenrobot.eventbus.Subscribe;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import nl.sense_os.googlefit.R;
import nl.sense_os.googlefit.adapters.ContentListAdapter;
import nl.sense_os.googlefit.core.BaseFragment;
import nl.sense_os.googlefit.entities.Content;
import nl.sense_os.googlefit.eventbus.GAClientConnReceivedEvent;
import nl.sense_os.googlefit.utils.SnackbarHelper;

import static nl.sense_os.googlefit.eventbus.GAClientConnReceivedEvent.Status;
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
    protected abstract void subscribe();

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

    @Subscribe
    @SuppressWarnings("unused")//This function being used by EventBus
    public void onAwarenessConnReceivedEvent(GAClientConnReceivedEvent event) {
        // receiving Awareness connection event
        if (event == null) return;

        Status status = event.getStatus();
        String message = event.getMessage();
        if (status.equals(Status.CONNECTED)) {
            Log.i(TAG, "Connected!!!");
        } else if (status.equals(Status.SIGN_IN_REQUIRED)) {
            ConnectionResult connectionResult = event.getConnResult();
            if (connectionResult != null) resolvePlayServiceCredentialProblem(connectionResult);
        } else {
            Log.w(TAG, message);
            showProgress(false);
            SnackbarHelper.show(mContainer, message);
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

    private void resolvePlayServiceCredentialProblem(@NonNull ConnectionResult connectionResult) {
        try {
            connectionResult.startResolutionForResult(getActivity(), ConnectionResult.SIGN_IN_REQUIRED);
        } catch (IntentSender.SendIntentException ex) {
            Log.e(TAG, ex.toString());
        }
    }
}
