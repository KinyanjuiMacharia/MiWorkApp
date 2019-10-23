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
import com.samuel.miwork.adapters.admin.AdminUsersAdapter;
import com.samuel.miwork.models.UserModel;
import com.samuel.miwork.utils.MiUtils;

import java.util.ArrayList;
import java.util.List;

import io.github.allaudin.annotations.FactoryType;
import io.github.allaudin.annotations.OxyViews;
import io.github.allaudin.oxygeroid.AdminClientsFragmentViews;

/**
 * @author M.Allaudin
 *         <p>Created on 9/22/2017.</p>
 */
@OxyViews(value = "fragment_clients_admin",type = FactoryType.VIEW)
public class AdminClientsFragment extends Fragment {

    private AdminClientsFragmentViews views;

    public AdminClientsFragment(){}

    public static AdminClientsFragment newInstance(){
        return new AdminClientsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_clients_admin, container, false);
        views = AdminClientsFragmentViews.newInstance(view);
        final ProgressDialog progress = MiUtils.getProgressDialog(getActivity());
        progress.show();

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            return view;
        }

        FirebaseDatabase.getInstance().getReference(FirebaseRefs.USERS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        progress.dismiss();
                        final List<UserModel> users = new ArrayList<>();
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            UserModel model = data.getValue(UserModel.class);
                            if(model != null && !model.isSameUid(user.getUid())){
                                users.add(model);
                            }
                        }

                        views.usersRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
                        views.usersRecycler.setAdapter(new AdminUsersAdapter(getActivity(), users));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        progress.dismiss();
                    }
                });
        return view;
    }
} // AdminClientsFragment
