package com.samuel.miwork.utils;

import android.util.Log;

/**
 * @author M.Allaudin
 *         <p>Created on 9/15/2017.</p>
 */

public final class MiLogger {

    private static final String TAG = "MiWorkApp";
    private MiLogger(){}

    public static void d(String format, Object... args){
        Log.d(TAG, String.format(format, args));
    } // d

} // MiLogger
