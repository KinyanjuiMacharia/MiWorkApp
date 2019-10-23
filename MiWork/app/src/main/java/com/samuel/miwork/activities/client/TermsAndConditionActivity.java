package com.samuel.miwork.activities.client;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.samuel.miwork.R;
import com.samuel.miwork.activities.BaseActivity;

public class TermsAndConditionActivity extends BaseActivity {

    @Override
    public void afterOnCreate(@Nullable Bundle savedInstanceState) {

    }

    @Override
    public int getTitleId() {
        return R.string.terms_and_condition;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_terms_and_condition;
    }

    @Override
    protected boolean showBackArrow() {
        return true;
    }
} // TermsAndConditionActivity
