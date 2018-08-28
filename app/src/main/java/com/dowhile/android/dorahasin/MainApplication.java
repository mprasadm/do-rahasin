package com.dowhile.android.dorahasin;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import io.flic.lib.FlicManager;

public class MainApplication extends Application {

    private static Context context;


    @Override
    public void onCreate() {
        super.onCreate();
        MainApplication.context = getApplicationContext();
        FlicManager.setAppCredentials("doRahasin", "879cfb06-454f-44bb-bc67-49cf4d513c97", "doRahasin");
    //FlicManager.setAppCredentials("[doRahasin]", "[879cfb06-454f-44bb-bc67-49cf4d513c97]", "[doRahasin]");
    }

    public static SharedPreferences getPreferenceManager() {
        return PreferenceManager.getDefaultSharedPreferences(MainApplication.context);
    }

    public static Context getAppContext() {
        return MainApplication.context;
    }
}
