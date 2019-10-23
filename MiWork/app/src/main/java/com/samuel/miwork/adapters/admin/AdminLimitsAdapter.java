package com.samuel.miwork.adapters.admin;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.samuel.miwork.FirebaseRefs;
import com.samuel.miwork.R;
import com.samuel.miwork.listeners.MiTextWatcher;
import com.samuel.miwork.models.UserModel;
import com.samuel.miwork.utils.MiDialogs;
import com.samuel.miwork.utils.MiUtils;

import java.util.List;
import java.util.Map;

import io.github.allaudin.annotations.FactoryType;
import io.github.allaudin.annotations.OxyViews;
import io.github.allaudin.oxygeroid.LimitsViewHolderViews;

/**
 * @author M.Allaudin
 *         <p>Created on 9/22/2017.</p>
 */

public class AdminLimitsAdapter extends RecyclerView.Adapter<AdminLimitsAdapter.LimitsViewHolder> implements View.OnClickListener {

    private Context context;
    private List<UserModel> users;
    private Map<String, String> limits;

    public AdminLimitsAdapter(Context context, Map<String, String> limits, List<UserModel> users) {
        this.context = context;
        this.limits = limits;
        this.users = users;
    }

    @Override
    public LimitsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(context).inflate(R.layout.item_limits_adapter_admin, parent, false);
        return new LimitsViewHolder(root);
    }

    @Override
    public void onBindViewHolder(LimitsViewHolder holder, int position) {
        UserModel user = users.get(position);
        String limit = limits.get(user.getId()) == null ? context.getString(R.string.zero) : limits.get(user.getId());
        holder.populate(context, limit, user, this);
    }

    @Override
    public void onClick(View v) {
        int itemPosition = (int) v.getTag();
        showUpdateLimitDialog(itemPosition);
    }

    private void showUpdateLimitDialog(final int itemPosition) {
        View dialogView = View.inflate(context, R.layout.dialog_update_limit_admin, null);
        final AlertDialog dialog = new AlertDialog.Builder(context)
                .setCancelable(false)
                .setView(dialogView).create();
        TextView userName = (TextView) dialogView.findViewById(R.id.user_name);
        TextView nationalId = (TextView) dialogView.findViewById(R.id.national_id);
        TextView currentLimit = (TextView) dialogView.findViewById(R.id.current_limit);
        final EditText limitEditText = (EditText) dialogView.findViewById(R.id.update_limit);

        limitEditText.addTextChangedListener(new MiTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                limitEditText.setBackgroundResource(R.drawable.input_style);
            }
        });

        final UserModel userModel = users.get(itemPosition);
        final String limit = limits.get(userModel.getId()) == null ? context.getString(R.string.zero) : limits.get(userModel.getId());
        currentLimit.setText(limit);
        userName.setText(userModel.getFullName());
        nationalId.setText(userModel.getNationalId());

        dialogView.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialogView.findViewById(R.id.update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String newLimit = limitEditText.getText().toString();

                if (MiUtils.isEmpty(newLimit)) {
                    limitEditText.setBackgroundResource(R.drawable.input_style_red);
                    return;
                }

                dialog.dismiss();

                final ProgressDialog progress = MiUtils.getProgressDialog(context);
                progress.show();

                FirebaseDatabase.getInstance().getReference(FirebaseRefs.LIMITS)
                        .child(userModel.getId()).setValue(newLimit).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progress.dismiss();
                        limits.put(userModel.getId(), newLimit);
                        notifyItemChanged(itemPosition);
                        MiDialogs.showMessageDialog(context, context.getString(R.string.limit_updated));
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progress.dismiss();
                        MiDialogs.showMessageDialog(context, context.getString(R.string.limit_update_failed));
                    }
                });
            }
        });
        dialog.show();
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    @OxyViews(value = "item_limits_adapter_admin", type = FactoryType.VIEW)
    static class LimitsViewHolder extends RecyclerView.ViewHolder {

        private final LimitsViewHolderViews views;
        private final View root;

        LimitsViewHolder(View itemView) {
            super(itemView);
            root = itemView;
            views = LimitsViewHolderViews.newInstance(itemView);

        }


        void populate(final Context context, final String limit, final UserModel user, View.OnClickListener clickListener) {
            views.limit.setText(context.getString(R.string.limit_with_label, limit));
            views.userName.setText(user.getFullName());
            views.nationalId.setText(user.getNationalId(context));
            root.setTag(getAdapterPosition());
            root.setOnClickListener(clickListener);
        }


    } // UsersViewHolder


} // AdminLimitsAdapter
