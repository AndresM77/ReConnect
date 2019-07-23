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
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import static com.example.reconnect.fragments.ConversationsFragment.REQUEST_CODE;

public class MessageContactsActivity extends AppCompatActivity {

    //Initializing fragment tag
    public final static String TAG = "MessagesContactActivity";
    //Initializing variables necessary for recycler view
    private RecyclerView rvConnections;
    private MessagesConnectionAdapter adapter;
    private List<Connection> mConnections;
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
        //Initialize
        listener = new ContactClickListener() {
            @Override
            public void onClick(Connection connection) {
                returnActivity(connection);
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

    private void returnActivity(Connection connection) {
        // Send intent back to home activity after selecting
        // a contact User name
        Intent intent = new Intent(this, RequestMeetingActivity.class);
        intent.putExtra("connection", connection);
        setResult(REQUEST_CODE, intent);
        finish();
    }


    public void queryConnections() {
        ParseQuery<Connection> postQuery = new ParseQuery<>(Connection.class);
        postQuery.whereEqualTo(Connection.KEY_USER1, ParseUser.getCurrentUser());

        ParseQuery<Connection> postQuery2 = new ParseQuery<>(Connection.class);
        postQuery2.whereEqualTo(Connection.KEY_USER2, ParseUser.getCurrentUser());

        List<ParseQuery<Connection>> queries = new ArrayList<>();
        queries.add(postQuery);
        queries.add(postQuery2);

        ParseQuery<Connection> mainQuery = ParseQuery.or(queries);
        postQuery.addDescendingOrder(Connection.KEY_CREATED_AT);
        postQuery.setLimit(20);

        mainQuery.findInBackground(new FindCallback<Connection>() {
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

    public interface ContactClickListener {
        void onClick(Connection connection);
    }
}
