package com.samuel.miwork.activities;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.samuel.miwork.R;

/**
 * @author M.Allaudin
 *         <p>Created on 9/15/2017.</p>
 */

public abstract class BaseActivity extends AppCompatActivity {

    protected Toolbar toolbar;
    protected TextView toolbarTitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            this.toolbar = toolbar;
            TextView title = (TextView) toolbar.findViewById(R.id.title);
            title.setText(getString(getTitleId()));
            toolbarTitle = title;
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);

            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        }

        if (toolbar != null && showBackArrow() && getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
        afterOnCreate(savedInstanceState);
    }

    protected boolean showBackArrow() {
        return false;
    }

    public abstract void afterOnCreate(@Nullable Bundle savedInstanceState);

    @StringRes
    public abstract int getTitleId();

    @LayoutRes
    public abstract int getLayoutId();
}
