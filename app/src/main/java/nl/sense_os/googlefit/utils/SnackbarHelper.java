package nl.sense_os.googlefit.utils;

import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.view.View;

/**
 * Created by panjiyudasetya on 3/27/17.
 */

public class SnackbarHelper {
    public static void show(@NonNull View attachToView,
                            @NonNull String message) {
        if (attachToView == null) return;
        Snackbar.make(attachToView, message, Snackbar.LENGTH_LONG).show();
    }

    public static void show(@NonNull View attachToView,
                            @NonNull String message,
                            @NonNull String actionText,
                            @NonNull View.OnClickListener onActionClick) {
        if (attachToView == null) return;
        Snackbar.make(attachToView, message, Snackbar.LENGTH_LONG)
                .setAction(actionText, onActionClick)
                .show();
    }
}
