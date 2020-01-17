package com.buzzvil.examples.buzzar;

import android.app.Application;

public class App extends Application {

    public static final String APP_ID = "100000043";
    public static final String UNIT_ID_NATIVE_AD = "166467299612761";

    @Override
    public void onCreate() {
        super.onCreate();
        AdManager.initBuzzAdBenefit(this, APP_ID, UNIT_ID_NATIVE_AD);
    }
}
