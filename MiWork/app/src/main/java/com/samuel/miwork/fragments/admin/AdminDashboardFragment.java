package com.samuel.miwork.fragments.admin;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSpinner;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.samuel.miwork.FirebaseRefs;
import com.samuel.miwork.R;
import com.samuel.miwork.utils.MiUtils;

import java.util.ArrayList;
import java.util.List;

import io.github.allaudin.annotations.FactoryType;
import io.github.allaudin.annotations.OxyViews;
import io.github.allaudin.oxygeroid.AdminDashboardFragmentViews;

/**
 * @author M.Allaudin
 *         <p>Created on 9/16/2017.</p>
 */
@OxyViews(value = "fragment_main_admin", type = FactoryType.VIEW)
public class AdminDashboardFragment extends Fragment implements View.OnClickListener {

    private AdminDashboardFragmentViews views;

    public AdminDashboardFragment() {
    }

    public static AdminDashboardFragment newInstance() {
        return new AdminDashboardFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_main_admin, container, false);
        views = AdminDashboardFragmentViews.newInstance(view);
        views.updateInterest.setOnClickListener(this);

        final ProgressDialog progress = MiUtils.getProgressDialog(getActivity());
        progress.show();

        FirebaseDatabase.getInstance().getReference(FirebaseRefs.INTEREST_RATE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Integer interestRate = dataSnapshot.getValue(Integer.class);
                        interestRate = interestRate == null ? 0 : interestRate;
                        views.interestRate.setText(String.format("%s %%", String.valueOf(interestRate)));
                        progress.dismiss();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        progress.dismiss();
                        Toast.makeText(getActivity(), "Failed to fetch interest rate", Toast.LENGTH_SHORT).show();
                    }
                });
        return view;
    } // onCreateView

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.update_interest: {
                updateInterest();
                break;
            }
        }
    } // onClick

    private void updateInterest() {
        final View view = View.inflate(getActivity(), R.layout.dialog_update_interest_rate, null);
        final AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
        dialog.setCancelable(false);
        dialog.setView(view);
        final AppCompatSpinner spinner = (AppCompatSpinner) view.findViewById(R.id.percent_spinner);

        List<String> percentage = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            percentage.add(i + "%");
        }
        ArrayAdapter<String> percentAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, percentage);
        percentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(percentAdapter);

        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        view.findViewById(R.id.update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                final int per = ((int) spinner.getSelectedItemId()) + 1;
                final ProgressDialog progress = MiUtils.getProgressDialog(getActivity());
                progress.setMessage(getString(R.string.updating));
                progress.show();
                FirebaseDatabase.getInstance().getReference(FirebaseRefs.INTEREST_RATE)
                        .setValue(per).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progress.dismiss();
                        views.interestRate.setText(String.valueOf(per) + "%");
                        Toast.makeText(getActivity(), "Interest rate updated.", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progress.dismiss();
                        Toast.makeText(getActivity(), "Interest update failed.", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        dialog.show();
    } // updateInterest

} // AdminDashboardFragment
