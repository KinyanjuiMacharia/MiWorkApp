package com.samuel.miwork.models;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.samuel.miwork.R;

import io.github.allaudin.yabk.YabkProcess;

/**
 * @author M.Allaudin
 *         <p>Created on 9/17/2017.</p>
 */
@YabkProcess(nonNullStrings = true)
abstract class $UserModel {
    String fullName;
    String nationalId;
    String dateOfBirth;
    String address;
    String pinCode;
    String id;

    public boolean hasSamePin(String pin) {
        return pinCode != null && pinCode.equalsIgnoreCase(String.valueOf(pin.hashCode()));
    }

    public boolean hasSameNationalId(String nationalId) {
        return this.nationalId != null && this.nationalId.equalsIgnoreCase(nationalId);
    }

    public void setHashedPin(@NonNull String pin) {
        pinCode = String.valueOf(pin.hashCode());
    }

    public static UserModel getClone(UserModel userModel) {
        UserModel newUser = new UserModel();
        newUser.setPinCode(userModel.getPinCode());
        newUser.setFullName(userModel.getFullName());
        newUser.setAddress(userModel.getAddress());
        newUser.setNationalId(userModel.getNationalId());
        newUser.setDateOfBirth(userModel.getDateOfBirth());
        return newUser;
    }

    public boolean isSameUid(String uid) {
        return uid != null && this.id.equals(uid);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof $UserModel)) return false;

        $UserModel that = ($UserModel) o;

        return id.equals(that.id);

    }

    public String getNationalId(Context context){
        return  context.getString(R.string.national_id_label, nationalId == null? "": nationalId);
    }
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
