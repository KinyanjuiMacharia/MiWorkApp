package com.samuel.miwork.utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcelable;

/**
 * @author M.Allaudin
 *         <p>Created on 7/3/2017.</p>
 */

public final class MiActivityUtils {

    private MiActivityUtils() {
    }

    public static void startNewActivity(Activity activity, Class clazz) {
        Intent intent = new Intent(activity, clazz);
        activity.startActivity(intent);
    }

    public static void startNewActivity(Activity activity, Class clazz, String key, Parcelable object) {
        Intent intent = new Intent(activity, clazz);
        intent.putExtra(key, object);
        activity.startActivity(intent);
    }


    public static void startNewActivity(Activity activity, Intent intent) {
        activity.startActivity(intent);
    }

    public static void startNewActivityAndClear(Activity activity, Class clazz) {
        Intent intent = new Intent(activity, clazz);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
    }

} // MiActivityUtils
