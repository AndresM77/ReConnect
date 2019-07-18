package com.example.reconnect.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Date;

@ParseClassName("Connection")
public class Connection extends ParseObject {
    public static final String KEY_USER1 = "user1";
    public static final String KEY_USER2 = "user2";
    public static final String KEY_STREAK = "streak";
    public static final String KEY_STARRED_12 = "starred12";
    public static final String KEY_STARRED_21 = "starred21";
    public static final String KEY_CREATED_AT = "createdAt";

    public ParseUser getUser1() {return getParseUser(KEY_USER1);}

    public void setUser1(ParseUser user1) {put(KEY_USER1, user1);}

    public ParseUser getUser2() {return getParseUser(KEY_USER2);}

    public void setUser2(ParseUser user2) {put(KEY_USER2, user2);}

    public Integer getStreak() {return getInt(KEY_STREAK);}

    public void setStreak(int streak) {put(KEY_STREAK, streak);}

    public Boolean getStarred12() {return getBoolean(KEY_STARRED_12);}

    public void setStarred12(Boolean starred12) {put(KEY_STARRED_12, starred12);}

    public Boolean getStarred21() {return getBoolean(KEY_STARRED_21);}

    public void setStarred21(Boolean starred21) {put(KEY_STARRED_21, starred21);}

    public ParseUser getCurrentUser() {
        if (ParseUser.getCurrentUser().getUsername().equals(getUser1().getUsername())){
            return getUser1();
        } else {
            return getUser2();
        }
    }

    public ParseUser getOtherUser() {
        if (ParseUser.getCurrentUser().getUsername().equals(getUser1().getUsername())){
            return getUser2();
        } else {
            return getUser1();
        }
    }


    @Override
    public Date getCreatedAt() {
        return super.getCreatedAt();
    }

    //Querying our Connection class
    public static class Query extends ParseQuery<Connection> {

        public Query() {
            super(Connection.class);
        }

        public Query getTop() {
            setLimit(20);
            return this;
        }

        public Query getStarred() {
            include("user1");
            include("starred12");
            return this;
        }

        public Query withUser() {
            include("user1");
            return this;
        }
    }
}
