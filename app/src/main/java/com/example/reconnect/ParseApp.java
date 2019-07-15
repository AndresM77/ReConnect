package com.example.reconnect;

import android.app.Application;

import com.parse.Parse;

public class ParseApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        final Parse.Configuration configuration = new Parse.Configuration.Builder(this)
                .applicationId("reconnect_id")
                .clientKey("dreamteam77")
                .server("http://andresm77-reconnect.herokuapp.com/parse")
                .build();



        Parse.initialize(configuration);
    }
}
