package nl.sense_os.googlefit.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import butterknife.BindView;
import nl.sense_os.googlefit.R;
import nl.sense_os.googlefit.constant.Navigation;
import nl.sense_os.googlefit.core.BaseActivity;
import nl.sense_os.googlefit.entities.Content;
import nl.sense_os.googlefit.fragments.ContentFragment;

public class MainActivity extends BaseActivity {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.bottom_navigation)
    BottomNavigationView mNavigationView;

    private int mSelectedItem;

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
                    return true;
                }
            }
        );

        MenuItem selectedItem;
        if (savedInstanceState != null) {
            mSelectedItem = savedInstanceState.getInt(Navigation.SELECTED_ITEM_KEY, 0);
            selectedItem = mNavigationView.getMenu().findItem(mSelectedItem);
        } else selectedItem = mNavigationView.getMenu().getItem(0);

        setupContent(selectedItem);
    }

    private void setupContent(@NonNull MenuItem menuItem) {
        ContentFragment fragment;
        switch (menuItem.getItemId()) {
            case R.id.menu_walk:
                fragment = ContentFragment.newInstance(Content.STEPS_TYPE);
                break;
            case R.id.menu_sleep:
                fragment = ContentFragment.newInstance(Content.SLEEP_TYPE);
                break;
            default:
                fragment = ContentFragment.newInstance(Content.STEPS_TYPE);
                break;
        }

        setActiveTab(menuItem);

        mFragmentManager
                .beginTransaction()
                .replace(R.id.main_container, fragment)
                .commit();
    }

    private void setActiveTab(@NonNull MenuItem menuItem) {
        // un check the other items.
        for (int i = 0; i < mNavigationView.getMenu().size(); i++) {
            MenuItem mItem = mNavigationView.getMenu().getItem(i);
            mItem.setChecked(mItem.getItemId() == menuItem.getItemId());
        }
    }
}