package com.samuel.miwork.fragments.shared;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.samuel.miwork.FirebaseRefs;
import com.samuel.miwork.R;
import com.samuel.miwork.listeners.MiTextWatcher;
import com.samuel.miwork.models.UserModel;
import com.samuel.miwork.utils.MiDialogs;
import com.samuel.miwork.utils.MiUtils;

import java.util.Calendar;

import io.github.allaudin.annotations.FactoryType;
import io.github.allaudin.annotations.OxyViews;
import io.github.allaudin.oxygeroid.ProfileFragmentViews;

/**
 * @author M.Allaudin
 *         <p>Created on 9/19/2017.</p>
 */

@OxyViews(value = "fragment_profile", type = FactoryType.VIEW)
public class ProfileFragment extends Fragment implements View.OnClickListener {

    private ProfileFragmentViews views;
    private UserModel currentUser;

    public ProfileFragment() {
    }

    private MiTextWatcher getTextWatcher(final EditText view) {
        return new MiTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (view.getId() == R.id.pin_code ||
                        view.getId() == R.id.pin_code_verification) {
                    views.pinError.setVisibility(View.GONE);
                }

                if (view.getId() == R.id.old_pin || view.getId() == R.id.new_pin) {
                    ((View) view.getTag()).setVisibility(View.GONE);
                }
                view.setBackgroundResource(R.drawable.input_style);
            }
        };
    }


    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        views = ProfileFragmentViews.newInstance(root);
        views.update.setOnClickListener(this);
        views.dateOfBirth.setOnClickListener(this);
        views.pinCode.setOnClickListener(this);

        MiUtils.disableView(views.dateOfBirth);
        MiUtils.disableView(views.pinCode);

        views.fullName.addTextChangedListener(getTextWatcher(views.fullName));
        views.nationalId.addTextChangedListener(getTextWatcher(views.nationalId));
        views.dateOfBirth.addTextChangedListener(getTextWatcher(views.dateOfBirth));
        views.address.addTextChangedListener(getTextWatcher(views.address));

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            showLoader();
            FirebaseDatabase.getInstance().getReference(FirebaseRefs.USERS)
                    .child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    UserModel userModel = dataSnapshot.getValue(UserModel.class);
                    if (userModel == null) {
                        showError();
                    } else {
                        currentUser = userModel;
                        populateFields(userModel);
                        hideLoader();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    showError();
                }
            });
        }
        return root;
    } // onCreateView

    private void populateFields(UserModel user) {
        views.fullName.setText(user.getFullName());
        views.address.setText(user.getAddress());
        views.dateOfBirth.setText(user.getDateOfBirth());
        views.nationalId.setText(user.getNationalId());
    }

    private void showLoader() {
        views.loader.setVisibility(View.VISIBLE);
        views.layoutWrapper.setVisibility(View.GONE);
        views.errorView.setVisibility(View.GONE);
    }

    private void hideLoader() {
        views.layoutWrapper.setVisibility(View.VISIBLE);
        views.loader.setVisibility(View.GONE);
    }

    private void showError() {
        hideLoader();
        views.errorView.setVisibility(View.VISIBLE);
        views.errorView.setText(getString(R.string.unable_to_load_data));
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.date_of_birth) {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    views.dateOfBirth.setText(String.format("%s-%s-%s", year, month, dayOfMonth));
                }
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            return;
        }

        if (v.getId() == R.id.pin_code) {
            View view = View.inflate(getActivity(), R.layout.dialog_update_pin, null);
            final EditText oldPin = (EditText) view.findViewById(R.id.old_pin);
            final EditText newPin = (EditText) view.findViewById(R.id.new_pin);
            final TextView wrongPinView = (TextView) view.findViewById(R.id.wrong_pin);

            oldPin.setTag(wrongPinView);
            newPin.setTag(wrongPinView);

            oldPin.addTextChangedListener(getTextWatcher(oldPin));
            newPin.addTextChangedListener(getTextWatcher(newPin));

            final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                    .setView(view)
                    .setCancelable(false)
                    .create();

            view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            view.findViewById(R.id.update).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String old = oldPin.getText().toString();
                    String updatePin = newPin.getText().toString();

                    if (MiUtils.isEmpty(old)) {
                        oldPin.setBackgroundResource(R.drawable.input_style_red);
                        return;
                    }

                    if (MiUtils.isEmpty(updatePin)) {
                        newPin.setBackgroundResource(R.drawable.input_style_red);
                        return;
                    }

                    verifyAndUpdatePin(dialog, old, updatePin, wrongPinView);
                }
            });
            dialog.show();
            return;
        }

        String fullName = views.fullName.getText().toString();
        String nationalId = views.nationalId.getText().toString();
        String dateOfBirth = views.dateOfBirth.getText().toString();
        String address = views.address.getText().toString();
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


        if (!isComplete) {
            return;
        }

        final UserModel newUser = UserModel.getClone(currentUser);
        newUser.setDateOfBirth(dateOfBirth);
        newUser.setNationalId(nationalId);
        newUser.setFullName(fullName);
        newUser.setAddress(address);

        updateProfile(newUser);
    } // onClick

    private void verifyAndUpdatePin(final AlertDialog dialog, final String old, final String updatePin, final TextView wrongPinView) {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseDatabase.getInstance().getReference(FirebaseRefs.USERS)
                    .child(user.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            UserModel thisUser = dataSnapshot.getValue(UserModel.class);
                            if (thisUser == null ) {
                                onCancelled(null);
                            } else if(!thisUser.hasSamePin(old)){
                                wrongPinView.setVisibility(View.VISIBLE);
                            }else {
                                thisUser.setHashedPin(updatePin);
                                FirebaseDatabase.getInstance().getReference(FirebaseRefs.USERS)
                                        .child(user.getUid()).setValue(thisUser).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        onCancelled(null);
                                    }
                                }).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        dialog.dismiss();
                                        MiDialogs.showMessageDialog(getActivity(), getString(R.string.pin_updated));
                                    }
                                });


                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            dialog.dismiss();
                            MiDialogs.showMessageDialog(getActivity(), getString(R.string.unable_to_update_pin));
                        }
                    });
        } else {
            dialog.dismiss();
            MiDialogs.showMessageDialog(getActivity(), getString(R.string.unable_to_update_pin));
        }
    } // verifyAndUpdatePin

    private void updateProfile(final UserModel newUser) {
        final ProgressDialog progressDialog = MiUtils.getProgressDialog(getActivity(), getString(R.string.updating));
        progressDialog.show();

        FirebaseUser remoteUser = FirebaseAuth.getInstance().getCurrentUser();
        if (remoteUser != null) {
            newUser.setId(remoteUser.getUid());
            FirebaseDatabase.getInstance()
                    .getReference(FirebaseRefs.USERS)
                    .child(remoteUser.getUid()).setValue(newUser)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            currentUser = newUser;
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), getString(R.string.profile_updated), Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    MiDialogs.showMessageDialog(getActivity(), e.getMessage() == null ? getString(R.string.profile_update_error) : e.getMessage());
                }
            });
        } else {
            progressDialog.dismiss();
            MiDialogs.showMessageDialog(getActivity(), getString(R.string.error_updating_profile));
        }
    }

} // ProfileFragment
