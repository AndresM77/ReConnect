package com.example.reconnect.model;

import android.location.Location;

import com.parse.FindCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
        try {
            if (ParseUser.getCurrentUser().fetchIfNeeded().getUsername().equals(getUser1().fetchIfNeeded().getUsername())){
                return getUser1();
            } else {
                return getUser2();
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return  getUser1();
        }
    }

    public ParseUser getOtherUser() {
        try {
            if (ParseUser.getCurrentUser().fetchIfNeeded().getUsername().equals(getUser1().fetchIfNeeded().getUsername())){
                return getUser2();
            } else {
                return getUser1();
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return getUser2();
        }
    }

    public static void queryConnections(FindCallback<Connection> callback) {

        ParseQuery<Connection> postQuery = new ParseQuery<>(Connection.class);
        postQuery.whereEqualTo(Connection.KEY_USER1, ParseUser.getCurrentUser());

        ParseQuery<Connection> postQuery2 = new ParseQuery<>(Connection.class);
        postQuery2.whereEqualTo(Connection.KEY_USER2, ParseUser.getCurrentUser());

        List<ParseQuery<Connection>> queries = new ArrayList<>();
        queries.add(postQuery);
        queries.add(postQuery2);

        ParseQuery<Connection> mainQuery = ParseQuery.or(queries);
        mainQuery.addDescendingOrder(Connection.KEY_CREATED_AT);
        mainQuery.setLimit(20);

        mainQuery.findInBackground(callback);
    }


    @Override
    public Date getCreatedAt() {
        return super.getCreatedAt();
    }

    public static String getDistanceAway(ParseGeoPoint position1, ParseGeoPoint position2) {
        Location loc = new Location("");
        loc.setLatitude(position1.getLatitude());
        loc.setLongitude(position1.getLongitude());

        Location loc2 = new Location("");
        loc2.setLatitude(position2.getLatitude());
        loc2.setLongitude(position2.getLongitude());

        Double distance = new Float(loc.distanceTo(loc2)).doubleValue();
        Double metersToMiles = 1609.344;
        distance /= metersToMiles;

        DecimalFormat round = new DecimalFormat("#.#");

        return round.format(distance) + " miles away";
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
