package com.samuel.miwork.sources;

import android.content.Context;

import com.samuel.miwork.utils.MiPrefUtils;

/**
 * @author M.Allaudin
 *         <p>Created on 9/16/2017.</p>
 */

public final class AppStateSource {

    private AppStateSource() {
    }

    public static boolean isLoggedInAsAdmin(Context context) {
        return Boolean.valueOf(MiPrefUtils.get(context, MiPrefUtils.Keys.IS_ADMIN, "false"));
    }

    public static void setLoginAsAdmin(Context context, boolean isAdmin) {
        MiPrefUtils.add(context, MiPrefUtils.Keys.IS_ADMIN, String.valueOf(isAdmin));
    }

    public static void setLimit(Context context, String limit) {
        MiPrefUtils.add(context, MiPrefUtils.Keys.LIMIT, limit);
    }

    public static void setInterestRate(Context context, int interest) {
        MiPrefUtils.add(context, MiPrefUtils.Keys.INTEREST_RATE, String.valueOf(interest));
    }


    public static int getLoanLimit(Context context) {
        return Integer.parseInt(MiPrefUtils.get(context, MiPrefUtils.Keys.LIMIT, "0"));
    }

    public static int getInterestRate(Context context) {
        return Integer.parseInt(MiPrefUtils.get(context, MiPrefUtils.Keys.INTEREST_RATE, "0"));
    }

    public static void setSessionTimedOut(Context context) {
        MiPrefUtils.add(context, MiPrefUtils.Keys.IS_SESSION_TIMED_OUT, String.valueOf(true));
    }

    public static boolean isSessionTimedOut(Context context) {
        return Boolean.valueOf(MiPrefUtils.get(context, MiPrefUtils.Keys.IS_SESSION_TIMED_OUT, "false"));
    }

    public static void clearSessionTimedOut(Context context) {
        MiPrefUtils.add(context, MiPrefUtils.Keys.IS_SESSION_TIMED_OUT, String.valueOf(false));
    }
}
