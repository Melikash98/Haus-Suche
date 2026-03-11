package com.melikash98.housesuche.HelperClass;

import com.melikash98.housesuche.Models.ItemsModel;

import java.util.ArrayList;

/**
 * DataHolder
 *
 * This class is used as a temporary in-memory data container
 * for storing lists of items that are used across different
 * parts of the application.
 *
 * The lists are static so they can be accessed globally
 * without passing them between Activities or Fragments.
 *
 * Use case:
 * - Store items that are already fetched from Firebase
 * - Avoid multiple database calls
 * - Share item lists between screens
 *
 * Lists stored:
 * - bestModel : items with the highest score (Best items)
 * - nearModel : items near the user's location
 * - newModel  : recently added items
 *
 * Important:
 * This class does NOT persist data. It only holds data
 * temporarily in memory while the app is running.
 */

public class DataHolder {
    /**
     * List of best scored items (used in Explore -> Best section)
     */
    public static ArrayList<ItemsModel> bestModel = null;
    /**
     * List of items near the user's location
     */
    public static ArrayList<ItemsModel> nearModel = null;
    /**
     * List of newly added items
     */
    public static ArrayList<ItemsModel> newModel = null;
    /**
     * Clears all cached item lists.
     *
     * This method is useful when:
     * - User logs out
     * - Data needs to be refreshed
     * - Prevent using outdated cached data
     */
    public static void clear() {
        bestModel = null;
        nearModel = null;
        newModel = null;
    }

}
