package com.example.reconnect.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.reconnect.Adapters.MessagesConnectionAdapter;
import com.example.reconnect.R;
import com.example.reconnect.model.Connection;
import com.example.reconnect.model.Conversation;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class MessageContactsActivity extends AppCompatActivity {

    //Initializing fragment tag
    public final static String TAG = "MessagesContactActivity";
    //Initializing variables necessary for recycler view
    private RecyclerView rvConnections;
    private MessagesConnectionAdapter adapter;
    private List<Connection> mConnections;
    private List<Conversation> mConversations;
    private SwipeRefreshLayout swipeContainer;
    private ContactClickListener listener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages_contacts);
        //Setup view objects
        rvConnections = findViewById(R.id.rvMessages);
        //Instantiating connections list
        mConnections = new ArrayList<>();
        mConversations = new ArrayList<>();
        //Initialize
        listener = new ContactClickListener() {
            @Override
            public void onClick(Connection connection) {
                query(connection);
                //returnActivity(connection);
            }
        };

        //Set up adapter
        adapter = new MessagesConnectionAdapter(this, mConnections, listener);
        //Set adapter on recycler view
        rvConnections.setAdapter(adapter);
        //Set up linear layout manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        //Set layout manager on recycler view
        rvConnections.setLayoutManager(linearLayoutManager);

        // Lookup the swipe container view
        swipeContainer = findViewById(R.id.swipeContainer);

        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                swipeContainer.setRefreshing(false);
                queryC();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        //query posts
        queryC();
    }

    private void queryC() {
        Connection.queryConnections(new FindCallback<Connection>() {
            @Override
            public void done(List<Connection> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error with Query");
                    e.printStackTrace();
                    return;
                }
                mConnections.clear();
                mConnections.addAll(objects);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void query(final Connection contact) {
        Conversation.queryConversations(new FindCallback<Conversation>() {
            @Override
            public void done(List<Conversation> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error with Query");
                    e.printStackTrace();
                    return;
                }
                mConversations.clear();
                mConversations.addAll(objects);
                Conversation conversation;
                Boolean change = true;
                for (int f = 0; f < mConversations.size(); f++) {
                    try {
                        if (mConversations.get(f).getConversee().fetchIfNeeded().getUsername().equals(ParseUser.getCurrentUser().fetchIfNeeded().getUsername())
                                || mConversations.get(f).getConverser().fetchIfNeeded().getUsername().equals(ParseUser.getCurrentUser().fetchIfNeeded().getUsername())) {
                            conversation = mConversations.get(f);
                            goToConversation(conversation);
                            change = false;
                        }
                    } catch (ParseException ee) {
                        ee.printStackTrace();
                    }
                }
                if (change) { createConversation(contact); }
            }
        });
    }

    public void createConversation(final Connection connection) {
        final Conversation conversation = new Conversation();
        conversation.setConverser(ParseUser.getCurrentUser());
        conversation.setConversee(connection.getOtherUser());
        conversation.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e!=null) {
                    Log.d(TAG, "Error while saving");
                    e.printStackTrace();
                    return;
                }
                Log.d(TAG, "Success");
                Intent i = new Intent(MessageContactsActivity.this, MessagesActivity.class);
                i.putExtra("conversation", conversation);
                startActivity(i);
            }
        });
    }

    private void goToConversation(Conversation conversation) {
        Intent i = new Intent(getApplicationContext(), MessagesActivity.class);
        i.putExtra("conversation", conversation);
        startActivity(i);
    }

    public interface ContactClickListener {
        void onClick(Connection connection);
    }
}
