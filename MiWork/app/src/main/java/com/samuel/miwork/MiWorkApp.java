package com.samuel.miwork;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.MobileAds;

import io.fabric.sdk.android.Fabric;
import io.github.allaudin.annotations.OxyConfig;

/**
 * @author M.Allaudin
 *         <p>Created on 9/15/2017.</p>
 */
@OxyConfig(resourcePackage = "com.samuel.miwork")
public class MiWorkApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        MobileAds.initialize(this, "ca-app-pub-4638253495527377~4709486221");
    }
} // MiWorkApp
