package com.melikash98.housesuche.HelperClass;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * The ScoreManager class is a powerful helper utility for managing the rating system
 * of real estate listings (Items) in the HouseSuche application.
 *
 * This class leverages Firebase Realtime Database along with the Transaction mechanism
 * to safely handle submitting, updating, retrieving, and removing user ratings without
 * concurrency issues (race conditions).
 *
 * Database structure:
 * Items/{itemId}/score
 *       ├── total      → sum of all ratings
 *       ├── count      → number of users who have rated
 *       ├── average    → average rating (automatically calculated)
 *       └── ratings    → map of userId to that user's rating (prevents duplicate ratings)
 *
 * Key features:
 *
 * - submitScore(): Submits or updates a user's rating for a specific item.
 *   If the user has already rated, the previous rating is subtracted and the new rating is added.
 *
 * - getScoreInfo(): Retrieves complete rating information for an item (total, count, average).
 *
 * - getUserRating(): Retrieves the rating given by a specific user for an item
 *   (useful for displaying the user’s previous rating).
 *
 * - removeUserRating(): Completely removes a user's rating from an item and updates
 *   the total and average accordingly.
 *
 * All critical operations (submitting and removing ratings) are executed using Firebase Transactions
 * to ensure data integrity when multiple requests occur simultaneously.
 *
 * This class is used in sections such as item detail views, user profiles, and other areas
 * related to listing quality, and it supports the intelligent ranking system (RankingUtils)
 * in calculating accurate listing scores.
 *
 * Note: All methods use callbacks to return operation results (success or failure)
 * asynchronously to the caller. Basic input validation is also included to prevent errors.
 */

public class ScoreManager {
    public ScoreManager() {
    }

    public static final int MIN_SCORE = 1;
    public static final int MAX_SCORE = 5;

    public interface Callback {
        void onComplete(boolean success, @Nullable String message);
    }

    public interface ScoreInfoCallback {
        void onResult(boolean success, long total, long count, double average);
    }

    public interface UserRatingCallback {
        void onResult(boolean success, int rating);
    }

    public static void submitScore(@Nullable String itemId, @Nullable String userId, int newScore, @Nullable Callback callback) {
        if (itemId.trim().isEmpty() || userId.trim().isEmpty()) {
            if (callback != null)
                callback.onComplete(false, "Die Artikel-ID oder Benutzer-ID ist ungültig.");
            return;
        }
        if (newScore < MIN_SCORE || newScore > MAX_SCORE) {
            if (callback != null)
                callback.onComplete(false, "Die Punktzahl sollte zwischen " + MIN_SCORE + "Bis" + MAX_SCORE + " dahin.");
            return;
        }
        DatabaseReference scoreRef = FirebaseDatabase.getInstance()
                .getReference("Items")
                .child(itemId)
                .child("score");

        scoreRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Map<String, Object> scoreMap = (Map<String, Object>) currentData.getValue();
                long total = 0;
                long count = 0;
                Map<String, Object> ratings = null;

                if (scoreMap != null) {
                    Object t = scoreMap.get("total");
                    Object c = scoreMap.get("count");
                    Object r = scoreMap.get("ratings");
                    if (t instanceof Number) total = ((Number) t).longValue();
                    else if (t instanceof String) {
                        try {
                            total = Long.parseLong((String) t);
                        } catch (Exception ignored) {
                        }
                    }
                    if (c instanceof Number) count = ((Number) c).longValue();
                    else if (c instanceof String) {
                        try {
                            count = Long.parseLong((String) c);
                        } catch (Exception ignored) {
                        }
                    }
                    if (r instanceof Map) ratings = (Map<String, Object>) r;
                }

                if (ratings == null) ratings = new HashMap<>();
                Integer prev = null;
                Object prevObj = ratings.get(userId);
                if (prevObj instanceof Number) prev = ((Number) prevObj).intValue();
                else if (prevObj instanceof String) {
                    try {
                        prev = Integer.parseInt((String) prevObj);
                    } catch (Exception ignored) {
                    }
                }

                if (prev != null && prev >= MIN_SCORE && prev <= MAX_SCORE) {
                    total = total - prev + newScore;
                } else {
                    total = total + newScore;
                    count = count + 1;
                }

                double average = 0.0;
                if (count > 0) average = ((double) total) / ((double) count);

                Map<String, Object> newScoreMap = new HashMap<>();
                newScoreMap.put("total", total);
                newScoreMap.put("count", count);
                newScoreMap.put("average", average);
                ratings.put(userId, newScore);
                newScoreMap.put("ratings", ratings);

                currentData.setValue(newScoreMap);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (error != null) {
                    if (callback != null)
                        callback.onComplete(false, "Datenbankfehler: " + error.getMessage());
                    return;
                }
                if (!committed) {
                    if (callback != null)
                        callback.onComplete(false, "Die Transaktion ist fehlgeschlagen.");
                    return;
                }
                if (callback != null) callback.onComplete(true, "Spielstand erfasst/aktualisiert.");
            }
        });


    }

    public static void getScoreInfo(@NonNull String itemId, @Nullable ScoreInfoCallback cb) {
        if (itemId.trim().isEmpty()) {
            if (cb != null) cb.onResult(false, 0, 0, 0.0);
            return;
        }

        DatabaseReference scoreRef = FirebaseDatabase.getInstance()
                .getReference("Items")
                .child(itemId)
                .child("score");

        scoreRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long total = null;
                Long count = null;
                Double avg = null;

                Object tObj = snapshot.child("total").getValue();
                Object cObj = snapshot.child("count").getValue();
                Object aObj = snapshot.child("average").getValue();

                if (tObj instanceof Number) total = ((Number) tObj).longValue();
                else if (tObj instanceof String) {
                    try {
                        total = Long.parseLong((String) tObj);
                    } catch (Exception ignored) {
                    }
                }
                if (cObj instanceof Number) count = ((Number) cObj).longValue();
                else if (cObj instanceof String) {
                    try {
                        count = Long.parseLong((String) cObj);
                    } catch (Exception ignored) {
                    }
                }
                if (aObj instanceof Number) avg = ((Number) aObj).doubleValue();
                else if (aObj instanceof String) {
                    try {
                        avg = Double.parseDouble((String) aObj);
                    } catch (Exception ignored) {
                    }
                }

                long t = total != null ? total : 0L;
                long c = count != null ? count : 0L;
                double av = avg != null ? avg : (c == 0 ? 0.0 : ((double) t) / (double) c);

                if (cb != null) cb.onResult(true, t, c, av);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (cb != null) cb.onResult(false, 0, 0, 0.0);
            }
        });
    }

    public static void getUserRating(@NonNull String itemId, @NonNull String userId, @Nullable UserRatingCallback cb) {
        if (itemId.trim().isEmpty() || userId.trim().isEmpty()) {
            if (cb != null) cb.onResult(false, 0);
            return;
        }

        DatabaseReference userRatingRef = FirebaseDatabase.getInstance()
                .getReference("Items")
                .child(itemId)
                .child("score")
                .child("ratings")
                .child(userId);

        userRatingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    if (cb != null) cb.onResult(true, 0);
                    return;
                }
                Integer val = null;
                Object obj = snapshot.getValue();
                if (obj instanceof Number) val = ((Number) obj).intValue();
                else if (obj instanceof String) {
                    try {
                        val = Integer.parseInt((String) obj);
                    } catch (Exception ignored) {
                    }
                }
                if (val == null) val = 0;
                if (cb != null) cb.onResult(true, val);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (cb != null) cb.onResult(false, 0);
            }
        });
    }
    public static void removeUserRating(@NonNull String itemId, @NonNull String userId, @Nullable Callback cb) {
        if (itemId.trim().isEmpty() || userId.trim().isEmpty()) {
            if (cb != null) cb.onComplete(false, "Die Item-ID oder userId ist ungültig.");
            return;
        }

        DatabaseReference scoreRef = FirebaseDatabase.getInstance()
                .getReference("Items")
                .child(itemId)
                .child("score");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference("Items")
                .child(itemId);
        scoreRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Map<String, Object> m = (Map<String, Object>) currentData.getValue();
                long total = 0;
                long count = 0;
                Map<String, Object> ratings = null;

                if (m != null) {
                    Object t = m.get("total");
                    Object c = m.get("count");
                    Object r = m.get("ratings");
                    if (t instanceof Number) total = ((Number) t).longValue();
                    if (c instanceof Number) count = ((Number) c).longValue();
                    if (r instanceof Map) ratings = (Map<String, Object>) r;
                }
                if (ratings == null || !ratings.containsKey(userId)) {
                    return Transaction.success(currentData);
                }
                Integer prev = null;
                Object prevObj = ratings.get(userId);
                if (prevObj instanceof Number) prev = ((Number) prevObj).intValue();
                else if (prevObj instanceof String) {
                    try { prev = Integer.parseInt((String) prevObj); } catch (Exception ignored) {}
                }
                if (prev == null) prev = 0;

                total = total - prev;
                count = Math.max(0, count - 1);
                double avg = (count > 0) ? ((double) total) / ((double) count) : 0.0;
                databaseReference.child("averageScore").setValue(avg);

                ratings.remove(userId);

                Map<String, Object> newMap = new HashMap<>();
                newMap.put("total", total);
                newMap.put("count", count);
                newMap.put("average", avg);
                newMap.put("ratings", ratings);

                currentData.setValue(newMap);
                return Transaction.success(currentData);
            }


            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (error != null) {
                    if (cb != null) cb.onComplete(false, "Fehler: " + error.getMessage());
                    return;
                }
                if (cb != null) cb.onComplete(committed, committed ? "Die Abstimmung wurde gelöscht." : "Die Transaktion ist fehlgeschlagen.");
            }
        });
    }
}
