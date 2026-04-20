package com.melikash98.housesuche.HelperClass;

import com.melikash98.housesuche.Models.ItemsModel;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * The RankingUtils class is a powerful and comprehensive utility for the scoring and ranking system
 * of real estate listings (ItemsModel) in the HouseSuche application.
 *
 * This class uses intelligent algorithms to calculate the score of each listing based on user
 * preferences, ensuring that search results, "Best" lists, and personalized recommendations are
 * more accurate and relevant.
 *
 * Key features of this class:
 *
 * • Calculates precise geographical distance between the user and listings (using the Haversine formula)
 * • Scores listings based on distance (proximity), category, keywords, price, and user ratings
 * • Computes keyword similarity using tokenization and set intersection
 * • Normalizes price scores relative to the user's budget
 * • Provides two final scoring methods: Linear Weighted Sum and Sigmoid (for non-linear behavior)
 * • Supports weight learning using a simple SGD (Stochastic Gradient Descent) algorithm based on
 *   listings viewed by the user
 * • Converts weights between Array and Map formats for easier usage
 * • Includes helper functions for extracting the most frequent category and the user's average budget
 *
 * This class acts as the core engine of the application's recommendation system and is used in sections such as:
 * - Home screen (Best / Near / New)
 * - Search results
 * - Personalized recommendations
 *
 * All scores are normalized within the range of 0 to 100, and clamp01 is used to prevent invalid values.
 * Default weights: category (30%), distance (25%), keywords (15%), price (15%), rating (15%)
 */

public class RankingUtils {
    private static final Pattern TOKEN_SPLIT = Pattern.compile("\\W+");
    public static Map<String, Double> userWeights = new HashMap<>();

    public static double distanceInKm(double lat1, double lon1, double lat2, double lon2) {
        if (Double.isNaN(lat1) || Double.isNaN(lon1) || Double.isNaN(lat2) || Double.isNaN(lon2))
            return Double.MAX_VALUE;
        if ((lat1 == 0.0 && lon1 == 0.0) || (lat2 == 0.0 && lon2 == 0.0))
            return Double.MAX_VALUE;
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public static double distanceScore(double distanceKm, double maxDistanceKm) {
        if (maxDistanceKm <= 0) maxDistanceKm = 1.0;
        if (distanceKm <= 0) return 1.0;
        if (distanceKm >= maxDistanceKm) return 0.0;
        double v = 1.0 - (distanceKm / maxDistanceKm);
        return clamp01(v);
    }

    public static double parsePrice(String priceStr) {
        if (priceStr == null) return 0.0;
        try {
            String cleaned = priceStr.replaceAll(",", "");
            cleaned = cleaned.replaceAll("[^0-9.]", "");
            if (cleaned.isEmpty()) return 0.0;
            return Double.parseDouble(cleaned);
        } catch (Exception e) {
            return 0.0;
        }
    }

    public static double normalizePriceScore(String itemPriceStr, Double userBudget) {
        if (userBudget != null && userBudget > 0) {
            double itemPrice = parsePrice(itemPriceStr);
            if (itemPrice <= 0) return 0.5;
            double ratio = Math.min(itemPrice, userBudget) / Math.max(itemPrice, userBudget);
            return clamp01(ratio);
        } else {
            return 0.5;
        }
    }

    private static Set<String> tokenizeToSet(String s) {
        Set<String> set = new HashSet<>();
        if (s == null) return set;
        String[] toks = TOKEN_SPLIT.split(s.toLowerCase(Locale.ROOT));
        for (String t : toks) {
            if (t != null) {
                String tt = t.trim();
                if (!tt.isEmpty()) set.add(tt);
            }
        }
        return set;
    }

    public static double keywordSimilarity(String userQuery, String text) {
        if ((userQuery == null || userQuery.trim().isEmpty()) && (text == null || text.trim().isEmpty())) {
            return 0.0;
        }
        Set<String> a = tokenizeToSet(userQuery == null ? "" : userQuery);
        Set<String> b = tokenizeToSet(text == null ? "" : text);
        if (a.isEmpty() || b.isEmpty()) {
            return 0.0;
        }
        int common = 0;
        for (String s : a) if (b.contains(s)) common++;
        double avg = (a.size() + b.size()) / 2.0;
        if (avg <= 0) return 0.0;
        double sim = common / avg;
        return clamp01(sim);
    }

    public static double normalizeRating(Double averageRating) {
        if (averageRating == null) return 0.5;
        try {
            double v = averageRating / 5.0;
            return clamp01(v);
        } catch (Exception e) {
            return 0.5;
        }
    }
    public static double[] buildFeatureVector(
            ItemsModel item,
            String userCategory,
            String userQueryKeywords,
            double userLat, double userLon,
            Double userBudget,
            double maxDistanceKm
    ) {
        double catScore = 0.0;
        try {
            if (userCategory != null && !userCategory.trim().isEmpty()) {
                if (item != null && item.getCategoryId() != null && item.getCategoryId().equalsIgnoreCase(userCategory.trim())) {
                    catScore = 1.0;
                } else {
                    catScore = 0.0;
                }
            } else {
                catScore = 0.5;
            }
        } catch (Exception e) {
            catScore = 0.0;
        }

        double distScore;
        try {
            if (userLat != 0.0 || userLon != 0.0) {
                if (item != null && item.getLatitude() != 0.0 && item.getLongitude() != 0.0) {
                    double d = distanceInKm(userLat, userLon, item.getLatitude(), item.getLongitude());
                    if (d == Double.MAX_VALUE) distScore = 0.0;
                    else distScore = distanceScore(d, maxDistanceKm);
                } else {
                    distScore = 0.5;
                }
            } else {
                try {
                    if (item != null && item.getCity() != null && userQueryKeywords != null && userQueryKeywords.toLowerCase(Locale.ROOT).contains(item.getCity().toLowerCase(Locale.ROOT))) {
                        distScore = 1.0;
                    } else {
                        distScore = 0.5;
                    }
                } catch (Exception e) {
                    distScore = 0.5;
                }
            }
        } catch (Exception e) {
            distScore = 0.5;
        }

        double kw = 0.0;
        try {
            kw = keywordSimilarity(userQueryKeywords == null ? "" : userQueryKeywords, item == null ? "" : (item.getOverview() == null ? "" : item.getOverview()));
        } catch (Exception e) {
            kw = 0.0;
        }

        double priceScore = normalizePriceScore(item == null ? null : item.getPrice(), userBudget);

        double ratingScore = 0.5;
        try {
            if (item != null && item.getScore() != null) {
                ratingScore = normalizeRating((double) item.getScore().average);
            } else {
                ratingScore = 0.5;
            }
        } catch (Exception e) {
            ratingScore = 0.5;
        }

        return new double[] {
                clamp01(catScore),
                clamp01(distScore),
                clamp01(kw),
                clamp01(priceScore),
                clamp01(ratingScore)
        };
    }

    public static double scoreFromWeightsLinear(double[] features, double[] weights) {
        if (features == null || weights == null) return 0.0;
        int n = Math.min(features.length, weights.length);
        if (n == 0) return 0.0;
        double sum = 0.0;
        double wsum = 0.0;
        for (int i = 0; i < n; i++) {
            sum += features[i] * weights[i];
            wsum += Math.abs(weights[i]);
        }
        double norm;
        if (wsum <= 1e-9) {
            norm = 0.0;
            for (double f : features) norm += f;
            norm = norm / Math.max(1, features.length);
        } else {
            norm = sum / wsum;
        }
        norm = clamp01(norm);
        return norm * 100.0;
    }

    public static double scoreFromWeightsSigmoid(double[] features, double[] weights) {
        if (features == null || weights == null) return 0.0;
        int n = Math.min(features.length, weights.length);
        double dot = 0.0;
        for (int i = 0; i < n; i++) dot += features[i] * weights[i];
        if (dot > 80) dot = 80;
        if (dot < -80) dot = -80;
        double pred = sigmoid(dot);
        return clamp01(pred) * 100.0;
    }
    public static double computeItemScore(
            ItemsModel item,
            String userCategory,
            String userQueryKeywords,
            double userLat, double userLon,
            Double userBudget,
            double[] weights,
            double maxDistanceKm
    ) {
        double[] features = buildFeatureVector(item, userCategory, userQueryKeywords, userLat, userLon, userBudget, maxDistanceKm);
        return scoreFromWeightsLinear(features, weights);
    }

    public static double computeItemScore(
            ItemsModel item,
            String userCategory,
            String userQueryKeywords,
            double userLat, double userLon,
            Double userBudget,
            Map<String, Double> weightsMap,
            double maxDistanceKm
    ) {
        double[] w = weightsMapToArray(weightsMap);
        return computeItemScore(item, userCategory, userQueryKeywords, userLat, userLon, userBudget, w, maxDistanceKm);
    }

    public static double sigmoid(double x) {
        if (x >= 0) {
            double z = Math.exp(-x);
            return 1.0 / (1.0 + z);
        } else {
            double z = Math.exp(x);
            return z / (1.0 + z);
        }
    }

    public static double[] sgdUpdate(double[] weights, double[] features, double label, double lr, double l2) {
        if (weights == null || features == null) return weights;
        int n = Math.min(weights.length, features.length);
        double dot = 0.0;
        for (int i = 0; i < n; i++) dot += weights[i] * features[i];
        double pred = sigmoid(dot);
        for (int i = 0; i < n; i++) {
            double grad = (pred - label) * features[i] + l2 * weights[i];
            weights[i] = weights[i] - lr * grad;
        }
        clipWeights(weights, -10.0, 10.0);
        return weights;
    }

    private static double clamp01(double x) {
        if (Double.isNaN(x)) return 0.0;
        if (x < 0.0) return 0.0;
        if (x > 1.0) return 1.0;
        return x;
    }

    public static void clipWeights(double[] w, double min, double max) {
        if (w == null) return;
        for (int i = 0; i < w.length; i++) {
            if (Double.isNaN(w[i]) || Double.isInfinite(w[i])) w[i] = 0.0;
            if (w[i] < min) w[i] = min;
            if (w[i] > max) w[i] = max;
        }
    }

    public static double[] defaultGlobalWeights() {
        return new double[]{0.30, 0.25, 0.15, 0.15, 0.15};
    }

    public static String featuresToString(double[] features) {
        if (features == null) return "null";
        return Arrays.toString(features);
    }
    public static double[] weightsMapToArray(Map<String, Double> map) {
        double[] w = defaultGlobalWeights();
        if (map == null) return w;
        try {
            w[0] = map.containsKey("category") ? map.get("category") : w[0];
            w[1] = map.containsKey("distance") ? map.get("distance") : w[1];
            w[2] = map.containsKey("keyword") ? map.get("keyword") : w[2];
            w[3] = map.containsKey("price") ? map.get("price") : w[3];
            w[4] = map.containsKey("rating") ? map.get("rating") : w[4];
        } catch (Exception ignored) {}
        return w;
    }

    public static Map<String, Double> weightsArrayToMap(double[] w) {
        Map<String, Double> map = new HashMap<>();
        double[] def = defaultGlobalWeights();
        if (w == null) w = def;
        map.put("category", w.length > 0 ? w[0] : def[0]);
        map.put("distance", w.length > 1 ? w[1] : def[1]);
        map.put("keyword", w.length > 2 ? w[2] : def[2]);
        map.put("price", w.length > 3 ? w[3] : def[3]);
        map.put("rating", w.length > 4 ? w[4] : def[4]);
        return map;
    }

    public static Map<String, Double> updateWeights(Map<String, Double> existing, List<ItemsModel> viewedItems) {
        double[] weights = weightsMapToArray(existing);
        if (viewedItems == null || viewedItems.isEmpty()) {
            return weightsArrayToMap(weights);
        }
        Double userBudget = null;
        try {
            double sum = 0.0;
            int cnt = 0;
            for (ItemsModel it : viewedItems) {
                double p = parsePrice(it == null ? null : it.getPrice());
                if (p > 0) {
                    sum += p; cnt++;
                }
            }
            if (cnt > 0) userBudget = sum / cnt;
        } catch (Exception ignored) {}

        double lr = 0.05;
        double l2 = 0.001;
        double maxDistanceKm = 50.0;

        for (ItemsModel it : viewedItems) {
            double[] feat = buildFeatureVector(it, "", "", 0.0, 0.0, userBudget, maxDistanceKm);
            sgdUpdate(weights, feat, 1.0, lr, l2);
        }
        clipWeights(weights, -5.0, 5.0);
        return weightsArrayToMap(weights);
    }
    public static String getMostFrequentCategory(List<ItemsModel> items) {
        if (items == null || items.isEmpty()) return "";

        Map<String, Integer> catCount = new HashMap<>();
        for (ItemsModel item : items) {
            if (item == null) continue;
            String cat = item.getCategoryName();
            if (cat != null && !cat.trim().isEmpty()) {
                catCount.put(cat, catCount.getOrDefault(cat, 0) + 1);
            }
        }
        if (catCount.isEmpty()) return "";
        return Collections.max(catCount.entrySet(), Map.Entry.comparingByValue()).getKey();
    }
    public static Double calculateAverageBudget(List<ItemsModel> items) {
        if (items == null || items.isEmpty()) return null;

        double sum = 0.0;
        int cnt = 0;
        for (ItemsModel i : items) {
            if (i == null) continue;
            double p = parsePrice(i.getPrice());
            if (p > 0) {
                sum += p;
                cnt++;
            }
        }
        return cnt > 0 ? sum / cnt : null;
    }
}
