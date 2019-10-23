package com.samuel.miwork.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.samuel.miwork.FirebaseRefs;
import com.samuel.miwork.R;
import com.samuel.miwork.TimeOutManager;
import com.samuel.miwork.fragments.admin.AdminClientsFragment;
import com.samuel.miwork.fragments.admin.AdminDashboardFragment;
import com.samuel.miwork.fragments.admin.AdminLimitsFragment;
import com.samuel.miwork.fragments.admin.AdminLoanRequests;
import com.samuel.miwork.fragments.client.ClientDashboardFragment;
import com.samuel.miwork.fragments.client.ClientLoanHistory;
import com.samuel.miwork.fragments.client.ClientNewLoanRequest;
import com.samuel.miwork.fragments.shared.ProfileFragment;
import com.samuel.miwork.models.UserModel;
import com.samuel.miwork.sources.AppStateSource;
import com.samuel.miwork.utils.MiActivityUtils;

import java.util.concurrent.TimeUnit;

public class MainActivity extends BaseActivity implements TimeOutManager.PinVerifier, NavigationView.OnNavigationItemSelectedListener {

    private static final int SESSION_TIMEOUT_SECONDS = 500;

    private Handler timeoutHandler = new Handler();

    private Runnable timeoutTask = new Runnable() {
        @Override
        public void run() {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                AppStateSource.clearSessionTimedOut(MainActivity.this);
            }
            AppStateSource.setSessionTimedOut(getApplicationContext());
        }
    };
    private NavigationView navigationView;

    @Override
    public void afterOnCreate(@Nullable Bundle savedInstanceState) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        final TextView headerName = (TextView) headerView.findViewById(R.id.nav_header_profile_name);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            return;
        }

        // populate nav drawer with user name
        FirebaseDatabase.getInstance().getReference(FirebaseRefs.USERS)
                .child(user.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        UserModel user = dataSnapshot.getValue(UserModel.class);
                        if (headerName != null) {
                            headerName.setText(user != null ? user.getFullName() : "");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        // set main fragment
        Fragment fragment = AppStateSource.isLoggedInAsAdmin(this) ? AdminDashboardFragment.newInstance() : ClientDashboardFragment.newInstance();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment).commit();


        // clear old menu and populate admin if logged in user is admin.
        if (AppStateSource.isLoggedInAsAdmin(this)) {
            navigationView.getMenu().clear();
            navigationView.inflateMenu(R.menu.main_menu_admin);
        }

        if (navigationView.getMenu().size() > 0) {
            navigationView.getMenu().getItem(0).setChecked(true);
        }
    }

    @Override
    public int getTitleId() {
        return R.string.dashboard;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (AppStateSource.isSessionTimedOut(this)) {
            TimeOutManager.getInstance(this).checkSession(this);
        }
    } // onStart

    @Override
    protected void onStop() {
        super.onStop();
        timeoutHandler.postDelayed(timeoutTask, TimeUnit.SECONDS.toMillis(SESSION_TIMEOUT_SECONDS));
    } // onStop

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timeoutHandler.removeCallbacks(timeoutTask);
        AppStateSource.setSessionTimedOut(getApplicationContext());
    } // onDestroy

    @Override
    public void onPinVerified() {
        timeoutHandler.removeCallbacks(timeoutTask);
        AppStateSource.clearSessionTimedOut(getApplicationContext());
    }

    @Override
    public void onPinVerificationFailed() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage(getString(R.string.pin_verification_failed))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TimeOutManager.getInstance(MainActivity.this).checkSession(MainActivity.this);
                    }
                })
                .create();

        dialog.show();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.nav_dashboard: {
                toolbarTitle.setText(getString(R.string.dashboard));
                addFragment(AppStateSource.isLoggedInAsAdmin(this) ? AdminDashboardFragment.newInstance() : ClientDashboardFragment.newInstance());
                break;
            }
            case R.id.nav_profile_client: {
                toolbarTitle.setText(getString(R.string.profile));
                addFragment(ProfileFragment.newInstance());
                break;
            }

            case R.id.nav_clients: {
                toolbarTitle.setText(getString(R.string.clients));
                addFragment(AdminClientsFragment.newInstance());
                break;
            }

            case R.id.nav_apply_loan_client: {
                newLoanRequestFragment();
                break;
            }


            case R.id.nav_limits: {
                toolbarTitle.setText(getString(R.string.limits));
                addFragment(AdminLimitsFragment.newInstance());
                break;
            }

            case R.id.nav_client_loan_requests: {
                toolbarTitle.setText(getString(R.string.client_loan_requests));
                addFragment(AdminLoanRequests.newInstance());
                break;
            }

            case R.id.nav_loan_history_client: {
                toolbarTitle.setText(getString(R.string.loan_history));
                addFragment(ClientLoanHistory.newInstance());
                break;
            }
            case R.id.nav_logout: {
                logoutUser();
                break;
            }
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    } // onNavigationItemSelected

    public void newLoanRequestFragment() {
        toolbarTitle.setText(getString(R.string.application_loan));
        addFragment(ClientNewLoanRequest.newInstance());
        navigationView.getMenu().getItem(1).setChecked(true);
    }

    private void logoutUser() {
        timeoutHandler.removeCallbacks(timeoutTask);
        if (FirebaseAuth.getInstance() != null) {
            FirebaseAuth.getInstance().signOut();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    AppStateSource.clearSessionTimedOut(MainActivity.this);
                    MiActivityUtils.startNewActivityAndClear(MainActivity.this, LoginActivity.class);
                }
            }, 500);
        }

    }

    private void addFragment(final Fragment newFragment) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, newFragment)
                        .commit();
            }
        }, 250);

    }
}
