package com.melikash98.housesuche.HelperClass;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.melikash98.housesuche.AccountActivity;
import com.melikash98.housesuche.MessageActivity;
import com.melikash98.housesuche.R;

/**
 * The NotificationHelper class is a helper utility for building and displaying Android notifications
 * in the HouseSuche application. This class centrally manages all logic related to creating notification
 * channels, checking permissions, constructing PendingIntent objects, and displaying notifications.
 *
 * Main purpose of this class: to separate notification logic from the FirebaseMessagingService,
 * resulting in cleaner, more maintainable, and reusable code.
 *
 * Key features:
 * - showReplyNotification(): Displays a notification specifically for new chat messages.
 *   When clicked, it directly navigates the user to the MessageActivity screen.
 *
 * - showGeneralNotification(): Displays general application notifications (such as new listings,
 *   updates, etc.). When clicked, it redirects the user to the AccountActivity screen.
 *
 * Technical features:
 * - Supports two separate channels: one for chat messages (CHANNEL_MESSAGES) and one for general
 *   app notifications (CHANNEL_APP).
 * - Automatically creates notification channels for Android 8.0 (Oreo) and above with high importance (IMPORTANCE_HIGH).
 * - Checks for POST_NOTIFICATIONS permission on Android 13 (Tiramisu) and above.
 * - Uses PendingIntent with FLAG_IMMUTABLE for improved security.
 * - Each notification is displayed with a unique ID (based on the current timestamp) to allow
 *   multiple notifications to be managed simultaneously.
 * - Includes proper logging for easier debugging (success or permission issues).
 *
 * This class is invoked by the NotificationApp class and uses the application icon (logo_app)
 * for all notifications.
 * All notifications are set to AutoCancel, meaning they are automatically dismissed when clicked.
 */

public class NotificationHelper {
    private static final String TAG = "NotificationHelper";
    private static final String CHANNEL_MESSAGES = "channel_messages";
    private static final String CHANNEL_APP = "channel_app_notifications";

    public static void showReplyNotification(Context context, String title, String body) {
        if (context == null) {
            Log.e(TAG, "Context is null in showReplyNotification!");
            return;
        }

        checkNotificationPermission(context);
        createNotificationChannel(context, CHANNEL_MESSAGES, "New Message");

        Intent intent = new Intent(context, MessageActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_MESSAGES)
                .setSmallIcon(R.drawable.logo_app)
                .setContentTitle(title != null ? title : "Neue Stellungnahme")
                .setContentText(body != null ? body : "Sie haben eine neue Antwort vom Eigentümer erhalten.")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationId = (int) System.currentTimeMillis();
        manager.notify(notificationId, builder.build());

        Log.d(TAG, "Reply Notification posted successfully | ID=" + notificationId);
    }

    public static void showGeneralNotification(Context context, String title, String body) {
        if (context == null) {
            Log.e(TAG, "Context is null in showGeneralNotification!");
            return;
        }

        checkNotificationPermission(context);
        createNotificationChannel(context, CHANNEL_APP, "App Notification");

        Intent intent = new Intent(context, AccountActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_APP)
                .setSmallIcon(R.drawable.logo_app)
                .setContentTitle(title != null ? title : "HouseSuche")
                .setContentText(body != null ? body : "Neue Benachrichtigung")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationId = (int) System.currentTimeMillis();
        manager.notify(notificationId, builder.build());

        Log.d(TAG, "General Notification posted successfully | ID=" + notificationId);
    }

    private static void checkNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "POST_NOTIFICATIONS permission is not granted!");
                return;
            }
        }
    }

    private static void createNotificationChannel(Context context, String channelId, String channelName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
            );

            if (channelId.equals(CHANNEL_MESSAGES)) {
                channel.setDescription("Benachrichtigung über eine neue Antwort des Grundstückseigentümers");
            } else {
                channel.setDescription("Allgemeine Benachrichtigungen der App");
            }

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
