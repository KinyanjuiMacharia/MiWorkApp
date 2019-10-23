package com.samuel.miwork.utils;

import android.content.Context;

import com.google.gson.Gson;

/**
 * Utility class for saving and retrieving data from shared preferences
 *
 * @author M.Allaudin
 *         <p>Created on 6/22/2017.</p>
 */

public final class MiPrefUtils {

    private static final String PREF_NAME = "ojo.prefs";


    private MiPrefUtils() {

    }

    /**
     * Add value to shared prefs
     *
     * @param context context for shared prefs
     * @param key     key
     * @param value   value for given key
     */
    public static void add(Context context, String key, String value) {
        if (context == null) {
            return;
        }
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit().putString(key, value)
                .apply();
    } // add

    public static void remove(Context context, String key) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit().remove(key).apply();
    }


    /**
     * Get string from prefs with given default value
     *
     * @param context context for shared prefs
     * @param key     key for this field
     * @param def     default value
     * @return value as String
     */
    public static String get(Context context, String key, String def) {
        if (context == null) {
            return "";
        }
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(key, def);
    }

    /**
     * Get String with given key and default to empty string
     *
     * @param context context for shared prefs
     * @param key     pref key
     * @return String against this key
     */
    public static String get(Context context, String key) {
        return get(context, key, "");
    }


    /**
     * Get JSON from prefs and convert it to given model
     *
     * @param context context for shared prefs
     * @param key     pref key
     * @param clazz   class of model
     * @param <T>     type of model
     * @return T modeled Json
     */
    public static <T> T getAsJson(Context context, String key, Class<T> clazz) {
        String json = get(context, key, "{}");
        return new Gson().fromJson(json, clazz);
    }

    /**
     * Preferences keys
     */
    public static class Keys {
        public static final String IS_ADMIN = String.valueOf("is_admin".hashCode());
        public static final String LIMIT = String.valueOf("limit".hashCode());
        public static final String INTEREST_RATE = String.valueOf("interest_rate".hashCode());
        public static final String IS_SESSION_TIMED_OUT = String.valueOf("is_session_timed_out".hashCode());
    } // Keys

} // MiPrefUtils
