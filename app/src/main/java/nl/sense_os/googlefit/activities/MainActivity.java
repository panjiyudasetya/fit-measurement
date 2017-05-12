package nl.sense_os.googlefit.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import butterknife.BindView;
import nl.sense_os.googlefit.R;
import nl.sense_os.googlefit.awareness.AwarenessIntentService;
import nl.sense_os.googlefit.awareness.GoogleFitIntentService;
import nl.sense_os.googlefit.constant.Navigation;
import nl.sense_os.googlefit.core.BaseActivity;
import nl.sense_os.googlefit.fragments.ContentListFragment;
import nl.sense_os.googlefit.fragments.DetectedActivityFragment;
import nl.sense_os.googlefit.fragments.GeofenceHistoryFragment;
import nl.sense_os.googlefit.fragments.LocationHistoryFragment;
import nl.sense_os.googlefit.fragments.StepCountFragment;

public class MainActivity extends BaseActivity {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.bottom_navigation)
    BottomNavigationView mNavigationView;

    private int mSelectedItem;
    private int mActiveMenuId;

    @Override
    protected int initWithLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected void initViews(@Nullable Bundle savedInstanceState) {
        initToolbar();
        initNavigationView(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(Navigation.SELECTED_ITEM_KEY, mSelectedItem);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        subscribe();
    }

    @Override
    public void onBackPressed() {
        MenuItem homeItem = mNavigationView.getMenu().getItem(0);
        if (mSelectedItem != homeItem.getItemId()) setupContent(homeItem);
        else super.onBackPressed();
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(getString(R.string.app_name));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
    }

    private void initNavigationView(@Nullable Bundle savedInstanceState) {
        mNavigationView.setOnNavigationItemSelectedListener(
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    setupContent(item);
                    return true;
                }
            }
        );

        MenuItem selectedItem;
        if (savedInstanceState != null) {
            mSelectedItem = savedInstanceState.getInt(Navigation.SELECTED_ITEM_KEY, R.id.menu_walk);
            mNavigationView.setSelectedItemId(mSelectedItem);
            selectedItem = mNavigationView.getMenu().findItem(mSelectedItem);
        } else selectedItem = mNavigationView.getMenu().findItem(R.id.menu_walk);

        setupContent(selectedItem);

    }

    private void setupContent(@NonNull MenuItem menuItem) {
        if (mActiveMenuId == menuItem.getItemId()) return;

        ContentListFragment fragment;
        switch (menuItem.getItemId()) {
            case R.id.menu_walk:
                fragment = StepCountFragment.newInstance();
                break;
            case R.id.menu_geofence:
                fragment = GeofenceHistoryFragment.newInstance();
                break;
            case R.id.menu_activity:
                fragment = DetectedActivityFragment.newInstance();
                break;
            case R.id.menu_location:
                fragment = LocationHistoryFragment.newInstance();
                break;
            default:
                fragment = StepCountFragment.newInstance();
                break;
        }

        mSelectedItem = menuItem.getItemId();

        mFragmentManager
                .beginTransaction()
                .replace(R.id.main_container, fragment)
                .commit();

        mActiveMenuId = menuItem.getItemId();
    }

    public void subscribe() {
        startService(AwarenessIntentService.withContext(this));
        startService(GoogleFitIntentService.withContext(this));
    }
}