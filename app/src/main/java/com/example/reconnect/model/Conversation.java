package com.example.reconnect.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

@ParseClassName("Conversation")
public class Conversation extends ParseObject {
    public static final String KEY_CONVERSER = "converser";
    public static final String KEY_CONVERSEE = "conversee";
    public static final String KEY_LAST_MESSAGE = "lastMessage";
    public static final String KEY_CREATED_AT = "createdAt";

    public ParseUser getConverser() {return getParseUser(KEY_CONVERSER);}

    public void setConverser(ParseUser converser) {put(KEY_CONVERSER, converser);}

    public ParseUser getConversee() {return getParseUser(KEY_CONVERSEE);}

    public void setConversee(ParseUser conversee) {put(KEY_CONVERSEE, conversee);}

    public ParseObject getLastMessage() {return getParseObject(KEY_LAST_MESSAGE);}

    public void setLastMessage(Message message) {put(KEY_LAST_MESSAGE, message);}

    //Querying our Conversation class
    public static class Query extends ParseQuery<Conversation> {

        public Query() {
            super(Conversation.class);
        }

        public Conversation.Query getTop() {
            setLimit(20);
            return this;
        }

        public Conversation.Query withUser() {
            include("converser");
            return this;
        }
    }
}
