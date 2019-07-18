package com.example.reconnect;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.reconnect.model.Message;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class MessagesActivity extends AppCompatActivity {

    //Initializing fragment tag
    public final static String TAG = "MessagesActivity";
    //Initializing variables necessary for recycler view
    private RecyclerView rvMessages;
    private MessagesAdapter adapter;
    private List<Message> mMessage;
    private SwipeRefreshLayout swipeContainer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_messages);
        final ParseUser recipient = getIntent().getParcelableExtra("recipient");

        //Setup view objects
        rvMessages = findViewById(R.id.rvMessages);
        //Instantiating connections list
        mMessage = new ArrayList<>();
        //Set up adapter
        adapter = new MessagesAdapter(this, mMessage);
        //Set adapter on recycler view
        rvMessages.setAdapter(adapter);
        //Set up linear layout manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        //Set layout manager on recycler view
        rvMessages.setLayoutManager(linearLayoutManager);

        // Lookup the swipe container view
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                swipeContainer.setRefreshing(false);
                queryMessages(recipient);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        //query posts
        queryMessages(recipient);
    }

    public void queryMessages(ParseUser recipient) {
        ParseQuery<Message> postQuery = new ParseQuery<Message>(Message.class);
        postQuery.include(Message.KEY_SENDER);
        postQuery.setLimit(20);
        postQuery.whereEqualTo(Message.KEY_SENDER, ParseUser.getCurrentUser());
        postQuery.whereEqualTo(Message.KEY_RECIPIENT, recipient);
        postQuery.addDescendingOrder(Message.KEY_CREATED_AT);

        postQuery.findInBackground(new FindCallback<Message>() {
            @Override
            public void done(List<Message> messages, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error with Query");
                    e.printStackTrace();
                    return;
                }
                mMessage.clear();
                mMessage.addAll(messages);
                adapter.notifyDataSetChanged();
                for (int i = 0; i < mMessage.size(); i++) {
                    Message message = mMessage.get(i);
                    Log.d(TAG, "Sender: " + message.getSender().getUsername());
                }
            }
        });
    }
}
