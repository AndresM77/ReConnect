package com.example.reconnect.model;

import com.parse.ParseClassName;
import com.parse.ParseQuery;
import com.parse.ParseUser;

@ParseClassName("User")
public class User extends ParseUser {
    public final static String KEY_USER = "username";

    //Querying our User class
    public static class Query extends ParseQuery<User> {

        public Query() {
            super(User.class);
        }

        public User.Query getTop() {
            setLimit(20);
            return this;
        }

        public User.Query withUser() {
            include("username");
            return this;
        }
    }
}
