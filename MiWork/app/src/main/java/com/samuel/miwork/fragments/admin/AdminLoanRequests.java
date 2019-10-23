package com.samuel.miwork.fragments.admin;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.samuel.miwork.FirebaseRefs;
import com.samuel.miwork.R;
import com.samuel.miwork.adapters.admin.AdminLoanRequestAdapter;
import com.samuel.miwork.models.ApplicationModel;
import com.samuel.miwork.models.UserModel;
import com.samuel.miwork.utils.MiUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.allaudin.annotations.FactoryType;
import io.github.allaudin.annotations.OxyViews;
import io.github.allaudin.oxygeroid.AdminLoanRequestsViews;


/**
 * @author M.Allaudin
 *         <p>Created on 2017-09-26.</p>
 */
@OxyViews(value = "fragment_loan_requests_admin", type = FactoryType.VIEW)
public class AdminLoanRequests extends Fragment {

    private List<ApplicationModel> applications = new ArrayList<>();
    private Map<String, UserModel> userMap = new HashMap<>();
    private AdminLoanRequestsViews views;
    private Map<String, Integer> statusMap = new HashMap<>();
    private AdminLoanRequestAdapter loanRequestAdapter;

    public AdminLoanRequests() {
    }

    public static AdminLoanRequests newInstance() {
        return new AdminLoanRequests();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_loan_requests_admin, container, false);
        views = AdminLoanRequestsViews.newInstance(root);

        final FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();

        if (fbUser == null) {
            return root;
        }

        views.searchView.setIconified(false);
        views.searchView.setQueryHint("Search by Name or National ID");
        views.searchView.clearFocus();

        views.searchView.setOnQueryTextListener(getQueryListener());

        final ProgressDialog progress = MiUtils.getProgressDialog(getActivity());
        progress.show();
        fetchUsersAndApplications(fbUser, progress);

        return root;
    } // onCreateView

    @NonNull
    private SearchView.OnQueryTextListener getQueryListener() {
        return new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (loanRequestAdapter != null) {
                    loanRequestAdapter.getFilter().filter(query);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (loanRequestAdapter != null && MiUtils.isEmpty(newText)) {
                    loanRequestAdapter.getFilter().filter(newText);
                }
                return true;
            }
        };
    } // query listener

    private void fetchUsersAndApplications(final FirebaseUser fbUser, final ProgressDialog progress) {
        FirebaseDatabase.getInstance().getReference(FirebaseRefs.USERS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            UserModel model = data.getValue(UserModel.class);
                            if (model != null && !model.isSameUid(fbUser.getUid())) {
                                userMap.put(model.getId(), model);
                            }
                        }

                        fetchApplications(progress);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        progress.dismiss();
                        showError();
                    }
                });
    } // fetchUsersAndApplications

    private void showError() {
        Toast.makeText(getActivity(), "Fetching requests failed.", Toast.LENGTH_SHORT).show();
    }

    private void fetchApplications(final ProgressDialog progress) {
        FirebaseDatabase.getInstance().getReference(FirebaseRefs.APPLICATIONS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            for (DataSnapshot innerData : data.getChildren()) {
                                ApplicationModel model = innerData.getValue(ApplicationModel.class);
                                if (model != null) {
                                    model.setUser(userMap.get(model.getUserId()));
                                    applications.add(model);
                                }
                            } // inner for
                        }
                        fetchApplicationsStatus(progress);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        progress.dismiss();
                        showError();
                    }
                });
    } // fetchApplications


    private void fetchApplicationsStatus(final ProgressDialog progress) {
        FirebaseDatabase.getInstance().getReference(FirebaseRefs.APPLICATION_STATUS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {


                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            for (DataSnapshot innerData : data.getChildren()) {
                                Integer status = innerData.getValue(Integer.class);
                                statusMap.put(innerData.getKey(), status);
                            } // inner for
                        }


                        Collections.sort(applications, new Comparator<ApplicationModel>() {
                            @Override
                            public int compare(ApplicationModel o1, ApplicationModel o2) {
                                return (int) (o2.getTimestamp() - o1.getTimestamp());
                            }
                        });
                        views.usersRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
                        loanRequestAdapter = new AdminLoanRequestAdapter(getActivity(), applications, statusMap);
                        views.usersRecycler.setAdapter(loanRequestAdapter);
                        progress.dismiss();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        progress.dismiss();
                        showError();
                    }
                });
    } // fetchApplications


} // AdminLoanRequests
