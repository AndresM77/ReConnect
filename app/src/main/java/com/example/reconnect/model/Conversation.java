package com.example.reconnect.model;

import com.parse.FindCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

@ParseClassName("Conversation")
public class
Conversation extends ParseObject {
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

    public ParseUser getCurrentUser() {
        try {
            if (ParseUser.getCurrentUser().fetchIfNeeded().getUsername().equals(getConversee().getUsername())){
                return getConversee();
            } else {
                return getConverser();
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return getConversee();
        }
    }

    public ParseUser getOtherUser() {
        try {
            if (ParseUser.getCurrentUser().fetchIfNeeded().getUsername().equals(getConversee().getUsername())){
                return getConverser();
            } else {
                return getConversee();
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return getConverser();
        }
    }

    public static void queryConversations(FindCallback<Conversation> callback) {

        ParseQuery<Conversation> postQuery = new ParseQuery<>(Conversation.class);
        postQuery.whereEqualTo(Conversation.KEY_CONVERSER, ParseUser.getCurrentUser());

        ParseQuery<Conversation> postQuery2 = new ParseQuery<>(Conversation.class);
        postQuery2.whereEqualTo(Conversation.KEY_CONVERSEE, ParseUser.getCurrentUser());

        List<ParseQuery<Conversation>> queries = new ArrayList<>();
        queries.add(postQuery);
        queries.add(postQuery2);

        ParseQuery<Conversation> mainQuery = ParseQuery.or(queries);

        mainQuery.include("converser");
        mainQuery.include("conversee");
        mainQuery.include("lastMessage");
        mainQuery.addDescendingOrder(Conversation.KEY_CREATED_AT);

        mainQuery.findInBackground(callback);
    }

    public static void findConversation(ParseUser user1, ParseUser user2, FindCallback<Conversation> callback) {
        ParseQuery<Conversation> query1 = new ParseQuery<Conversation>(Conversation.class);
        query1.whereEqualTo(Conversation.KEY_CONVERSER, user1);
        query1.whereEqualTo(Conversation.KEY_CONVERSEE, user2);


        ParseQuery<Conversation> query2 = new ParseQuery<Conversation>(Conversation.class);
        query2.whereEqualTo(Conversation.KEY_CONVERSEE, user1);
        query2.whereEqualTo(Conversation.KEY_CONVERSER, user2);


        List<ParseQuery<Conversation>> queries = new ArrayList<>();
        queries.add(query1);
        queries.add(query2);

        ParseQuery<Conversation> mainQuery = ParseQuery.or(queries);

        mainQuery.include("converser");
        mainQuery.include("conversee");
        mainQuery.include("lastMessage");

        mainQuery.findInBackground(callback);
    }

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
            include("conversee");
            return this;
        }
    }
}
