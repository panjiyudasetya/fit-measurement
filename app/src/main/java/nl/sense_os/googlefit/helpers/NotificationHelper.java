package nl.sense_os.googlefit.helpers;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;

import nl.sense_os.googlefit.R;
import nl.sense_os.googlefit.activities.MainActivity;

/**
 * Created by panjiyudasetya on 5/12/17.
 */

public class NotificationHelper {

   public static void createNotification(@NonNull Context context,
                                         @NonNull String title,
                                         @NonNull String content,
                                         int notificationId,
                                         boolean redirectToMainApp) {
       
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(content);
        if (redirectToMainApp) {
            // Creates an explicit intent for an Activity in your app
            Intent resultIntent = new Intent(context, MainActivity.class);

            // The stack builder object will contain an artificial back stack for the
            // started Activity.
            // This ensures that navigating backward from the Activity leads out of
            // your application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            // Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(MainActivity.class);
            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            notificationBuilder.setContentIntent(resultPendingIntent);
        }
        NotificationManagerCompat.from(context).notify(notificationId, notificationBuilder.build());
    }
}
