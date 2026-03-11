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

public class NotificationManagement {
    private DatabaseReference notificationsRef;

    public static void sendProfileIncompleteNotification(String uid, List<String> missingFields){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Notifications");
        String message = "Bitte füllen Sie die folgenden Felder aus: " + String.join(", ",missingFields);
        int count = missingFields.size();
        HashMap<String,Object> map = new HashMap<>();

        map.put("type","PROFILE_INCOMPLETE");
        map.put("missingFields",missingFields);
        map.put("message",message);
        map.put("read",false);
        map.put("timestamp",System.currentTimeMillis());
        if (count != 0) {
            map.put("countMissing", count);
        }else {
            map.put("countMissing", 0);
        }

        ref.push().setValue(map);
    }
    public static void sendEmailVerificationNotification(String uid){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Notifications");

        HashMap<String,Object> map = new HashMap<>();

        map.put("type","EMAIL_VERIFICATION");
        map.put("message","Bitte bestätigen Sie Ihre E-Mail-Adresse");
        map.put("read",false);
        map.put("timestamp",System.currentTimeMillis());

        ref.push().setValue(map);

    }
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
