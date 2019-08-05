package com.example.reconnect;

import android.app.Application;

import com.example.reconnect.model.Connection;
import com.example.reconnect.model.Conversation;
import com.example.reconnect.model.Event;
import com.example.reconnect.model.Message;
import com.example.reconnect.model.User;
import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.ParseUser;

public class ParseApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ParseObject.registerSubclass(Connection.class);
        ParseObject.registerSubclass(Event.class);
        ParseObject.registerSubclass(Message.class);
        ParseObject.registerSubclass(Conversation.class);
        ParseUser.registerSubclass(User.class);

        final Parse.Configuration configuration = new Parse.Configuration.Builder(this)
                .applicationId("reconnect_id")
                .clientKey("dreamteam77")
                .server("http://andresm77-reconnect.herokuapp.com/parse")
                .build();

        /*
        ****Back4App Data*****
        * .applicationId("3OmO8VKHOH67fUwxjApifThHKMFO9ekctPQinpfb")
                .clientKey("Hmo5MI87Y9rJiMMzlR0zaJUsZaC6mRKBpWE4NXL0")
                .server("https://parseapi.back4app.com/")
        ****Heroku Data*****
        * .applicationId("reconnect_id")
                .clientKey("dreamteam77")
                .server("http://andresm77-reconnect.herokuapp.com/parse")
        AppId: 3OmO8VKHOH67fUwxjApifThHKMFO9ekctPQinpfb
        ClientKey: Hmo5MI87Y9rJiMMzlR0zaJUsZaC6mRKBpWE4NXL0
        Server: mongodb://admin:kGCXhftgon7EntawKPYirDRj@mongodb.back4app.com:27017/84c81018df4944d485867a2060708d2d?ssl=true
        You do not need to host it yourself as it is automatically hosted on AWS so you just need o go on the website to access it
        mongodb://heroku_pf7dg9dw:mongoparsepass7@ds135217.mlab.com:35217/heroku_pf7dg9dw
        mongodb://heroku_pf7dg9dw:mongoparsepass7@ds135217.mlab.com:35217/heroku_pf7dg9dw
         */



        Parse.initialize(configuration);
    }
}
