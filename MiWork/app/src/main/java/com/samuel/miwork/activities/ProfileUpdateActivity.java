package com.samuel.miwork.activities;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.samuel.miwork.FirebaseRefs;
import com.samuel.miwork.R;
import com.samuel.miwork.listeners.MiTextWatcher;
import com.samuel.miwork.models.UserModel;
import com.samuel.miwork.utils.MiActivityUtils;
import com.samuel.miwork.utils.MiDialogs;
import com.samuel.miwork.utils.MiUtils;

import java.util.Calendar;

import io.github.allaudin.annotations.OxyViews;
import io.github.allaudin.oxygeroid.ProfileUpdateActivityViews;

@OxyViews("activity_profile_update")
public class ProfileUpdateActivity extends BaseActivity implements View.OnClickListener {

    private ProfileUpdateActivityViews views;

    @Override
    public void afterOnCreate(@Nullable Bundle savedInstanceState) {
        views = ProfileUpdateActivityViews.newInstance(this);
        views.done.setOnClickListener(this);
        views.fullName.addTextChangedListener(getTextWatcher(views.fullName));
        views.nationalId.addTextChangedListener(getTextWatcher(views.nationalId));
        views.dateOfBirth.addTextChangedListener(getTextWatcher(views.dateOfBirth));
        views.address.addTextChangedListener(getTextWatcher(views.address));
        views.pinCode.addTextChangedListener(getTextWatcher(views.pinCode));
        views.pinCodeVerification.addTextChangedListener(getTextWatcher(views.pinCodeVerification));
        views.dateOfBirth.setFocusable(false);
        views.dateOfBirth.setFocusableInTouchMode(false);
        views.dateOfBirth.setOnClickListener(this);

    }

    private MiTextWatcher getTextWatcher(final EditText view) {
        return new MiTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (view.getId() == R.id.pin_code || view.getId() == R.id.pin_code_verification) {
                    views.pinError.setVisibility(View.GONE);
                }
                view.setBackgroundResource(R.drawable.input_style);
            }
        };
    }

    @Override
    public int getTitleId() {
        return R.string.sign_up;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_profile_update;
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.date_of_birth) {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    views.dateOfBirth.setText(String.format("%s-%s-%s", year, month, dayOfMonth));
                }
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            return;
        }

        String fullName = views.fullName.getText().toString();
        String nationalId = views.nationalId.getText().toString();
        String dateOfBirth = views.dateOfBirth.getText().toString();
        String address = views.address.getText().toString();
        String pin = views.pinCode.getText().toString();
        String verifyPin = views.pinCodeVerification.getText().toString();
        boolean isComplete = true;

        if (MiUtils.isEmpty(fullName)) {
            isComplete = false;
            views.fullName.setBackgroundResource(R.drawable.input_style_red);
        }


        if (MiUtils.isEmpty(nationalId)) {
            isComplete = false;
            views.nationalId.setBackgroundResource(R.drawable.input_style_red);
        }


        if (MiUtils.isEmpty(dateOfBirth)) {
            isComplete = false;
            views.dateOfBirth.setBackgroundResource(R.drawable.input_style_red);
        }


        if (MiUtils.isEmpty(address)) {
            isComplete = false;
            views.address.setBackgroundResource(R.drawable.input_style_red);
        }

        boolean hasEnteredBothPin = true;

        if (MiUtils.isEmpty(pin)) {
            isComplete = false;
            hasEnteredBothPin = false;
            views.pinCode.setBackgroundResource(R.drawable.input_style_red);
        }

        if (MiUtils.isEmpty(verifyPin)) {
            isComplete = false;
            hasEnteredBothPin = false;
            views.pinCodeVerification.setBackgroundResource(R.drawable.input_style_red);
        }

        if (hasEnteredBothPin && !pin.equalsIgnoreCase(verifyPin)) {
            views.pinError.setVisibility(View.VISIBLE);
        }

        if (!isComplete) {
            return;
        }

        UserModel user = new UserModel();
        user.setFullName(fullName);
        user.setAddress(address);
        user.setDateOfBirth(dateOfBirth);
        user.setNationalId(nationalId);
        user.setHashedPin(pin);


        FirebaseUser remoteUser = FirebaseAuth.getInstance().getCurrentUser();

        if (remoteUser != null) {
            final ProgressDialog progressDialog = MiUtils.getProgressDialog(this, getString(R.string.updating));
            progressDialog.show();
            user.setId(remoteUser.getUid());
            FirebaseDatabase.getInstance()
                    .getReference(FirebaseRefs.USERS)
                    .child(remoteUser.getUid()).setValue(user)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            progressDialog.dismiss();
                            Toast.makeText(ProfileUpdateActivity.this, getString(R.string.profile_updated), Toast.LENGTH_SHORT).show();
                            MiActivityUtils.startNewActivityAndClear(ProfileUpdateActivity.this, MainActivity.class);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    MiDialogs.showMessageDialog(ProfileUpdateActivity.this, e.getMessage() == null ? getString(R.string.profile_update_error) : e.getMessage());
                }
            });
        } // remoteUser != null

    } // onClick

} // ProfileUpdateActivity
