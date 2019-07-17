package com.example.reconnect;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.reconnect.model.Connection;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class MessageContactsActivity extends AppCompatActivity {

    //Initializing fragment tag
    public final static String TAG = "MessagesContactActivity";
    //Initializing variables necessary for recycler view
    private RecyclerView rvConnections;
    private MessagesConnectionAdapter adapter;
    private List<Connection> mConnections;
    private SwipeRefreshLayout swipeContainer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages_contacts);
        //Setup view objects
        rvConnections = findViewById(R.id.rvConnections);
        //Instantiating connections list
        mConnections = new ArrayList<>();
        //Set up adapter
        adapter = new MessagesConnectionAdapter(this, mConnections);
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
                queryConnections();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        //query posts
        queryConnections();
    }

    public void queryConnections() {
        ParseQuery<Connection> postQuery = new ParseQuery<>(Connection.class);
        postQuery.include(Connection.KEY_USER1);
        postQuery.setLimit(20);

        postQuery.addDescendingOrder(Connection.KEY_CREATED_AT);
        postQuery.whereEqualTo(Connection.KEY_USER1, ParseUser.getCurrentUser());
        // TODO - Add a check for KEY_USER2 and currentUser

        postQuery.findInBackground(new FindCallback<Connection>() {
            @Override
            public void done(List<Connection> connections, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error with Query");
                    e.printStackTrace();
                    return;
                }
                mConnections.clear();
                mConnections.addAll(connections);
                adapter.notifyDataSetChanged();
            }
        });
    }
}
