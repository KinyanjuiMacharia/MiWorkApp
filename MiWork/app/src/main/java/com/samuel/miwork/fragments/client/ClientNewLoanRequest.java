package com.samuel.miwork.fragments.client;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
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
import com.samuel.miwork.activities.client.TermsAndConditionActivity;
import com.samuel.miwork.listeners.MiTextWatcher;
import com.samuel.miwork.models.ApplicationModel;
import com.samuel.miwork.sources.AppStateSource;
import com.samuel.miwork.utils.MiActivityUtils;
import com.samuel.miwork.utils.MiDialogs;
import com.samuel.miwork.utils.MiLogger;
import com.samuel.miwork.utils.MiUtils;

import java.util.Locale;

import io.github.allaudin.annotations.FactoryType;
import io.github.allaudin.annotations.OxyViews;
import io.github.allaudin.oxygeroid.ClientNewLoanRequestViews;


/**
 * @author M.Allaudin
 *         <p>Created on 2017-09-25.</p>
 */
@OxyViews(value = "fragment_add_new_app_client", type = FactoryType.VIEW)
public class ClientNewLoanRequest extends Fragment implements AdapterView.OnItemSelectedListener, View.OnClickListener {

    private String selectedIncome;
    private String selectedEmployeeStatus;
    private ClientNewLoanRequestViews views;
    private ArrayAdapter<CharSequence> incomeAdapter;
    private ArrayAdapter<CharSequence> empStatusAdapter;
    private int loanLimit;
    private int interestRate;

    public ClientNewLoanRequest() {
    }

    public static ClientNewLoanRequest newInstance() {
        return new ClientNewLoanRequest();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loanLimit = AppStateSource.getLoanLimit(getContext());
        interestRate = AppStateSource.getInterestRate(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_add_new_app_client, container, false);
        views = ClientNewLoanRequestViews.newInstance(root);
        empStatusAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.employee_status, android.R.layout.simple_spinner_item);
        selectedEmployeeStatus = (String) empStatusAdapter.getItem(0);
        empStatusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        views.empStatusSpinner.setAdapter(empStatusAdapter);
        incomeAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.income_options, android.R.layout.simple_spinner_item);
        incomeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectedIncome = (String) incomeAdapter.getItem(0);
        views.incomeSpinner.setAdapter(incomeAdapter);
        views.incomeSpinner.setOnItemSelectedListener(this);
        views.empStatusSpinner.setOnItemSelectedListener(this);
        views.apply.setOnClickListener(this);
        views.termsAndConditionLink.setOnClickListener(this);
        views.interestRate.setText(String.format(Locale.US, "Interest Rate: %d%%", interestRate));
        views.maxLimit.setText(String.format(Locale.US, "max. %d", loanLimit));

        views.loanAmountInput.setFilters(new InputFilter[]{new InputFilterMinMax("1", String.valueOf(loanLimit))});

        // add watchers
        views.compInstInput.addTextChangedListener(getTextWatcher(views.compInstInput));
        views.loanAmountInput.addTextChangedListener(getTextWatcher(views.loanAmountInput));

        views.termsCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                views.termsError.setVisibility(isChecked ? View.GONE : View.VISIBLE);
            }
        });


        return root;
    } // onCreate


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.emp_status_spinner: {
                selectedEmployeeStatus = (String) empStatusAdapter.getItem(position);
                break;
            }
            case R.id.income_spinner: {
                selectedIncome = (String) incomeAdapter.getItem(position);
                break;
            }
        } // switch
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        MiLogger.d("%s", "nothing selected");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.terms_and_condition_link: {
                MiActivityUtils.startNewActivity(getActivity(), TermsAndConditionActivity.class);
                break;
            }
            case R.id.apply: {
                final String loanAmount = views.loanAmountInput.getText().toString();
                String companyOrInstitute = views.compInstInput.getText().toString();
                boolean isComplete = true;
                if (MiUtils.isEmpty(loanAmount)) {
                    isComplete = false;
                    views.loanAmountInput.setBackgroundResource(R.drawable.input_style_red);
                }

                if (MiUtils.isEmpty(companyOrInstitute)) {
                    isComplete = false;
                    views.compInstInput.setBackgroundResource(R.drawable.input_style_red);
                }

                if (!views.termsCheckbox.isChecked()) {
                    isComplete = false;
                    views.termsError.setVisibility(View.VISIBLE);
                }

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (isComplete && user != null) {

                    ApplicationModel model = new ApplicationModel();
                    model.setCompanyOrInstitution(companyOrInstitute);
                    model.setEmployeeStatus(selectedEmployeeStatus);
                    model.setLoanAmount(loanAmount);
                    model.setInterestRate(interestRate + "");
                    model.setMonthlyIncome(selectedIncome);
                    model.setTimestamp(System.currentTimeMillis());
                    final ProgressDialog progress = MiUtils.getProgressDialog(getActivity());
                    progress.show();
                    String applicationId = FirebaseDatabase.getInstance().getReference(FirebaseRefs.APPLICATIONS).push().getKey();
                    model.setId(applicationId);
                    model.setUserId(user.getUid());

                    FirebaseDatabase.getInstance().getReference(FirebaseRefs.APPLICATIONS)
                            .child(user.getUid())
                            .child(applicationId).setValue(model)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    //  reset data
                                    views.compInstInput.setText(null);
                                    views.loanAmountInput.setText(null);
                                    views.empStatusSpinner.setSelection(0);
                                    views.incomeSpinner.setSelection(0);
                                    progress.dismiss();

                                    String borrowMessage = String.format(Locale.US, "%s %s, %s %d%%.\n\nTotal Amount = %d", "You have successfully applied to borrow loan of", loanAmount,
                                            "with interest rate of", interestRate, getTotalAmount(Integer.parseInt(loanAmount)));
                                    MiDialogs.showMessageDialog(getActivity(), borrowMessage);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progress.dismiss();
                            Toast.makeText(getActivity(), getString(R.string.new_application_failed), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                break;
            }
        }
    } // onClick


    private MiTextWatcher getTextWatcher(final EditText view) {
        return new MiTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                view.setBackgroundResource(R.drawable.input_style);
            }
        };
    } // getTextWatcher

    private class InputFilterMinMax implements InputFilter {
        private int min;
        private int max;

        InputFilterMinMax(String min, String max) {
            this.min = Integer.parseInt(min);
            this.max = Integer.parseInt(max);
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

            String newVal = dest.toString().substring(0, dstart) + dest.toString().substring(dend, dest.toString().length());
            newVal = newVal.substring(0, dstart) + source.toString() + newVal.substring(dstart, newVal.length());
            try {
                int input = Integer.parseInt(newVal);
                int total = getTotalAmount(input);

                if (input >= min && input <= max) {
                    views.maxLimit.setTextColor(ContextCompat.getColor(getContext(), R.color.miwok_text));
                    views.totalAmount.setText(String.format(Locale.US, "Total Amount: %d", total));
                    return null;
                }
            } catch (NumberFormatException ignore) {
                views.totalAmount.setText(String.format(Locale.US, "Total Amount: %d", 0));
            }
            views.maxLimit.setTextColor(ContextCompat.getColor(getContext(), R.color.colorRed));
            return "";
        }
    }

    private int getTotalAmount(int input) {
        return input + ((int) (input * (interestRate / 100f)));
    }
} // ClientNewLoanRequest
