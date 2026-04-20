package com.melikash98.housesuche.HelperClass;

import com.melikash98.housesuche.Models.ItemsModel;

import java.util.ArrayList;

/**
 * The DataHolder class is a simple and lightweight helper class for temporarily storing
 * (in-memory cache) lists of ItemsModel data across the entire application.
 *
 * This class uses static variables to keep three main categories of data accessible
 * throughout all activities and fragments of the application, without the need for
 * persistence in a database or SharedPreferences.
 *
 * Stored lists:
 * - bestModel → List of "best" items (e.g., based on rating, popularity, or special features)
 * - nearModel → List of "nearby" items (based on the user's location)
 * - newModel  → List of "new" items (most recently added listings)
 *
 * The clear() method is used to completely reset all lists in scenarios such as user logout,
 * full data refresh, or when changing city/region.
 *
 * Main purpose of this class: to improve application performance and speed by preventing
 * repeated data fetching from the server across different screens (such as the home page
 * and Best / Near / New tabs).
 *
 * Note: Since the data is stored in memory, it will be lost when the application is fully
 * closed or when the system clears memory due to low resources, which is expected behavior
 * for a temporary cache.
 */

public class DataHolder {
    public static ArrayList<ItemsModel> bestModel = null;
    public static ArrayList<ItemsModel> nearModel = null;
    public static ArrayList<ItemsModel> newModel = null;

    public static void clear() {
        bestModel = null;
        nearModel = null;
        newModel = null;
    }

}
