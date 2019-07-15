package com.example.reconnect;

import android.app.Application;

import com.parse.Parse;

public class ParseApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        final Parse.Configuration configuration = new Parse.Configuration.Builder(this)
                .applicationId("andres-parseagram")
                .clientKey("space-odyssey")
                .server("https://andresm77-parseagram.herokuapp.com/parse")
                .build();



        Parse.initialize(configuration);
    }
}
