package com.samuel.miwork.adapters.admin;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.samuel.miwork.R;
import com.samuel.miwork.models.UserModel;

import java.util.List;

import io.github.allaudin.annotations.FactoryType;
import io.github.allaudin.annotations.OxyViews;
import io.github.allaudin.oxygeroid.UsersViewHolderViews;

/**
 * @author M.Allaudin
 *         <p>Created on 9/22/2017.</p>
 */

public class AdminUsersAdapter extends RecyclerView.Adapter<AdminUsersAdapter.UsersViewHolder> {

    private Context context;
    private List<UserModel> users;

    public AdminUsersAdapter(Context context, List<UserModel> users) {
        this.context = context;
        this.users = users;
    }

    @Override
    public UsersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(context).inflate(R.layout.item_user_adapter_admin, parent, false);
        return new UsersViewHolder(root);
    }

    @Override
    public void onBindViewHolder(UsersViewHolder holder, int position) {
        holder.populate(context, users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    @OxyViews(value = "item_user_adapter_admin", type = FactoryType.VIEW)
    static class UsersViewHolder extends RecyclerView.ViewHolder {

        private final UsersViewHolderViews views;

        UsersViewHolder(View itemView) {
            super(itemView);
            views = UsersViewHolderViews.newInstance(itemView);

        }

        void populate(Context context, UserModel user) {
            views.userName.setText(user.getFullName());
            views.nationalId.setText(user.getNationalId(context));
        }
    } // UsersViewHolder

}// AdminUsersAdapter
