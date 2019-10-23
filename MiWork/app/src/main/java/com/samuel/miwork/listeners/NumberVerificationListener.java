package com.samuel.miwork.listeners;

import android.app.Dialog;

/**
 * @author M.Allaudin
 *         <p>Created on 9/16/2017.</p>
 */

public interface NumberVerificationListener {
    void onVerify(Dialog dialog, String code);
    void onVerificationCanceled(Dialog dialog);
}
