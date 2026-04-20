package com.melikash98.housesuche.HelperClass;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

/**
 * The WeightManager class is a simple and centralized helper utility for managing
 * the ranking weights used in the intelligent recommendation and scoring system
 * of listings in the HouseSuche application.
 *
 * This class interacts with Firebase Realtime Database to store and retrieve
 * the weights used by the RankingUtils class. These weights determine the importance
 * of each feature (category, distance, keywords, price, and rating) in calculating
 * the final score of each listing.
 *
 * Database structure:
 * RankingWeights/
 * ├── global → default global weights (applied to all users)
 * └── users/
 *     └── {userId} → personalized weights for each user (based on their behavior and preferences)
 *
 * Key features:
 * - loadGlobalWeights() → Loads global weights (used as defaults or when a user has no personalized weights)
 * - saveGlobalWeights() → Saves or updates global weights (typically managed by an admin)
 * - loadUserWeights() → Loads weights specific to a user
 * - saveUserWeights() → Saves personalized user weights (e.g., after learning from user interactions)
 *
 * This class enables RankingUtils to support a dynamic and adaptive ranking system.
 * By storing weights in the database, it allows rapid updates to the algorithm without
 * requiring an application update.
 *
 * It is typically used in settings screens, user profiles, or during automatic weight
 * updates (e.g., via SGD-based learning).
 */

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
