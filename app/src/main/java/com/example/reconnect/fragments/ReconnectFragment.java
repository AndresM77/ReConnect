package com.example.reconnect.fragments;

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

import com.example.reconnect.Adapters.ConnectionsAdapter;
import com.example.reconnect.R;
import com.example.reconnect.model.Connection;
import com.example.reconnect.model.Conversation;
import com.parse.FindCallback;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.List;

public class ReconnectFragment extends Fragment {

    //Initializing fragment tag
    public final static String TAG = "ReconnectFragment";
    //Initializing variables necessary for recycler view
    private RecyclerView rvConnections;
    private ConnectionsAdapter adapter;
    private List<Connection> mConnections;
    private SwipeRefreshLayout swipeContainer;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reconnect, container, false);
    }

    public static ReconnectFragment newInstance() {
        return new ReconnectFragment();
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
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

        // Lookup the swipe container view
        swipeContainer = view.findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                swipeContainer.setRefreshing(false);
                query(view);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        //query posts
        query(view);
    }

    private void query(final View view) {
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
                view.findViewById(R.id.progressContacts).setVisibility(View.GONE);
            }
        });
    }

    // will wort the queried connections based on how far away they are
    private void sortConnections(List<Connection> mConnections) {

    }
}
