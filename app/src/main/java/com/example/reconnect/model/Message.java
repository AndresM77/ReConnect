package com.example.reconnect.model;

import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.Date;

@ParseClassName("Message")
public class Message extends ParseObject {
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_CONVERSATION = "conversation";
    public static final String KEY_SENDER = "sender";
    public static final String KEY_RECIPIENT ="recipient";
    public static final String KEY_CREATED_AT = "createdAt";
    private static final String KEY_IS_REQUEST = "isRequest";

    public String getMessage() { return getString(KEY_MESSAGE); }

    public void setMessage(String message) {put(KEY_MESSAGE, message);}

    public ParseObject getConversation() {
        return getParseObject(KEY_CONVERSATION);
    }

    public void setConversation(Conversation conversation) {put(KEY_CONVERSATION, conversation);}

    public ParseUser getSender() {
        return getParseUser(KEY_SENDER);
    }

    public void setSender(ParseUser sender) {put(KEY_SENDER, sender);}

    public ParseUser getRecipient() {
        return getParseUser(KEY_RECIPIENT);
    }

    public void setRecipient(ParseUser recipient) {put(KEY_RECIPIENT, recipient);}

    public Boolean getIsRequest() { return getBoolean(KEY_IS_REQUEST); }

    public void setIsRequest(Boolean bool) {put(KEY_IS_REQUEST, bool);}

    @Override
    public Date getCreatedAt() {
        return super.getCreatedAt();
    }

    public ParseUser getCurrentUser() {
        if (ParseUser.getCurrentUser().getUsername().equals(getSender().getUsername())){
            return getSender();
        } else {
            return getRecipient();
        }
    }

    public ParseUser getOtherUser() {
        if (ParseUser.getCurrentUser().getUsername().equals(getSender().getUsername())){
            return getRecipient();
        } else {
            return getSender();
        }
    }

    //Save a message
    private void saveMessage(final Conversation conversation, String messageText, final String TAG, final SaveCallback callback) {
        final Message message = new Message();
        message.setConversation(conversation);
        message.setMessage(messageText);
        message.setRecipient(conversation.getOtherUser());
        message.setSender(ParseUser.getCurrentUser());
        message.setIsRequest(false);
        message.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e!=null) {
                    Log.d(TAG, "Error while saving");
                    e.printStackTrace();
                    return;
                }
                Log.d(TAG, "Success");
                conversation.setLastMessage(message);
                conversation.saveInBackground(callback);
            }
        });
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
