package com.samuel.miwork.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.samuel.miwork.R;
import com.samuel.miwork.activities.LoginActivity;
import com.samuel.miwork.listeners.MiTextWatcher;
import com.samuel.miwork.listeners.NumberVerificationListener;

import io.github.allaudin.annotations.FactoryType;
import io.github.allaudin.annotations.OxyViews;
import io.github.allaudin.oxygeroid.VerificationDialogViews;

/**
 * @author M.Allaudin
 *         <p>Created on 9/15/2017.</p>
 */

@OxyViews(value = "dialog_number_verification", type = FactoryType.VIEW, className = "VerificationDialogViews")
public final class MiDialogs {
    private MiDialogs() {
    }

    public static void showMessageDialog(Context context, String message) {
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .create();
        dialog.show();
    } // showMessageDialog

    private static TextWatcher getTextWatcher(final EditText nextEditText) {
        return new MiTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                nextEditText.setBackgroundResource(R.drawable.input_style);
            }
        };
    }

    public static LoginActivity.UpdateFieldsListener showNumberVerificationDialog(Context context, final NumberVerificationListener verificationListener) {
        View view = View.inflate(context, R.layout.dialog_number_verification, null);
        final AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .setCancelable(false)
                .create();

        final VerificationDialogViews views = VerificationDialogViews.newInstance(view);
        views.pinCode.addTextChangedListener(getTextWatcher(views.pinCode));

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.verify) {
                    String code = views.pinCode.getText().toString();
                    if (MiUtils.isEmpty(code)) {
                        views.pinCode.setBackgroundResource(R.drawable.input_style_red);
                        return;
                    }
                    verificationListener.onVerify(dialog, code);
                } else {
                    verificationListener.onVerificationCanceled(dialog);
                }
            }
        };

        views.cancel.setOnClickListener(clickListener);
        views.verify.setOnClickListener(clickListener);
        dialog.show();
        return new LoginActivity.UpdateFieldsListener() {
            @Override
            public void onUpdate(String code, boolean isAutoFetched) {
                views.pinCode.setText(code);
                if (isAutoFetched) {
                    views.verify.callOnClick();
                }
            }

            @Override
            public Dialog getDialog() {
                return dialog;
            }
        };
    } // showMessageDialog


}
