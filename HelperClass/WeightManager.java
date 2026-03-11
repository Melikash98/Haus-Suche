package com.melikash98.housesuche.HelperClass;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;
/**
 * WeightManager
 *
 * This class is responsible for managing ranking weights
 * used by the recommendation / ranking system of the application.
 *
 * The weights influence how items are ranked in the Explore section
 * and are used by the RankingUtils class when calculating item scores.
 *
 * Two types of weights are supported:
 *
 * 1. Global Weights
 *    Default weights used for all users.
 *
 * 2. User-Specific Weights
 *    Personalized weights that adapt to user behavior
 *    (for example based on viewed or interacted items).
 *
 * Firebase Structure:
 *
 * RankingWeights
 *   ├── global
 *   │     ├── category
 *   │     ├── distance
 *   │     ├── keyword
 *   │     ├── price
 *   │     └── rating
 *   │
 *   └── users
 *         └── uid
 *               ├── category
 *               ├── distance
 *               ├── keyword
 *               ├── price
 *               └── rating
 *
 * These weights are later converted into arrays
 * and used by RankingUtils for scoring items.
 */
public class WeightManager {
    /**
     * Root path in Firebase where ranking weights are stored.
     */
    private static final String PATH = "RankingWeights";
    /**
     * Firebase database reference to the ranking weights node.
     */
    private DatabaseReference ref = FirebaseDatabase.getInstance().getReference(PATH);
    /**
     * Loads the global ranking weights from Firebase.
     *
     * These weights act as the default ranking configuration
     * for all users in the system.
     *
     * @param listener Firebase ValueEventListener that receives the data snapshot
     */
    public void loadGlobalWeights(ValueEventListener listener) {
        ref.child("global").addListenerForSingleValueEvent(listener);
    }
    /**
     * Saves or updates the global ranking weights.
     *
     * The provided weights map should contain keys such as:
     * - category
     * - distance
     * - keyword
     * - price
     * - rating
     *
     * @param weights Map containing ranking weight values
     */
    public void saveGlobalWeights(Map<String, Double> weights) {
        ref.child("global").updateChildren((Map)weights);
    }
    /**
     * Loads personalized ranking weights for a specific user.
     *
     * These weights can adapt to user preferences based on
     * previous interactions or viewed items.
     *
     * @param uid      User ID
     * @param listener Firebase ValueEventListener that receives the data snapshot
     */
    public void loadUserWeights(String uid, ValueEventListener listener) {
        ref.child("users").child(uid).addListenerForSingleValueEvent(listener);
    }
    /**
     * Saves or updates personalized ranking weights for a user.
     *
     * This allows the ranking system to adapt to
     * individual user behavior.
     *
     * @param uid     User ID
     * @param weights Map containing personalized ranking weights
     */
    public void saveUserWeights(String uid, Map<String, Double> weights) {
        ref.child("users").child(uid).updateChildren((Map)weights);
    }
}
