package com.example.reconnect.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.reconnect.Adapters.UsersAdapter;
import com.example.reconnect.R;
import com.example.reconnect.model.Connection;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class AllUsersActivity extends AppCompatActivity {

    //Initializing fragment tag
    public final static String TAG = "AllUsersActivity";
    //Initializing variables necessary for recycler view
    private RecyclerView rvConnections;
    private UsersAdapter adapter;
    private List<ParseUser> mUsers;
    private List<Connection> mConnections;
    private SwipeRefreshLayout swipeContainer;
    private UserClickListener listener;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages_contacts);
        //Setup view objects
        rvConnections = findViewById(R.id.rvMessages);
        //Instantiating connections list
        mUsers = new ArrayList<>();
        mConnections = new ArrayList<>();
        //Initialize
        listener = new UserClickListener() {
            @Override
            public void onClick(ParseUser user) {
                //checking to see if unique connection
                for (int i = 0; i < mConnections.size(); i++) {
                    try {
                        if (mConnections.get(i).getOtherUser().fetchIfNeeded().getUsername().equals(user.fetchIfNeeded().getUsername())) {
                            return;
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                saveConnection(user);
            }
        };

        //Set up adapter
        adapter = new UsersAdapter(getBaseContext(), mUsers, listener);
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
                queryU();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        //query posts
        queryU();
    }

    private void saveConnection(ParseUser user) {
        Connection connection = new Connection();
        connection.put("user1", ParseUser.getCurrentUser());
        connection.put("user2", user);

        connection.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e!=null) {
                    Log.d("AllUsersActivity", "Error while saving");
                    e.printStackTrace();
                    return;
                }
                Log.d("AllUsersActivity", "Success");
                Intent i = new Intent(AllUsersActivity.this, SettingsActivity.class);
                startActivity(i);
            }
        });
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

    private void queryU() {
        ParseQuery<ParseUser> postQuery = new ParseQuery<>(ParseUser.class);
        postQuery.whereNotEqualTo("username", ParseUser.getCurrentUser());

        postQuery.addDescendingOrder("createdAt");
        postQuery.setLimit(20);

        postQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                mUsers.clear();
                mUsers.addAll(objects);
                adapter.notifyDataSetChanged();
            }
        });
    }

    public interface UserClickListener {
        void onClick(ParseUser user);
    }
}
