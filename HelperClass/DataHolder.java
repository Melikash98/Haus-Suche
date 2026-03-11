package com.melikash98.housesuche.HelperClass;

import com.melikash98.housesuche.Models.ItemsModel;

import java.util.ArrayList;

/**
 * DataHolder is a utility class that temporarily holds categorized lists of ItemsModel objects.
 * <p>
 * This class provides static references to:
 * <ul>
 *     <li>bestModel - items marked as "best" or featured</li>
 *     <li>nearModel - items located near the user</li>
 *     <li>newModel - newly added items</li>
 * </ul>
 * <p>
 * These lists are intended for in-memory storage and quick access without repeated database queries.
 * Use {@link #clear()} to reset all lists and free memory.
 *
 * <b>Note:</b> All fields are static, so the data is shared across the entire app session.
 */
public class DataHolder {
    /** List of featured or "best" items */
    public static ArrayList<ItemsModel> bestModel = null;
    /** List of items located near the user */
    public static ArrayList<ItemsModel> nearModel = null;
    /** List of newly added items */
    public static ArrayList<ItemsModel> newModel = null;
    /**
     * Clears all stored item lists.
     * <p>
     * This method resets {@link #bestModel}, {@link #nearModel}, and {@link #newModel} to null.
     * Call this method when the data needs to be refreshed or when the app is logging out the user.
     */
    public static void clear() {
        bestModel = null;
        nearModel = null;
        newModel = null;
    }

}
