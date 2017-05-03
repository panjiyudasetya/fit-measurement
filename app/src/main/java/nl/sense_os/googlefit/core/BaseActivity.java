package nl.sense_os.googlefit.core;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by panjiyudasetya on 5/2/17.
 */

public abstract class BaseActivity extends AppCompatActivity {
    @SuppressWarnings("SpellCheckingInspection")
    private Unbinder mUnbinder;
    protected FragmentManager mFragmentManager;
    protected abstract int initWithLayout();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(initWithLayout());
        mUnbinder = ButterKnife.bind(this);
        mFragmentManager = getSupportFragmentManager();
        initViews(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }

    protected void initViews(@Nullable Bundle savedInstanceState) { }
}
