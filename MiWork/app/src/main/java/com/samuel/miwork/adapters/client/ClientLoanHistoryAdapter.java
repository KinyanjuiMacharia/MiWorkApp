package com.samuel.miwork.adapters.client;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.samuel.miwork.R;
import com.samuel.miwork.models.ApplicationModel;
import com.samuel.miwork.models.admin.LoanRequestStatus;
import com.samuel.miwork.utils.MiUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.allaudin.annotations.FactoryType;
import io.github.allaudin.annotations.OxyViews;
import io.github.allaudin.oxygeroid.ClientLoanHistoryVHViews;

/**
 * @author M.Allaudin
 *         <p>Created on 2017-09-29.</p>
 */

public class ClientLoanHistoryAdapter extends RecyclerView.Adapter<ClientLoanHistoryAdapter.ClientLoanHistoryVH> {

    private Context context;
    private List<ApplicationModel> applications = new ArrayList<>();
    private Map<String, Integer> statusMap = new HashMap<>();

    public ClientLoanHistoryAdapter(Context context, List<ApplicationModel> applications, Map<String, Integer> statuses) {
        this.applications = applications;
        this.context = context;
        this.statusMap = statuses;
    }

    @Override
    public ClientLoanHistoryVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(context).inflate(R.layout.item_loan_history_client, parent, false);
        return new ClientLoanHistoryVH(root);
    }

    @Override
    public void onBindViewHolder(ClientLoanHistoryVH holder, int position) {
        holder.populate(applications.get(position));
    }

    @Override
    public int getItemCount() {
        return applications.size();
    }

    @OxyViews(value = "item_loan_history_client", type = FactoryType.VIEW)
    class ClientLoanHistoryVH extends RecyclerView.ViewHolder {

        private final ClientLoanHistoryVHViews views;

        ClientLoanHistoryVH(View itemView) {
            super(itemView);
            views = ClientLoanHistoryVHViews.newInstance(itemView);
        }

        void populate(ApplicationModel model) {
            views.monthlySalary.setText(String.format("Monthly Income: %s", model.getMonthlyIncome()));
            views.dateAndTime.setText(String.format("Date: %s", MiUtils.getFormattedDate(model.getTimestamp())));
            views.totalLoanAmount.setText(String.format("Total Amount (with interest): %s", model.getTotalAmount()));
            views.interestRate.setText(String.format("Interest Rate: %s%%", model.getInterestRate()));
            views.loanAmount.setText(String.format("Loan Amount: %s", model.getLoanAmount()));
            views.companyOrInstitution.setText(String.format("Company/Institute: %s", model.getCompanyOrInstitution()));

            int status = statusMap.get(model.getId()) == null ? LoanRequestStatus.NOT_VIEWED : statusMap.get(model.getId());
            if (LoanRequestStatus.isViewed(status)) {
                views.status.setText(LoanRequestStatus.isAccepted(status) ? "Accepted" : "Rejected");
                views.status.setTextColor(LoanRequestStatus.isAccepted(status) ?
                        ContextCompat.getColor(context, R.color.colorPrimary) :
                        ContextCompat.getColor(context, R.color.colorRed));
            } else {
                views.status.setText(context.getString(R.string.pending));
                views.status.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
            }
        }
    } // ClientLoanHistoryVH

} // ClientLoanHistoryAdapter
