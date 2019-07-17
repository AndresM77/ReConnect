package com.example.reconnect.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.reconnect.ConnectionsAdapter;
import com.example.reconnect.MapActivity;
import com.example.reconnect.R;
import com.example.reconnect.model.Connection;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment {

    //Initializing fragment tag
    public final static String TAG = "MapFragment";
    //Initializing variables necessary for recycler view
    private RecyclerView rvConnections;
    private ConnectionsAdapter adapter;
    private List<Connection> mConnections;
    private SwipeRefreshLayout swipeContainer;
    //Initializing view objects
    private Button switchBtn;

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Setup view objects
        rvConnections = view.findViewById(R.id.rvConnections);
        //Instantiating connections list
        mConnections = new ArrayList<>();
        //Set up adapter
        adapter = new ConnectionsAdapter(getContext(), mConnections);
        //Set adapter on recycler view
        rvConnections.setAdapter(adapter);
        //Set up linear layout manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager((getContext()));
        //Set layout manager on recycler view
        rvConnections.setLayoutManager(linearLayoutManager);
        //Initializing view objects
        switchBtn = view.findViewById(R.id.btnSwitch);

        switchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(), MapActivity.class);
                startActivity(i);
            }
        });

        // Lookup the swipe container view
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
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
        ParseQuery<Connection> postQuery = new ParseQuery<Connection>(Connection.class);
        postQuery.include(Connection.KEY_USER1);
        postQuery.setLimit(20);
        postQuery.whereEqualTo(Connection.KEY_USER1, ParseUser.getCurrentUser());
        postQuery.addDescendingOrder(Connection.KEY_CREATED_AT);

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
                for (int i = 0; i < mConnections.size(); i++) {
                    Connection connection = connections.get(i);
                    Log.d(TAG, "User1: " + connection.getUser1().getUsername() + ", User2: " + connection.getUser2().getUsername());
                }
            }
        });
    }
}
