package com.samuel.miwork.adapters.admin;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.samuel.miwork.FirebaseRefs;
import com.samuel.miwork.R;
import com.samuel.miwork.models.ApplicationModel;
import com.samuel.miwork.models.UserModel;
import com.samuel.miwork.models.admin.LoanRequestStatus;
import com.samuel.miwork.utils.MiUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.allaudin.annotations.FactoryType;
import io.github.allaudin.annotations.OxyViews;
import io.github.allaudin.oxygeroid.AdminLoanRequestVHViews;

/**
 * @author M.Allaudin
 *         <p>Created on 2017-09-28.</p>
 */

public class AdminLoanRequestAdapter extends RecyclerView.Adapter<AdminLoanRequestAdapter.AdminLoanRequestVH> implements Filterable {

    private final Context context;
    private Map<String, Integer> statusMap;
    private List<ApplicationModel> applications;
    private List<ApplicationModel> applicationsBackup;

    public AdminLoanRequestAdapter(Context context, List<ApplicationModel> applications, Map<String, Integer> statusMap) {
        this.applications = applications;
        this.context = context;
        this.statusMap = statusMap;
        applicationsBackup = new ArrayList<>(applications);
    }

    @Override
    public AdminLoanRequestVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(context).inflate(R.layout.item_loan_requests_admin, parent, false);
        return new AdminLoanRequestVH(root);
    }

    @Override
    public void onBindViewHolder(AdminLoanRequestVH holder, int position) {
        holder.populate(applications.get(position));
    }

    @Override
    public int getItemCount() {
        return applications.size();
    }


    @OxyViews(value = "item_loan_requests_admin", type = FactoryType.VIEW)
    class AdminLoanRequestVH extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final AdminLoanRequestVHViews views;


        AdminLoanRequestVH(View root) {
            super(root);
            views = AdminLoanRequestVHViews.newInstance(root);
        }

        void populate(ApplicationModel model) {
            UserModel fbUser = model.getUser();
            if (fbUser == null) {
                return;
            }
            views.applicantName.setText(String.format("%s", fbUser.getFullName()));
            views.nationalId.setText(String.format("National ID: %s", fbUser.getNationalId()));
            views.monthlySalary.setText(String.format("Monthly Income: %s", model.getMonthlyIncome()));
            views.dateAndTime.setText(String.format("Date: %s", MiUtils.getFormattedDate(model.getTimestamp())));
            views.loanAmount.setText(String.format("Loan Amount: %s", model.getLoanAmount()));
            views.totalLoanAmount.setText(String.format("Total Amount (with interest): %s", model.getTotalAmount()));
            views.interestRate.setText(String.format("Interest Rate: %s%%", model.getInterestRate()));
            views.companyOrInstitution.setText(String.format("Company/Institute: %s", model.getCompanyOrInstitution()));
            views.accept.setOnClickListener(this);
            views.reject.setOnClickListener(this);

            int status = statusMap.get(model.getId()) == null ? LoanRequestStatus.NOT_VIEWED : statusMap.get(model.getId());
            views.buttonsWrapper.setVisibility(LoanRequestStatus.isViewed(status) ? View.GONE : View.VISIBLE);
            views.status.setVisibility(LoanRequestStatus.isViewed(status) ? View.VISIBLE : View.GONE);
            if (LoanRequestStatus.isViewed(status)) {
                views.status.setText(LoanRequestStatus.isAccepted(status) ? "Accepted" : "Rejected");
                views.status.setTextColor(LoanRequestStatus.isAccepted(status) ?
                        ContextCompat.getColor(context, R.color.colorPrimary) :
                        ContextCompat.getColor(context, R.color.colorRed));
            }

        } // populate


        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.accept: {
                    updateStatus(getAdapterPosition(), LoanRequestStatus.ACCEPTED);
                    break;
                }
                case R.id.reject: {
                    updateStatus(getAdapterPosition(), LoanRequestStatus.REJECTED);
                    break;
                }
            }
        }

        private void updateStatus(final int itemId, final int status) {
            final ApplicationModel applicationModel = applications.get(itemId);
            final ProgressDialog progress = MiUtils.getProgressDialog(context);
            progress.show();
            FirebaseDatabase.getInstance().getReference(FirebaseRefs.APPLICATION_STATUS)
                    .child(applicationModel.getUserId())
                    .child(applicationModel.getId()).setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    progress.dismiss();
                    statusMap.put(applicationModel.getId(), status);
                    notifyDataSetChanged();
                    String display = String.format("Application %s", LoanRequestStatus.isAccepted(status) ? "accepted" : "rejected");
                    Toast.makeText(context, display, Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progress.dismiss();
                    Toast.makeText(context, context.getString(R.string.status_failed), Toast.LENGTH_SHORT).show();
                }
            });
        }

    } // AdminLoanRequestVH

    @Override
    public Filter getFilter() {
        return new LoanRequestFilter();
    }

    private class LoanRequestFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults result = new FilterResults();
            if (constraint == null || constraint.length() == 0) {
                result.values = new ArrayList<>(applicationsBackup);
                result.count = applicationsBackup.size();
            } else {
                List<ApplicationModel> filteredList = new ArrayList<>();
                constraint = constraint.toString().toLowerCase();
                for (ApplicationModel model : applicationsBackup) {
                    if (model.getUser().getFullName().toLowerCase().contains(constraint) || model.getUser().getNationalId().toLowerCase().contains(constraint)) {
                        filteredList.add(model);
                    }
                }
                result.values = filteredList;
                result.count = filteredList.size();
            }
            return result;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {

            //noinspection unchecked
            applications = (ArrayList<ApplicationModel>) results.values;
            notifyDataSetChanged();

        }
    } // LoanRequestFilter

} // AdminLoanRequestAdapter
