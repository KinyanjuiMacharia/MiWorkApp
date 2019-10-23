package com.samuel.miwork.models;

import com.google.gson.Gson;

import io.github.allaudin.yabk.YabkProcess;

/**
 * @author M.Allaudin
 *         <p>Created on 2017-09-26.</p>
 */
@YabkProcess(nonNullStrings = true)
abstract class $ApplicationModel {

    String id;
    String loanAmount;
    String interestRate;
    String employeeStatus;
    String monthlyIncome;
    String companyOrInstitution;
    long timestamp;
    String userId;
    boolean isProcessed;
    // UserModel will be attached manually.
    UserModel user;

    public int getTotalAmount() {
        int loanAmount = Integer.parseInt(this.loanAmount);
        return loanAmount + ((int) (loanAmount * (Integer.parseInt(interestRate) / 100f)));
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
