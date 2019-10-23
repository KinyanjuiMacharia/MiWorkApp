package com.samuel.miwork.fragments.admin;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
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
import com.samuel.miwork.adapters.admin.AdminLimitsAdapter;
import com.samuel.miwork.models.UserModel;
import com.samuel.miwork.utils.MiDialogs;
import com.samuel.miwork.utils.MiUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.allaudin.annotations.FactoryType;
import io.github.allaudin.annotations.OxyViews;
import io.github.allaudin.oxygeroid.AdminLimitsFragmentViews;

/**
 * @author M.Allaudin
 *         <p>Created on 9/22/2017.</p>
 */
@OxyViews(value = "fragment_limits_admin", type = FactoryType.VIEW)
public class AdminLimitsFragment extends Fragment {

    private ProgressDialog progress;
    private AdminLimitsFragmentViews views;

    public AdminLimitsFragment() {
    }

    public static AdminLimitsFragment newInstance() {
        return new AdminLimitsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_limits_admin, container, false);
        views = AdminLimitsFragmentViews.newInstance(root);
        views.limitRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            return root;
        }

        progress = MiUtils.getProgressDialog(getActivity());
        progress.show();

        FirebaseDatabase.getInstance().getReference(FirebaseRefs.USERS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final List<UserModel> users = new ArrayList<>();
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            UserModel model = data.getValue(UserModel.class);
                            if (model != null && !model.isSameUid(user.getUid())) {
                                users.add(model);
                            }
                            fetchLimits(users);
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        progress.dismiss();
                    }
                });

        return root;
    }

    private void fetchLimits(final List<UserModel> users) {
        FirebaseDatabase.getInstance().getReference(FirebaseRefs.LIMITS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        @SuppressWarnings("unchecked")
                        Map<String, String> limits = (HashMap<String, String>) dataSnapshot.getValue();
                        limits = limits == null? new HashMap<String, String>(): limits;
                        setAdapter(limits, users);
                        progress.dismiss();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        progress.dismiss();
                        MiDialogs.showMessageDialog(getActivity(), databaseError.getMessage());
                    }
                });
    } // fetchLimits

    private void setAdapter(Map<String, String> limits, List<UserModel> users) {
        views.limitRecycler.setAdapter(new AdminLimitsAdapter(getActivity(), limits, users));
    }
} // AdminLimitsFragment
