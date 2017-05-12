package nl.sense_os.googlefit.core;

import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import nl.sense_os.googlefit.R;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.INTERNET;

/**
 * Created by panjiyudasetya on 5/2/17.
 */

public abstract class BaseActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQ_CODE = 103;
    private static final String[] PERMISSIONS = {
            INTERNET,
            ACCESS_FINE_LOCATION,
            ACCESS_COARSE_LOCATION,
            "com.google.android.gms.permission.ACTIVITY_RECOGNITION"
    };

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

        requestAllPermissions();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }

    protected void initViews(@Nullable Bundle savedInstanceState) { }

    private void requestAllPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSIONS_REQ_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQ_CODE:
                if (!isAllPermissionGranted(PERMISSIONS)) showPermissionsDeniedMessageDialog();
                break;
            default: return;
        }
    }

    /**
     * Helper function to detect all permission has been granted by user.
     *
     * @return True if permissions granted, False otherwise.
     */
    protected boolean isAllPermissionGranted(String[] permissions) {
        if (permissions == null || permissions.length == 0) return true;

        try {
            PackageInfo info = getPackageManager()
                    .getPackageInfo(getPackageName(), PackageManager.GET_PERMISSIONS);

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
        new AlertDialog.Builder(this)
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
