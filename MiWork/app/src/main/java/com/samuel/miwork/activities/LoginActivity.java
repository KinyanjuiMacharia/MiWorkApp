package com.samuel.miwork.activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.samuel.miwork.FirebaseRefs;
import com.samuel.miwork.R;
import com.samuel.miwork.listeners.MiItemSelectedListener;
import com.samuel.miwork.listeners.MiTextWatcher;
import com.samuel.miwork.listeners.NumberVerificationListener;
import com.samuel.miwork.models.UserModel;
import com.samuel.miwork.sources.AppStateSource;
import com.samuel.miwork.utils.MiActivityUtils;
import com.samuel.miwork.utils.MiDialogs;
import com.samuel.miwork.utils.MiUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.github.allaudin.annotations.OxyViews;
import io.github.allaudin.oxygeroid.LoginActivityViews;

@OxyViews("activity_login")
public class LoginActivity extends BaseActivity implements View.OnClickListener, NumberVerificationListener {

    private String mVerificationId;
    private LoginActivityViews mViews;
    private List<String> mNumberPrefixes;
    private String mCurrentSelectedPrefix;
    private boolean mShowingVerifyDialog = false;
    private UpdateFieldsListener mUpdateFieldsListener;
    private String msisdn;


    @Override
    public void afterOnCreate(@Nullable Bundle savedInstanceState) {
        mViews = LoginActivityViews.newInstance(this);
        mNumberPrefixes = getNumberPrefixes();
        mCurrentSelectedPrefix = mNumberPrefixes.get(0);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, mNumberPrefixes);
        mViews.prefixNumberSpinner.setAdapter(adapter);
        mViews.prefixNumberSpinner.setOnItemSelectedListener(new MiItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCurrentSelectedPrefix = mNumberPrefixes.get(position);
            }
        });

        mViews.loginButton.setOnClickListener(this);
        mViews.numberEditText.addTextChangedListener(new MiTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mViews.numberError.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public int getTitleId() {
        return 0;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_login;
    }


    private List<String> getNumberPrefixes() {
        List<String> prefixList = new ArrayList<>();
        prefixList.add("+254 700");
        prefixList.add("+254 701");
        prefixList.add("+254 702");
        prefixList.add("+254 703");
        prefixList.add("+254 704");
        prefixList.add("+254 705");
        prefixList.add("+254 706");
        prefixList.add("+254 707");
        prefixList.add("+254 708");
        prefixList.add("+254 710");
        prefixList.add("+254 711");
        prefixList.add("+254 712");
        prefixList.add("+254 713");
        prefixList.add("+254 714");
        prefixList.add("+254 715");
        prefixList.add("+254 716");
        prefixList.add("+254 717");
        prefixList.add("+254 718");
        prefixList.add("+254 719");
        prefixList.add("+254 720");
        prefixList.add("+254 721");
        prefixList.add("+254 722");
        prefixList.add("+254 723");
        prefixList.add("+254 724");
        prefixList.add("+254 725");
        prefixList.add("+254 726");
        prefixList.add("+254 727");
        prefixList.add("+254 728");
        prefixList.add("+254 729");
        return prefixList;
    } // getNumberPrefixes

    @Override
    public void onClick(View v) {
        String inputNumber = mViews.numberEditText.getText().toString();

        if (TextUtils.isEmpty(inputNumber) || inputNumber.length() != 6) {
            mViews.numberError.setVisibility(View.VISIBLE);
            mViews.numberError.setText(getString(R.string.please_enter_number));
            return;
        }
        loginWithPhone(inputNumber);


    } // onClick

    private void loginWithPhone(final String inputNumber) {
        final ProgressDialog signInProgressDialog = new ProgressDialog(this);
        signInProgressDialog.setMessage(getString(R.string.verifying_number));
        signInProgressDialog.show();
        msisdn = mCurrentSelectedPrefix.trim().replaceAll(" ", "") + inputNumber;
//        msisdn = "+923338291874";
//        msisdn = "+923138833257"; // client


        PhoneAuthProvider.getInstance().verifyPhoneNumber(msisdn, 60,
                TimeUnit.SECONDS, this, new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                        if (mUpdateFieldsListener != null) {
                            signInProgressDialog.dismiss();
                            mUpdateFieldsListener.onUpdate(phoneAuthCredential.getSmsCode(), true);
                        } else {
                            signInProgressDialog.setMessage(getString(R.string.signing_in));
                            signInWithCredentials(phoneAuthCredential, signInProgressDialog);
                        }
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        signInProgressDialog.dismiss();
                        if (mShowingVerifyDialog && mUpdateFieldsListener != null) {
                            mUpdateFieldsListener.getDialog().dismiss();
                        }
                        MiDialogs.showMessageDialog(LoginActivity.this, e.getMessage() == null ? getString(R.string.verification_failed) : e.getMessage());
                    }

                    @Override
                    public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        signInProgressDialog.dismiss();
                        mShowingVerifyDialog = true;
                        mVerificationId = verificationId;
                        mUpdateFieldsListener = MiDialogs.showNumberVerificationDialog(LoginActivity.this, LoginActivity.this);
                    }

                    @Override
                    public void onCodeAutoRetrievalTimeOut(String s) {
                        signInProgressDialog.dismiss();
                        showResendCodeDialog(inputNumber);
                    }
                });
    }

    private void showResendCodeDialog(final String inputNumber) {
        AlertDialog dialog = new AlertDialog.Builder(LoginActivity.this)
                .setPositiveButton(getString(R.string.resend), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        loginWithPhone(inputNumber);
                    }
                })
                .setCancelable(false)
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        dialog.show();
    } // showResendCodeDialog

    @Override
    public void onVerify(Dialog dialog, String code) {
        dialog.dismiss();
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        final ProgressDialog loginProgress = new ProgressDialog(LoginActivity.this);
        loginProgress.setCancelable(false);
        loginProgress.setMessage(getString(R.string.signing_in));
        loginProgress.show();
        signInWithCredentials(credential, loginProgress);
    }

    private void signInWithCredentials(PhoneAuthCredential credential, final ProgressDialog loginProgress) {
        final FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {

                    FirebaseUser user = auth.getCurrentUser();

                    if (user == null) {
                        MiDialogs.showMessageDialog(LoginActivity.this, getString(R.string.login_failed));
                        loginProgress.dismiss();
                        return;
                    }

                    AppStateSource.setLoginAsAdmin(getApplicationContext(), MiUtils.isAdminNumber(msisdn));
                    FirebaseDatabase.getInstance().getReference(FirebaseRefs.USERS)
                            .child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            loginProgress.dismiss();
                            UserModel user = dataSnapshot.getValue(UserModel.class);
                            if (user == null) {
                                MiActivityUtils.startNewActivityAndClear(LoginActivity.this, ProfileUpdateActivity.class);
                            } else {
                                AppStateSource.setSessionTimedOut(LoginActivity.this);
                                MiActivityUtils.startNewActivityAndClear(LoginActivity.this, MainActivity.class);
                            }
                        } // onDataChange

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            loginProgress.dismiss();
                            MiDialogs.showMessageDialog(LoginActivity.this, databaseError.getMessage());
                        }
                    });

                } else {
                    loginProgress.dismiss();
                    String message = getString(R.string.login_failed);
                    //noinspection ThrowableResultOfMethodCallIgnored
                    message = task.getException() != null ? task.getException().getMessage() : message;
                    MiDialogs.showMessageDialog(LoginActivity.this, message);
                }
            }
        });
    }

    @Override
    public void onVerificationCanceled(Dialog dialog) {
        dialog.dismiss();
    }

    public interface UpdateFieldsListener {
        void onUpdate(String code, boolean isAutoFetched);

        Dialog getDialog();
    }

} // LoginActivity
