package com.samuel.miwork;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.samuel.miwork.listeners.MiTextWatcher;
import com.samuel.miwork.models.UserModel;
import com.samuel.miwork.sources.AppStateSource;
import com.samuel.miwork.utils.MiDialogs;
import com.samuel.miwork.utils.MiUtils;

import io.github.allaudin.annotations.FactoryType;
import io.github.allaudin.annotations.OxyViews;
import io.github.allaudin.oxygeroid.NewPinDialogViews;
import io.github.allaudin.oxygeroid.PinDialogViews;

/**
 * @author M.Allaudin
 *         <p>Created on 9/18/2017.</p>
 */

@OxyViews(value = "dialog_pin_verification", className = "PinDialogViews", type = FactoryType.VIEW)
public final class TimeOutManager {

    private AppCompatActivity activity;
    private PinVerifier verifier;
    private PinDialogViews vdViews;

    private static TextWatcher getTextWatcher(final EditText thisEditText) {
        return new MiTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                thisEditText.setBackgroundResource(R.drawable.input_style);
            }
        };
    }

    private TimeOutManager(AppCompatActivity activity) {
        this.activity = activity;
    }

    public static TimeOutManager getInstance(AppCompatActivity activity) {
        return new TimeOutManager(activity);
    }

    public void checkSession(PinVerifier verifier) {
        this.verifier = verifier;
        if (AppStateSource.isSessionTimedOut(activity)) {
            showVerificationDialog();
        } else {
            callPinVerified();
        }
    }


    private void verifyPin(final String pin) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callPinVerifiedFailed();
            return;
        }

        final ProgressDialog verifyDialog = new ProgressDialog(activity);
        verifyDialog.setCancelable(false);
        verifyDialog.setMessage(activity.getString(R.string.loading));
        verifyDialog.show();

        FirebaseDatabase.getInstance().getReference(FirebaseRefs.USERS)
                .child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        UserModel user = dataSnapshot.getValue(UserModel.class);
                        if (user != null && user.hasSamePin(pin)) {
                            verifyDialog.dismiss();
                            callPinVerified();
                        } else {
                            verifyDialog.dismiss();
                            callPinVerifiedFailed();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        verifyDialog.dismiss();
                        callPinVerifiedFailed();
                    }
                });

    } // verifyPin

    private void callPinVerifiedFailed() {
        verifier.onPinVerificationFailed();
    }

    private void verifyNationalId(final String nationalId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            MiDialogs.showMessageDialog(activity, activity.getString(R.string.couldnt_verify));
            return;
        }

        final ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(activity.getString(R.string.loading));
        progressDialog.show();

        FirebaseDatabase.getInstance().getReference(FirebaseRefs.USERS)
                .child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        UserModel user = dataSnapshot.getValue(UserModel.class);
                        if (user != null && user.hasSameNationalId(nationalId)) {
                            progressDialog.dismiss();
                            showNewPinDialog();
                        } else {
                            progressDialog.dismiss();
                            MiDialogs.showMessageDialog(activity, activity.getString(R.string.wrong_national_id));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        progressDialog.dismiss();
                        MiDialogs.showMessageDialog(activity, activity.getString(R.string.couldnt_verify));
                    }
                });

    } // verifyNationalId


    private void showVerificationDialog() {
        View layout = View.inflate(activity, R.layout.dialog_pin_verification, null);
        final AlertDialog dialog = new AlertDialog.Builder(activity)
                .setCancelable(false)
                .setView(layout)
                .create();
        vdViews = PinDialogViews.newInstance(layout);

        vdViews.pinEditText.addTextChangedListener(getTextWatcher(vdViews.pinEditText));

        vdViews.verify.setTag(dialog);
        vdViews.verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onVerifyPin(dialog);
            }
        });
        vdViews.forgotPin.setTag(dialog);
        vdViews.forgotPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showResetPinDialog();
            }
        });
        dialog.show();
    } // getVerificationDialog

    private void onVerifyPin(AlertDialog dialog) {
        String pinCode = vdViews.pinEditText.getText().toString();

        if (MiUtils.isEmpty(pinCode)) {
            vdViews.pinEditText.setBackgroundResource(R.drawable.input_style_red);
            return;
        }

        dialog.dismiss();
        verifyPin(pinCode);
    }

    private void showResetPinDialog() {
        View view = View.inflate(activity, R.layout.dialog_forgot_pin, null);
        final AlertDialog dialog = new AlertDialog.Builder(activity)
                .setView(view)
                .setCancelable(false)
                .create();
        final EditText nationalId = (EditText) view.findViewById(R.id.national_id);
        nationalId.addTextChangedListener(new MiTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                nationalId.setBackgroundResource(R.drawable.input_style);
            }
        });
        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        view.findViewById(R.id.reset_pin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = nationalId.getText().toString();
                if (MiUtils.isEmpty(id)) {
                    nationalId.setBackgroundResource(R.drawable.input_style_red);
                    return;
                }
                dialog.dismiss();
                verifyNationalId(id);
            }
        });
        dialog.show();
    } // showResetPinDialog

    private void showNewPinDialog() {
        View view = View.inflate(activity, R.layout.dialog_new_pin, null);
        final NewPinDialogViews newPinDialogViews = NewPinDialogViews.newInstance(view);

        newPinDialogViews.pinCode.addTextChangedListener(getTextWatcher(newPinDialogViews.pinCode));

        final AlertDialog dialog = new AlertDialog.Builder(activity)
                .setView(view)
                .setCancelable(false)
                .create();
        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        view.findViewById(R.id.done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (MiUtils.isEmpty(newPinDialogViews.pinCode.getText().toString())) {
                    newPinDialogViews.pinCode.setBackgroundResource(R.drawable.input_style);
                    return;
                }

                final String pin = newPinDialogViews.pinCode.getText().toString();
                dialog.dismiss();
                final ProgressDialog progressDialog = new ProgressDialog(activity);
                progressDialog.setCancelable(false);
                progressDialog.setMessage(activity.getString(R.string.setting_new_pin));
                progressDialog.show();
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) {
                    progressDialog.dismiss();
                    MiDialogs.showMessageDialog(activity, activity.getString(R.string.couldnt_verify));
                    return;
                }

                FirebaseDatabase.getInstance().getReference(FirebaseRefs.USERS)
                        .child(user.getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                UserModel userModel = dataSnapshot.getValue(UserModel.class);
                                if (userModel != null) {
                                    userModel.setHashedPin(pin);
                                    FirebaseDatabase.getInstance().getReference(FirebaseRefs.USERS)
                                            .child(user.getUid()).setValue(userModel)
                                            .addOnCompleteListener(activity, new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(activity, activity.getString(R.string.pin_updated), Toast.LENGTH_SHORT).show();
                                                    callPinVerified();
                                                }
                                            }).addOnFailureListener(activity, new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog.dismiss();
                                            MiDialogs.showMessageDialog(activity, e.getMessage());
                                        }
                                    });
                                } else {
                                    progressDialog.dismiss();
                                    MiDialogs.showMessageDialog(activity, activity.getString(R.string.pin_update_failed));
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                progressDialog.dismiss();
                                MiDialogs.showMessageDialog(activity, databaseError.getMessage());
                            }
                        });
            }
        });
        dialog.show();
    } // showResetPinDialog

    private void callPinVerified() {
        verifier.onPinVerified();
    }


    public interface PinVerifier {
        void onPinVerified();

        void onPinVerificationFailed();
    } // PinVerifier

    @OxyViews(value = "dialog_new_pin", className = "NewPinDialogViews", type = FactoryType.VIEW)
    private class NewPinDialogs {
    }

} // TimeOutManager
