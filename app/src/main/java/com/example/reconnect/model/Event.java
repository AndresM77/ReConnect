package com.example.reconnect.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.Date;

@ParseClassName("Event")
public class Event extends ParseObject {
    public static final String KEY_START_TIME = "startTime";
    public static final String KEY_END_TIME = "endTime";
    public static final String KEY_NAME = "name";
    public static final String KEY_CREATOR = "creator";
    public static final String KEY_ATTENDEE = "attendee";
    public static final String KEY_PENDING = "pending";
    public static final String KEY_SPECIAL = "reconnect";

    public Date getStartTime() {return getDate(KEY_START_TIME);}

    public void setStartTime(Date startTime) {
        put(KEY_START_TIME, startTime);
    }

    public Date getEndTime() {return getDate(KEY_END_TIME);}

    public void setEndTime(Date endTime) {put(KEY_END_TIME, endTime);}

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
}