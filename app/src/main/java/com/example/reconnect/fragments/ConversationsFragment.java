package com.example.reconnect.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.reconnect.Activities.MessagesActivity;
import com.example.reconnect.Adapters.ConversationsAdapter;
import com.example.reconnect.R;
import com.example.reconnect.model.Conversation;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class ConversationsFragment extends Fragment {

    //Initializing fragment tag
    public final static String TAG = "ConversationsFragment";
    public final static int REQUEST_CODE = 20;
    //Initializing variables necessary for recycler view
    private RecyclerView rvConversations;
    private TextView title;
    private ConversationsAdapter adapter;
    private List<Conversation> mConversations;
    private SwipeRefreshLayout swipeContainer;
    //Initializing extraneous view objects
    private ConversationClickListener listener;

    public static ConversationsFragment newInstance() {
        return new ConversationsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_conversation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Setup view objects
        title = view.findViewById(R.id.chats_tv);
        title.setText(ParseUser.getCurrentUser().get("firstName").toString() + "'s Chats");
        rvConversations = view.findViewById(R.id.rvConversations);
        //Instantiating connections list
        mConversations = new ArrayList<>();
        //Set up listener
        listener = new ConversationClickListener() {
            @Override
            public void onClick(Conversation conversation) {
                goToConversation(conversation);
            }
        };
        //Set up adapter
        adapter = new ConversationsAdapter(getContext(), mConversations, listener);
        //Set adapter on recycler view
        rvConversations.setAdapter(adapter);
        //Set up linear layout manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager((getContext()));
        //Set layout manager on recycler view
        rvConversations.setLayoutManager(linearLayoutManager);

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

    private void goToConversation(Conversation conversation) {
        Intent i = new Intent(getContext(), MessagesActivity.class);
        i.putExtra("conversation", conversation);
        startActivity(i);
    }

    private void query(final View view) {
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
                adapter.notifyDataSetChanged();
                view.findViewById(R.id.progressConversation).setVisibility(View.GONE);
            }
        });
    }

    public interface ConversationClickListener {
        void onClick(Conversation conversation);
    }
}