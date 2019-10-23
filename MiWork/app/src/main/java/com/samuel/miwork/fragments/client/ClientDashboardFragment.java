package com.samuel.miwork.fragments.client;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.samuel.miwork.FirebaseRefs;
import com.samuel.miwork.R;
import com.samuel.miwork.activities.MainActivity;
import com.samuel.miwork.adapters.client.RecentHistoryAdapter;
import com.samuel.miwork.models.ApplicationModel;
import com.samuel.miwork.sources.AppStateSource;
import com.samuel.miwork.utils.MiDialogs;
import com.samuel.miwork.utils.MiUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.allaudin.annotations.FactoryType;
import io.github.allaudin.annotations.OxyViews;
import io.github.allaudin.oxygeroid.ClientDashboardFragmentViews;

/**
 * @author M.Allaudin
 *         <p>Created on 9/16/2017.</p>
 */

@OxyViews(value = "fragment_main_client", type = FactoryType.VIEW)
public class ClientDashboardFragment extends Fragment implements View.OnClickListener {

    private List<ApplicationModel> applications = new ArrayList<>();
    private Map<String, Integer> applicationStatuses = new HashMap<>();


    private ClientDashboardFragmentViews views;

    public ClientDashboardFragment() {
    }

    public static ClientDashboardFragment newInstance() {
        return new ClientDashboardFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_main_client, container, false);
        views = ClientDashboardFragmentViews.newInstance(view);
        views.applyForLoan.setOnClickListener(this);

        AdView adView = (AdView) view.findViewById(R.id.adView);
        adView.loadAd(new AdRequest.Builder().build());


        views.recentHistoryRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            return view;
        }

        final ProgressDialog progress = MiUtils.getProgressDialog(getActivity());
        progress.show();

        fetchApplications(user, progress);
        getLoanLimitAndInterestRate(user, progress);
        return view;
    } // onCreateView

    private void getLoanLimitAndInterestRate(FirebaseUser user, final ProgressDialog progress) {
        FirebaseDatabase.getInstance().getReference(FirebaseRefs.LIMITS)
                .child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String limit = dataSnapshot.getValue(String.class);
                if (limit != null) {
                    views.limit.setText(limit);
                    AppStateSource.setLimit(getContext(), limit);

                    FirebaseDatabase.getInstance().getReference(FirebaseRefs.INTEREST_RATE)
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Integer interest = dataSnapshot.getValue(Integer.class);
                                    AppStateSource.setInterestRate(getContext(), interest == null ? 0 : interest);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    MiDialogs.showMessageDialog(getActivity(), databaseError.getMessage());
                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progress.dismiss();
                MiDialogs.showMessageDialog(getActivity(), databaseError.getMessage());
            }
        });
    } // getLoanLimitAndInterestRate

    private void fetchApplications(final FirebaseUser fbUser, final ProgressDialog progress) {
        FirebaseDatabase.getInstance().getReference(FirebaseRefs.APPLICATIONS)
                .child(fbUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            ApplicationModel model = data.getValue(ApplicationModel.class);
                            applications.add(model);
                        }

                        fetchStatuses(fbUser, progress);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        progress.dismiss();

                    }
                });
    } // fetchUsersAndApplications

    private void fetchStatuses(final FirebaseUser fbUser, final ProgressDialog progress) {
        FirebaseDatabase.getInstance().getReference(FirebaseRefs.APPLICATION_STATUS)
                .child(fbUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            Integer status = data.getValue(Integer.class);
                            applicationStatuses.put(data.getKey(), status);
                        }
                        Collections.sort(applications, new Comparator<ApplicationModel>() {
                            @Override
                            public int compare(ApplicationModel o1, ApplicationModel o2) {
                                return (int) (o2.getTimestamp() - o1.getTimestamp());
                            }
                        });

                        views.recentHistoryError.setVisibility(applications.isEmpty() ? View.VISIBLE : View.GONE);
                        views.recentHistoryRecycler.setAdapter(new RecentHistoryAdapter(getActivity(), applications, applicationStatuses));
                        progress.dismiss();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        progress.dismiss();
                    }
                });
    } // fetchUsersAndApplications

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.apply_for_loan: {
                ((MainActivity) getActivity()).newLoanRequestFragment();
                break;
            }
        }
    }
} // ClientDashboardFragment
