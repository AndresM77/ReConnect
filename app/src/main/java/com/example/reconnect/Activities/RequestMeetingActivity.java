package com.example.reconnect.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.reconnect.Dialogs.DatePickerFragment;
import com.example.reconnect.Dialogs.TimePickerFragment;
import com.example.reconnect.NotificationHandler;
import com.example.reconnect.R;
import com.example.reconnect.model.Connection;
import com.example.reconnect.model.Event;
import com.example.reconnect.model.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.sql.Date;

public class RequestMeetingActivity extends AppCompatActivity {

    //Profile Items
    TextView tvUserName;
    TextView tvIndustry;
    TextView tvDistance;
    ImageView ivProfileImg;
    Button btnMessage;
    //Meeting Items
    EditText meetingName;
    ImageView startTime;
    TextView tv_startTime;
    ImageView endTime;
    TextView tv_endTime;
    Button submitRequest;
    ImageView selectDate;
    TextView tv_meetingDate;
    ParseUser requestedUser;
    ParseFile profileImg = null;

    // Notifications
    private FirebaseFunctions mFunctions;
    final private String FCM_API = "https://fcm.googleapis.com/fcm/send";
    final private String serverKey = "key=" + "AAAAImePEvQ:APA91bGBbetvSXQVxAjLHzkm97o14Dam0rpXkOh1aCxVrUSJVYjYELneksrf_YNJdS8B-dLoQH6_-VUatNFX7V3xHFcUsuXqz-SNhEdugthrpfljrwyC8JLcY3vcmIrvMO5W43AM2LCE"
    ;
    final private String contentType = "application/json";
    final String TAG = "NOTIFICATION TAG";
    public final String SENDER_ID = "147766317812";
    String NOTIFICATION_TITLE;
    String NOTIFICATION_MESSAGE;
    String TOPIC;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set the layout
        setContentView(R.layout.activity_request_meeting);

        setProfileItems();

        setMeetingItems();

        setUpToolbar();

        // grab the objectId of the requested User
        final String requestedUserId = getIntent().getStringExtra("requesteeId");

        // find the requested User in our Parse database
        ParseQuery<ParseUser> userParseQuery = new ParseQuery<>(ParseUser.class);

        findAndSetUser(userParseQuery, requestedUserId);

        if (profileImg != null) {
            Glide.with(getBaseContext()).load(profileImg.getUrl()).circleCrop().into(ivProfileImg);
        }


        selectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment datePicker = new DatePickerFragment(getDatePickerDoneListener(R.id.tv_meetingDate));
                datePicker.show(getSupportFragmentManager(), "DatePicker");
            }
        });

        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment timePicker = new TimePickerFragment(getPickerDoneListener(R.id.tv_startTime));
                timePicker.show(getSupportFragmentManager(), "TimePicker");
            }
        });

        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment timePicker = new TimePickerFragment(getPickerDoneListener(R.id.tv_endTime));
                timePicker.show(getSupportFragmentManager(), "TimePicker");
            }
        });

        // onClick listener for the submit action
        submitRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // create Event for the requested meeting
                // Creates event under the user's profile section
                final Event event = new Event();

                createEventUnderProfile(event);

                ParseQuery<ParseUser> userParseQuery = new ParseQuery<>(ParseUser.class);
                try {
                    ParseUser requestedUser = userParseQuery.get(requestedUserId);
                    event.put("attendee", requestedUser);
                }
                catch(ParseException e) {
                    Log.e("RequestMeeting Activity", "Unable to get the name of the requested User for the Event!");
                    e.printStackTrace();
                }

                event.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.d("requestMeeting", "Error while saving");
                            e.printStackTrace();
                            return;
                        }
                        finish();
                    }
                });

                // sends notification
                NotificationHandler nHandler = new NotificationHandler(FirebaseFunctions.getInstance());
                Task<String> result = nHandler.sendNotifications(event.getAttendee().get("deviceId").toString(),
                        "You have a new meeting request from " + User.getFullName(event.getAttendee()));

                // send back to the Calendar when done
                Intent i = new Intent(RequestMeetingActivity.this, HomeActivity.class);
                i.putExtra("sendToCalendar", "yes");
                startActivity(i);
            }


        });
    }

    public void setProfileItems() {
        //Profile items
        tvUserName = findViewById(R.id.tvUserName);
        tvIndustry = findViewById(R.id.tvIndustry);
        tvDistance = findViewById(R.id.tvDistance);
        ivProfileImg = findViewById(R.id.ivProfileImg);
        btnMessage = findViewById(R.id.btnMessage);
    }

    public void setMeetingItems() {
        //Meeting items
        meetingName = findViewById(R.id.meetingName);
        selectDate = findViewById(R.id.selectDate);
        tv_meetingDate = findViewById(R.id.tv_meetingDate);
        startTime = findViewById(R.id.startTime);
        endTime = findViewById(R.id.endTime);
        submitRequest = findViewById(R.id.submitRequest);
        tv_startTime = findViewById(R.id.tv_startTime);
        tv_endTime = findViewById(R.id.tv_endTime);
    }

    public void setUpToolbar() {
        // Find the toolbar view inside the activity layout
        Toolbar toolbar = findViewById(R.id.toolbar);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void findAndSetUser( ParseQuery<ParseUser> userParseQuery, String requestedUserId ) {
        try {
            requestedUser = userParseQuery.get(requestedUserId);
            if (!requestedUser.equals(ParseUser.getCurrentUser())) {
                tvUserName.setText(User.getFullName(requestedUser));
                tvIndustry.setText((String) requestedUser.fetchIfNeeded().get("industry"));
                profileImg = (ParseFile) requestedUser.fetchIfNeeded().get("profileImg");
                tvDistance.setText(Connection.getDistanceAway(ParseUser.getCurrentUser().getParseGeoPoint("location"),requestedUser.getParseGeoPoint("location")));
            }
            else {
//                TextView prompt = findViewById(R.id.requestMeetingPrompt);
//                prompt.setText("Add personal event.");
                ivProfileImg.setVisibility(View.GONE);
                tvUserName.setVisibility(View.GONE);
                tvIndustry.setVisibility(View.GONE);
                tvDistance.setVisibility(View.GONE);
            }
        }
        catch(ParseException e) {
            Log.e("RequestMeeting Activity", "Unable to get the name of the requested User!");
            e.printStackTrace();
        }
    }

    public void createEventUnderProfile(Event event) {
        event.put("startTime", tv_startTime.getText().toString());
        event.put("endTime", tv_endTime.getText().toString());
        event.put("name", meetingName.getText().toString());
        event.put("creator", ParseUser.getCurrentUser());
        event.put("pending", true);
        event.put("accepted", false);
        event.put("reconnect", true);
        event.put("date", Date.valueOf(tv_meetingDate.getText().toString()));
    }

    private RequestMeetingActivity.DatePickerDoneListener getDatePickerDoneListener(final int dialogId) {
        return new RequestMeetingActivity.DatePickerDoneListener() {
            @Override
            public void done(String dateText) {
                TextView tv_meetingDate = findViewById(dialogId);
                tv_meetingDate.setText(dateText);
            }
        };
    }

    private RequestMeetingActivity.TimePickerDoneListener getPickerDoneListener(final int dialogId) {
        return new RequestMeetingActivity.TimePickerDoneListener() {
            @Override
            public void done(String timeText) {
                TextView timeSet = findViewById(dialogId);
                timeSet.setText(timeText);
            }
        };
    }

    public interface DatePickerDoneListener {
        void done(String dateText);
    }

    public interface TimePickerDoneListener {
        void done(String timeText);
    }

    // for toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_request, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.ivSettings:
                //Take to settings activity
                Intent i = new Intent(RequestMeetingActivity.this, SettingsActivity.class);
                startActivity(i); // brings up the second activity
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}