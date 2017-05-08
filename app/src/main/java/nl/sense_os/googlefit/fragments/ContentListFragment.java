package nl.sense_os.googlefit.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import nl.sense_os.googlefit.R;
import nl.sense_os.googlefit.adapters.ContentListAdapter;
import nl.sense_os.googlefit.core.BaseFragment;
import nl.sense_os.googlefit.entities.Content;

/**
 * Created by panjiyudasetya on 5/3/17.
 */

public abstract class ContentListFragment extends BaseFragment
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
            SwipeRefreshLayout.OnRefreshListener {

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

    protected void hideEmptyView() {
        if (getView() == null) return;
        mTvInfo.setVisibility(View.GONE);
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

    /**
     * Helper function to detect all permission has been granted by user.
     *
     * @return True if permissions granted, False otherwise.
     */
    protected boolean isAllPermissionGranted(String[] permissions) {
        if (permissions == null || permissions.length == 0) return true;

        try {
            Context context = getActivity();
            PackageInfo info = context
                    .getPackageManager()
                    .getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);

            List<Boolean> grantedPermissions = new ArrayList<>();
            for (String permission : permissions) {
                if (info.requestedPermissions != null) {
                    for (String p : info.requestedPermissions) {
                        if (p.equals(permission)) {
                            grantedPermissions.add(true);
                            break;
                        }
                    }
                }
            }

            return grantedPermissions.size() == permissions.length;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    protected void showPermissionsDeniedMessageDialog() {
        new AlertDialog.Builder(getActivity())
                .setMessage(R.string.permission_denied)
                .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create()
                .show();

    }
}
