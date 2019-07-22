package com.example.reconnect.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.reconnect.Adapters.EventAdapter;
import com.example.reconnect.R;
import com.example.reconnect.model.DateTitle;
import com.example.reconnect.model.Event;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class CalendarFragment extends Fragment {

    //Initializing fragment tag
    public final static String TAG = "CalendarFragment";
    //Initializing variables necessary for recycler view
    private RecyclerView rvEvents;
    private EventAdapter adapter;
    private List<Event> mEvents;
    private TextView tvCurrentUsername;
    private SwipeRefreshLayout swipeContainer;

    // for changing profile picture
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public String photoFileName = "photo.jpg";
    private File photoFile;
    private ImageView ivProfilePic;
    private Button btnChangeProfile;

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
        //Display username of current user
        tvCurrentUsername = view.findViewById(R.id.tvCurrentUsername);
        //Instantiating connections list
        mEvents = new ArrayList<>();
        //Set up adapter
        adapter = new EventAdapter(getContext(), mEvents);
        //Set adapter on recycler view
        rvEvents.setAdapter(adapter);
        //Set up linear layout manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager((getContext()));
        //Set layout manager on recycler view
        rvEvents.setLayoutManager(linearLayoutManager);

        //Set up button to change profile picture
        btnChangeProfile = view.findViewById(R.id.btnChangeProfile);
        //Set up profile picture to be changed
        ivProfilePic = view.findViewById(R.id.ivProfilePic);

        ParseUser user = ParseUser.getCurrentUser();

        if (user.get("profileImg") != null) {
            ParseFile img = (ParseFile) user.get("profileImg");
            Glide.with(getContext()).load(img.getUrl()).into(ivProfilePic);
        }
        btnChangeProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchCamera();
                if (photoFile == null) {
                    Log.e(TAG, "No photo to submit");
                    Toast.makeText(getContext(), "There is no photo!", Toast.LENGTH_SHORT).show();
                    return;
                }
                saveUser(photoFile);
                Glide.with(getContext()).load(photoFile).into(ivProfilePic);
            }
        });

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
                queryEvents();
            }
        });

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        //query posts
        queryEvents();
    }

    public void launchCamera() {
        Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        if (hasImageCaptureBug()) {
            i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File("/sdcard/tmp")));
        } else {
            i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        startActivityForResult(i, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    public void saveUser (File photoFile) {
        ParseFile photo = new ParseFile(photoFile);
        ParseUser user = ParseUser.getCurrentUser();
        user.put("profileImg", photo);
        user.saveInBackground();
    }

    public boolean hasImageCaptureBug() {

        // list of known devices that have the bug
        ArrayList<String> devices = new ArrayList<String>();
        devices.add("android-devphone1/dream_devphone/dream");
        devices.add("generic/sdk/generic");
        devices.add("vodafone/vfpioneer/sapphire");
        devices.add("tmobile/kila/dream");
        devices.add("verizon/voles/sholes");
        devices.add("google_ion/google_ion/sapphire");

        return devices.contains(android.os.Build.BRAND + "/" + android.os.Build.PRODUCT + "/"
                + android.os.Build.DEVICE);

    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:
                Uri u;
                if (hasImageCaptureBug()) {
                    File fi = new File("/sdcard/tmp");
                    try {
                        u = Uri.parse(android.provider.MediaStore.Images.Media.insertImage(getContext().getContentResolver(), fi.getAbsolutePath(), null, null));
                        if (!fi.delete()) {
                            Log.i("profileImg", "Failed to delete " + fi);
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                } else {
                    u = intent.getData();
                }
        }
    }


    public void queryEvents() {
        ParseQuery<Event> postQuery = new ParseQuery<>(Event.class);
        postQuery.include(Event.KEY_CREATOR);
        postQuery.addAscendingOrder(Event.KEY_DATE);
        //TODO add: postQuery.whereLessThan("KEY_DATE", new Date(System.currentTimeMillis()));
        postQuery.setLimit(20);

        // Work to create event on both user profiles (the creator and the attendee)
        if (Event.KEY_CREATOR.equals(ParseUser.getCurrentUser())) {
            postQuery.whereEqualTo(Event.KEY_CREATOR, ParseUser.getCurrentUser());
        } else if (Event.KEY_ATTENDEE.equals(ParseUser.getCurrentUser())) {
            postQuery.whereEqualTo(Event.KEY_ATTENDEE, ParseUser.getCurrentUser());
        }

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
            }
        });

    }
}
