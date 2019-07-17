package com.example.reconnect;

import android.app.Application;

import com.example.reconnect.model.Connection;
import com.example.reconnect.model.Conversation;
import com.example.reconnect.model.Event;
import com.example.reconnect.model.Message;
import com.parse.Parse;
import com.parse.ParseObject;

public class ParseApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ParseObject.registerSubclass(Connection.class);
        ParseObject.registerSubclass(Event.class);
        ParseObject.registerSubclass(Message.class);
        ParseObject.registerSubclass(Conversation.class);

        final Parse.Configuration configuration = new Parse.Configuration.Builder(this)
                .applicationId("reconnect_id")
                .clientKey("dreamteam77")
                .server("http://andresm77-reconnect.herokuapp.com/parse")
                .build();



        Parse.initialize(configuration);
    }
}
