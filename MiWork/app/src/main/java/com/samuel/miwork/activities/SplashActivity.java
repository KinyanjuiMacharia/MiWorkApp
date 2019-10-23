package com.samuel.miwork.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.samuel.miwork.FirebaseRefs;
import com.samuel.miwork.R;
import com.samuel.miwork.TimeOutManager;
import com.samuel.miwork.models.UserModel;
import com.samuel.miwork.sources.AppStateSource;
import com.samuel.miwork.utils.MiActivityUtils;
import com.samuel.miwork.utils.MiUtils;

import java.util.concurrent.TimeUnit;

public class SplashActivity extends AppCompatActivity implements TimeOutManager.PinVerifier {

    private static final int SPLASH_TIMEOUT_SECONDS = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    AppStateSource.clearSessionTimedOut(SplashActivity.this);
                    MiActivityUtils.startNewActivityAndClear(SplashActivity.this, LoginActivity.class);
                }
            }, TimeUnit.SECONDS.toMillis(SPLASH_TIMEOUT_SECONDS));
        } else {
            FirebaseDatabase.getInstance().getReference(FirebaseRefs.USERS)
                    .child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    UserModel userModel = dataSnapshot.getValue(UserModel.class);
                    if(userModel == null){
                        onCancelled(null);
                    }else if(MiUtils.isEmpty(userModel.getPinCode())){
                        openProfileActivity();
                    }else {
                        AppStateSource.setSessionTimedOut(SplashActivity.this);
                        TimeOutManager.getInstance(SplashActivity.this).checkSession(SplashActivity.this);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    openProfileActivity();
                }
            });

        }

    } // onCreate

    private void openProfileActivity() {
        MiActivityUtils.startNewActivityAndClear(SplashActivity.this, ProfileUpdateActivity.class);
    }

    @Override
    public void onPinVerified() {
        AppStateSource.clearSessionTimedOut(this);
        MiActivityUtils.startNewActivityAndClear(this, MainActivity.class);
    }

    @Override
    public void onPinVerificationFailed() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage(getString(R.string.pin_verification_failed))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TimeOutManager.getInstance(SplashActivity.this).checkSession(SplashActivity.this);
                    }
                })
                .create();

        dialog.show();
    }
} // SplashActivity
