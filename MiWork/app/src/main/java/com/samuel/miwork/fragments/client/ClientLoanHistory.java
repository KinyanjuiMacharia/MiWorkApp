package com.samuel.miwork.fragments.client;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.samuel.miwork.FirebaseRefs;
import com.samuel.miwork.R;
import com.samuel.miwork.adapters.client.ClientLoanHistoryAdapter;
import com.samuel.miwork.models.ApplicationModel;
import com.samuel.miwork.utils.MiUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author M.Allaudin
 *         <p>Created on 2017-09-29.</p>
 */

public class ClientLoanHistory extends Fragment {

    private List<ApplicationModel> applications = new ArrayList<>();
    private Map<String, Integer> applicationStatuses = new HashMap<>();
    private RecyclerView requestRecycler;

    public ClientLoanHistory() {
    }

    public static ClientLoanHistory newInstance() {
        return new ClientLoanHistory();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_loan_history_client, container, false);
        requestRecycler = (RecyclerView) root.findViewById(R.id.request_recycler);
        requestRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        ProgressDialog progress = MiUtils.getProgressDialog(getActivity());
        progress.show();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        fetchApplications(user, progress);
        return root;
    } // onCreateView

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
                        requestRecycler.setAdapter(new ClientLoanHistoryAdapter(getActivity(), applications, applicationStatuses));
                        progress.dismiss();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        progress.dismiss();
                    }
                });
    } // fetchUsersAndApplications

} // ClientHistory
