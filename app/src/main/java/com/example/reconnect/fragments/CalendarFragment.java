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

import com.example.reconnect.ConnectionsAdapter;
import com.example.reconnect.R;
import com.example.reconnect.model.Event;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class CalendarFragment extends Fragment {

    //Initializing fragment tag
    public final static String TAG = "CalendarFragment";
    //Initializing variables necessary for recycler view
    private RecyclerView rvEvents;
    private ConnectionsAdapter adapter;
    private List<Event> mEvents;
    private SwipeRefreshLayout swipeContainer;
    //Initializing view objects
    private Button switchBtn;

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Setup view objects
        rvEvents = view.findViewById(R.id.rvEvents);
        //Instantiating connections list
        mEvents = new ArrayList<>();
        //Set up adapter
        //adapter = new EventAdapter(getContext(), mEvents);
        //Set adapter on recycler view
        rvEvents.setAdapter(adapter);
        //Set up linear layout manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager((getContext()));
        //Set layout manager on recycler view
        rvEvents.setLayoutManager(linearLayoutManager);

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
                queryEvents();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        //query posts
        queryEvents();
    }

    public void queryEvents() {
        ParseQuery<Event> postQuery = new ParseQuery<Event>(Event.class);
        postQuery.include(Event.KEY_CREATOR);
        postQuery.setLimit(20);
        postQuery.whereEqualTo(Event.KEY_CREATOR, ParseUser.getCurrentUser());
        postQuery.addDescendingOrder(Event.KEY_CREATED_AT);

        postQuery.findInBackground(new FindCallback<Event>() {
            @Override
            public void done(List<Event> events, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error with Query");
                    e.printStackTrace();
                    return;
                }
                mEvents.clear();
                mEvents.addAll(events);
                adapter.notifyDataSetChanged();
                for (int i = 0; i < mEvents.size(); i++) {
                    Event event = mEvents.get(i);
                    Log.d(TAG, "Creator: " + event.getCreator().getUsername());
                }
            }
        });
    }
}
