package com.example.reconnect.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.reconnect.ConnectionsAdapter;
import com.example.reconnect.R;
import com.example.reconnect.model.Connection;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MapFragment extends Fragment {

    //Initializing fragment tag
    public final static String TAG = "TimelineFragment";
    //Initializing variables necessary for recycler view
    private RecyclerView rvConnections;
    private ConnectionsAdapter adapter;
    private List<Connection> mConnections;
    private SwipeRefreshLayout swipeContainer;


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
        //Setting adapter on recycler view
        adapter = new ConnectionsAdapter(getContext(), mConnections);
    }

    public void queryPosts(boolean EndlessScrolling) {
        ParseQuery<Connection> postQuery = new ParseQuery<Connection>(Connection.class);
        postQuery.include(Connection.KEY_USER1);
        postQuery.setLimit(20);
        postQuery.addDescendingOrder(Connection.KEY_CREATED_AT);
        Date maxDate;
        //Endless Pagination Functionality
        if (EndlessScrolling) {
            maxDate = mPosts.get(mPosts.size() - 1).getCreatedAt();
            postQuery.whereLessThan(Post.KEY_CREATED_AT, maxDate);
        }

        postQuery.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error with Querey");
                    e.printStackTrace();
                    return;
                }
                mPosts.clear();
                mPosts.addAll(posts);
                adapter.notifyDataSetChanged();
                for (int i = 0; i < posts.size(); i++) {
                    Post post = posts.get(i);
                    Log.d(TAG, "Post" + post.getDescription() + ", username: " + post.getUser().getUsername());
                }
            }
        });
    }
}
