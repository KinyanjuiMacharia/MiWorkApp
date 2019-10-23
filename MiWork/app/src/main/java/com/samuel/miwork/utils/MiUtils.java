package com.samuel.miwork.utils;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.View;

import com.samuel.miwork.R;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author M.Allaudin
 *         <p>Created on 9/17/2017.</p>
 */

public final class MiUtils {

    private static String dateFormat = "E, d-MM-yyyy, hh:mm a";
    @SuppressLint("SimpleDateFormat")
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);

    private MiUtils() {
    }

    public static String getFormattedDate(long millis){
        return simpleDateFormat.format(new Date(millis));
    }
    public static boolean isEmpty(String string) {
        return string != null && string.trim().length() == 0;
    }

    public static ProgressDialog getProgressDialog(Context context, String message) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage(message);
        dialog.setCancelable(false);
        return dialog;
    }

    public static ProgressDialog getProgressDialog(Context context) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage(context.getString(R.string.loading));
        dialog.setCancelable(false);
        return dialog;
    }

    public static void disableView(View view) {
        view.setFocusable(false);
        view.setFocusableInTouchMode(false);
    }


    public static boolean isAdminNumber(String number) {
        return number != null && number.equalsIgnoreCase("+254722247862");
//        return number != null && number.equalsIgnoreCase("+923338291874");
    } // isAdminNumber


}
