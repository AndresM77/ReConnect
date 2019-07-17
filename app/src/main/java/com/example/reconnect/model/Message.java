package com.example.reconnect.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Date;

@ParseClassName("Message")
public class Message extends ParseObject {
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_ID = "id";
    public static final String KEY_SENDER = "sender";
    public static final String KEY_RECIPIENT ="recipient";
    public static final String KEY_CREATED_AT = "createdAt";

    public String getMessage() {
        return getString(KEY_MESSAGE);
    }

    public void setMessage(String message) {put(KEY_MESSAGE, message);}

    public Integer getId() {
        return getInt(KEY_ID);
    }

    public void setID(int id) {put(KEY_ID, id);}

    public ParseUser getSender() {
        return getParseUser(KEY_SENDER);
    }

    public void setSender(ParseUser sender) {put(KEY_SENDER, sender);}

    public ParseUser getRecipient() {
        return getParseUser(KEY_RECIPIENT);
    }

    public void setRecipient(ParseUser recipient) {put(KEY_RECIPIENT, recipient);}

    @Override
    public Date getCreatedAt() {
        return super.getCreatedAt();
    }

    //Querying our Message class
    public static class Query extends ParseQuery<Message> {

        public Query() {
            super(Message.class);
        }

        public Message.Query getTop() {
            setLimit(20);
            return this;
        }

        public Message.Query withUser() {
            include("sender");
            return this;
        }
    }
}
