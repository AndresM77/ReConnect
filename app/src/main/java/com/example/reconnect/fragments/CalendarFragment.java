package com.example.reconnect.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
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
import com.parse.ParseUser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CalendarFragment extends Fragment {

    //Initializing fragment tag
    public final static String TAG = "CalendarFragment";
    //Initializing variables necessary for recycler view
    private RecyclerView rvEvents;
    private EventAdapter adapter;
    private List<Object> mEvents;
    private TextView tvCurrentUsername;
    private SwipeRefreshLayout swipeContainer;

    // for changing profile picture
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public String photoFileName = "photo.jpg";
    private File photoFile;
    private ImageView ivProfilePic;
    private ImageView btnChangeProfile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Setup view objects
        rvEvents = view.findViewById(R.id.rvEvents);
        //Display username of current user
        tvCurrentUsername = view.findViewById(R.id.tvCurrentUsername);
        //Instantiating connections list
        mEvents = new ArrayList<>();
        //Set up adapter
        adapter = new EventAdapter(getContext(), mEvents, this);
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
        try {
            tvCurrentUsername.setText(user.fetchIfNeeded().get("firstName").toString() + "'s Calendar");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (user.get("profileImg") != null) {
            ParseFile img = (ParseFile) user.get("profileImg");
            Glide.with(getContext()).load(img.getUrl()).circleCrop().into(ivProfilePic);
        }
        btnChangeProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!hasWritePermissions() || !hasReadPermissions()) {
                    requestPermissions(
                            new String[] {
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                            }, 1234); // your request code
                }
                launchCamera();
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
                eventQuery(view);
            }
        });

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        //query posts
        eventQuery(view);
    }

    public void launchCamera() {
        Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(i, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    public void saveUser (File photoFile) {
        ParseFile photo = new ParseFile(photoFile);
        ParseUser user = ParseUser.getCurrentUser();
        user.put("profileImg", photo);
        user.saveInBackground();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:

                Bitmap photo = (Bitmap) intent.getExtras().get("data");

                FileOutputStream outputFileStream = null;

                String fileName = Environment.getExternalStorageDirectory() + "/hello.jpg";
                try {
                    outputFileStream = new FileOutputStream(fileName);

                    photo.compress(Bitmap.CompressFormat.JPEG, 100, outputFileStream);

                    File image = new File(fileName);

                    Glide.with(getContext()).load(image).circleCrop().into(ivProfilePic);
                    saveUser(image);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
        }

    }


    public void eventQuery(final View view) {
        Event.queryEvents(new FindCallback<Event>() {
            @Override
            public void done(List<Event> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error with Query");
                    e.printStackTrace();
                    return;
                }
                mEvents.clear();
                mEvents.addAll(createViewOrderArray(objects));
                adapter.notifyDataSetChanged();
                getView().findViewById(R.id.progressCalendar).setVisibility(View.GONE);
            }
        });
    }

    private boolean hasReadPermissions() {
        return (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    private boolean hasWritePermissions() {
        return (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    /* creates an array that represents the order of title and event views we wish to display on the profile */
    public ArrayList<Object> createViewOrderArray(List<Event> events) {
        ArrayList<Object> toReturn = new ArrayList<>();

        if (events.size() > 0) {
            Date dateTracker = events.get(0).getDate("date");
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d");
            Calendar cal = Calendar.getInstance();
            cal.setTime(dateTracker);
            int day = cal.get(Calendar.DAY_OF_MONTH);
            toReturn.add(new DateTitle(dateFormat.format(dateTracker)+ getEnding(day)));

            for (int i = 0; i < events.size(); i++) {
                Event currEvent = events.get(i);
                Date currEventDate = currEvent.getDate("date");

                if (!currEventDate.equals(dateTracker)) {
                    dateTracker = currEventDate;
                    Calendar cal2 = Calendar.getInstance();
                    cal2.setTime(dateTracker);
                    day = cal2.get(Calendar.DAY_OF_MONTH);
                    toReturn.add(new DateTitle(dateFormat.format(dateTracker) + getEnding(day)));
                }
                toReturn.add(currEvent);
            }
        }
        return toReturn;

    }

    public String getEnding(int day) {

        String stringDay = Integer.toString(day);
        char sigDigit = stringDay.charAt(stringDay.length()-1);

        if (day == 11 || day == 12 || day == 13) { return "th"; }

        switch(sigDigit) {
            case '1':
                return "st";
            case '2':
                return "nd";
            case '3':
                return "rd";
            default:
                return "th";
        }
    }

}
