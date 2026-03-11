package com.melikash98.housesuche.HelperClass;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class WeightManager {
    private static final String PATH = "RankingWeights";
    private DatabaseReference ref = FirebaseDatabase.getInstance().getReference(PATH);

    public void loadGlobalWeights(ValueEventListener listener) {
        ref.child("global").addListenerForSingleValueEvent(listener);
    }

    public void saveGlobalWeights(Map<String, Double> weights) {
        ref.child("global").updateChildren((Map)weights);
    }

    public void loadUserWeights(String uid, ValueEventListener listener) {
        ref.child("users").child(uid).addListenerForSingleValueEvent(listener);
    }

    public void saveUserWeights(String uid, Map<String, Double> weights) {
        ref.child("users").child(uid).updateChildren((Map)weights);
    }
}
