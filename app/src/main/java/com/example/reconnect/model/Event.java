package com.example.reconnect.model;

import android.os.Build;

import com.parse.FindCallback;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@ParseClassName("Event")
public class Event extends ParseObject {
    public static final String KEY_START_TIME = "startTime";
    public static final String KEY_END_TIME = "endTime";
    public static final String KEY_NAME = "name";
    public static final String KEY_CREATOR = "creator";
    public static final String KEY_ATTENDEE = "attendee";
    public static final String KEY_PENDING = "pending";
    public static final String KEY_SPECIAL = "reconnect";
    public static final String KEY_DATE = "date";
    public static final String KEY_CREATED_AT = "createdAt";
    public static final String KEY_ACCEPTED = "accepted";


    public String getStartTime() {return getString(KEY_START_TIME);}

    public void setStartTime(String startTime) {
        put(KEY_START_TIME, startTime);
    }

    public String getEndTime() {return getString(KEY_END_TIME);}

    public void setEndTime(String endTime) {put(KEY_END_TIME, endTime);}

    public String getName() {return getString(KEY_NAME);}

    public void setName(String name) {put(KEY_NAME, name);}

    public ParseUser getCreator() {return getParseUser(KEY_CREATOR);}

    public void setCreator(ParseUser creator) {put(KEY_CREATOR, creator);}

    public ParseUser getAttendee() {return getParseUser(KEY_ATTENDEE);}

    public void setAttendee(ParseUser attendee) {put(KEY_ATTENDEE, attendee);}

    public Boolean getPending() {return getBoolean(KEY_PENDING);}

    public void setPending(Boolean pending) {put(KEY_PENDING, pending);}

    public Boolean getSpecial() {return getBoolean(KEY_SPECIAL);}

    public void setSpecial (Boolean special) {put(KEY_SPECIAL, special);}

    public String getDate() {
        return KEY_DATE;
    }

    public void setDate(Date date) {
        put(KEY_DATE, date);
    }

    public Boolean getAccepted() {
        return getBoolean(KEY_ACCEPTED);
    }

    public void setAccepted(Boolean accepted) { put(KEY_ACCEPTED, accepted);}

    public ParseUser getCurrentUser() {
        if (ParseUser.getCurrentUser().getUsername().equals(getCreator().getUsername())){
            return getCreator();
        } else {
            return getAttendee();
        }
    }

    public static void queryEvents(FindCallback<Event> callback) {
        ParseQuery<Event> query1 = new ParseQuery<Event>(Event.class);
        query1.whereEqualTo(Event.KEY_CREATOR, ParseUser.getCurrentUser());
        ParseQuery<Event> query2 = new ParseQuery<Event>(Event.class);
        query2.whereEqualTo(Event.KEY_ATTENDEE, ParseUser.getCurrentUser());

        List<ParseQuery<Event>> queries = new ArrayList<>();
        queries.add(query1);
        queries.add(query2);

        ParseQuery<Event> mainQuery = ParseQuery.or(queries);
        mainQuery.addDescendingOrder(Event.KEY_PENDING);
        mainQuery.addDescendingOrder(Event.KEY_CREATED_AT);
        mainQuery.addAscendingOrder(Event.KEY_DATE);
        mainQuery.setLimit(20);

        mainQuery.findInBackground(callback);

    }

    public ParseUser getOtherUser() {
        if (ParseUser.getCurrentUser().getUsername().equals(getCreator().getUsername())){
            return getAttendee();
        } else {
            return getCreator();
        }
    }

    public Boolean currUserIsAttendee() {
        if (ParseUser.getCurrentUser().getUsername().equals(getCreator().getUsername())) {
            return false;
        }

        return true;
    }
    public String getDateString() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime((Date) get("date"));
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return month + "/" + day + "/" + year;
    }

    //Querying our Event class
    public static class Query extends ParseQuery<Event> {

        public Query() {
            super(Event.class);
        }

        public Event.Query getTop() {
            setLimit(20);
            return this;
        }

        public Event.Query withUser() {
            include("creator");
            return this;
        }
    }
}
