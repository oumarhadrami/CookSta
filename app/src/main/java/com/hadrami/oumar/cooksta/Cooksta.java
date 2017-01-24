package com.hadrami.oumar.cooksta;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by oumar on 09/11/2016.
 */

public class Cooksta extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
