package com.melikash98.housesuche.HelperClass;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.melikash98.housesuche.AccountActivity;
import com.melikash98.housesuche.MessageActivity;
import com.melikash98.housesuche.R;

/**
 * The NotificationApp class is a FirebaseMessagingService responsible for receiving and handling
 * push notifications (FCM) across the entire HouseSuche application.
 *
 * This class performs two main tasks:
 *
 * 1. FCM Token Management (onNewToken):
 *    Whenever a new token is generated (e.g., first app launch, app reinstall, or token refresh),
 *    it stores the token in Firebase Realtime Database under the path:
 *    Users/{userId}/fcmToken.
 *    This allows the server to send notifications specifically to the currently logged-in user.
 *
 * 2. Processing Incoming Messages (onMessageReceived):
 *    When a notification is received from the server, the class extracts the title and body.
 *    It then checks the "type" field in the data payload to determine the notification type:
 *       - If type equals "message" → a chat notification (with reply capability) is displayed.
 *       - Otherwise → a standard general notification is shown.
 *
 * All notification display logic (including channel creation, PendingIntent configuration,
 * sound, vibration, etc.) is delegated to the NotificationHelper class to keep this service
 * clean and focused.
 *
 * This service must be registered as an FCM service in the AndroidManifest.xml file.
 * It is used for important notifications such as new chat messages, new listings,
 * updates, and more.
 *
 * Note: TAG-based logs are included to facilitate easier debugging in Logcat.
 */

public class NotificationApp extends FirebaseMessagingService {
    private static final String TAG = "NotificationApp";

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "New FCM token received: " + token);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseDatabase.getInstance().getReference("Users")
                    .child(user.getUid())
                    .child("fcmToken")
                    .setValue(token);
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d(TAG, "FCM Message received from server");

        String title = remoteMessage.getNotification() != null ?
                remoteMessage.getNotification().getTitle() : "HouseSuche";

        String body = remoteMessage.getNotification() != null ?
                remoteMessage.getNotification().getBody() : "Neue Nachricht";

        String type = remoteMessage.getData().get("type");

        if ("message".equals(type)) {
            NotificationHelper.showReplyNotification(this, title, body);
        } else {
            NotificationHelper.showGeneralNotification(this, title, body);
        }
    }
}
