package com.notes.neat;

import android.app.Application;
import android.content.Context;

import java.util.Locale;

public class NeatApplication extends Application {

    private static NeatApplication sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    public static synchronized NeatApplication getInstance() {
        return sInstance;
    }

    public Locale getLocale() {
        return Locale.US;
    }
}
