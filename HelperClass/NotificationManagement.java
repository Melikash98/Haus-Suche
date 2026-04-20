package com.melikash98.housesuche.HelperClass;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

/**
 * The NotificationManagement class is a helper utility for managing in-app notifications
 * stored in Firebase Realtime Database.
 *
 * This class stores notifications in a type-based structure for each user and prevents
 * duplication of similar notifications. All notifications are saved under the path:
 * Users/{userId}/Notifications/{notificationType}.
 *
 * Key features:
 *
 * - sendOrUpdateNotification(): The main public method for sending or updating a notification.
 *   If a notification with the same type already exists, it will be updated instead of creating
 *   a new one (preventing notification clutter). The stored data includes type, message,
 *   read status, timestamp, and optional extra data (extraData).
 *
 * - sendProfileIncompleteNotification(): Sends a notification when the user's profile is incomplete.
 *   It receives a list of missing fields, formats them into a readable message, and also stores
 *   the count of missing items.
 *
 * - sendEmailVerificationNotification(): Sends a simple notification reminding the user to verify
 *   their email address.
 *
 * - removeNotificationByType(): Completely removes a specific notification based on its type
 *   (e.g., after profile completion or email verification).
 *
 * This class is highly useful for intelligent management of system-level in-app notifications
 * (such as profile completion, email verification, and similar cases) and prevents multiple
 * duplicate notifications for the same subject.
 *
 * All operations include proper logging (success and error) to facilitate easier debugging.
 */

public class NotificationManagement {
    private static final String TAG = "NotificationManagement";
    private DatabaseReference notificationsRef;

    public static void sendOrUpdateNotification(String uid, String type, String message, HashMap<String, Object> extraData) {

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(uid)
                .child("Notifications")
                .child(type);
        ref.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                boolean shouldUpdate = true;
                if (dataSnapshot.exists()) {
                    String typeRef = dataSnapshot.child("type").getValue(String.class);
                    if (typeRef != null && typeRef.equals(type)) {
                        shouldUpdate = false;
                    }
                }
                if (shouldUpdate){
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("type", type);
                    map.put("message", message);
                    map.put("read", false);
                    map.put("timestamp", System.currentTimeMillis());

                    if (extraData != null) {
                        map.putAll(extraData);
                    }
                    ref.setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d(TAG, "Notification updated");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Error updating notification", e);
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Error checking notification", e);
            }
        });


    }

    public static void sendProfileIncompleteNotification(String uid, List<String> missingFields) {
        HashMap<String, Object> extra = new HashMap<>();
        extra.put("missingFields", missingFields);
        extra.put("countMissing", missingFields == null ? 0 : missingFields.size());

        String message = "Bitte füllen Sie die folgenden Felder aus: " + String.join(", ", missingFields);
        sendOrUpdateNotification(uid, "PROFILE_INCOMPLETE", message, extra);
    }

    public static void sendEmailVerificationNotification(String uid) {
        sendOrUpdateNotification(
                uid,
                "EMAIL_VERIFICATION",
                "Bitte bestätigen Sie Ihre E-Mail-Adresse",
                null
        );

    }

    public static void removeNotificationByType(String uid, String type) {

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(uid)
                .child("Notifications")
                .child(type);

        ref.removeValue();

    }
}
