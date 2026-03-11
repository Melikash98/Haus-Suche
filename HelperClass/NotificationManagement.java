package com.melikash98.housesuche.HelperClass;

import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
/**
 * NotificationManagement
 *
 * Helper class responsible for managing user notifications
 * stored in Firebase Realtime Database.
 *
 * This class provides utility methods for:
 *
 * - Sending a notification when the user profile is incomplete
 * - Sending a notification when email verification is required
 * - Removing notifications by their type
 *
 * Database structure:
 *
 * Users
 *   └── uid
 *        └── Notifications
 *              └── notificationId
 *                    ├── type
 *                    ├── message
 *                    ├── read
 *                    ├── timestamp
 *                    └── additional fields depending on type
 *
 * All notifications are stored under:
 * Users/{uid}/Notifications
 *
 * Note:
 * Notifications are pushed with unique IDs using Firebase push().
 */
public class NotificationManagement {
    private DatabaseReference notificationsRef;
    /**
     * Sends a notification informing the user that
     * their profile information is incomplete.
     *
     * @param uid User ID
     * @param missingFields List of fields that the user has not filled yet
     */
    public static void sendProfileIncompleteNotification(String uid, List<String> missingFields){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Notifications");
        // Create message showing which fields are missing
        String message = "Bitte füllen Sie die folgenden Felder aus: " + String.join(", ",missingFields);
        int count = missingFields.size();
        HashMap<String,Object> map = new HashMap<>();

        map.put("type","PROFILE_INCOMPLETE");
        map.put("missingFields",missingFields);
        map.put("message",message);
        map.put("read",false);
        map.put("timestamp",System.currentTimeMillis());
        // Store number of missing fields
        if (count != 0) {
            map.put("countMissing", count);
        }else {
            map.put("countMissing", 0);
        }
        // Push notification to Firebase
        ref.push().setValue(map);
    }
    /**
     * Sends a notification requesting the user
     * to verify their email address.
     *
     * @param uid User ID
     */
    public static void sendEmailVerificationNotification(String uid){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Notifications");

        HashMap<String,Object> map = new HashMap<>();

        map.put("type","EMAIL_VERIFICATION");
        map.put("message","Bitte bestätigen Sie Ihre E-Mail-Adresse");
        map.put("read",false);
        map.put("timestamp",System.currentTimeMillis());

        ref.push().setValue(map);

    }
    /**
     * Removes all notifications of a specific type
     * for the given user.
     *
     * Example:
     * removeNotificationByType(uid, "EMAIL_VERIFICATION")
     *
     * @param uid User ID
     * @param type Notification type to remove
     */
    public static void removeNotificationByType(String uid,String type){

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Notifications");

        Query query = ref.orderByChild("type").equalTo(type);

        query.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot ds : snapshot.getChildren()){

                    ds.getRef().removeValue();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }
}
