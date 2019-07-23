package com.example.reconnect.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.reconnect.Dialogs.DatePickerFragment;
import com.example.reconnect.Dialogs.TimePickerFragment;
import com.example.reconnect.R;
import com.example.reconnect.model.Event;
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
    Button btnReturn;
    //Meeting Items
    TextView request;
    EditText meetingName;
    EditText meetingDate;
    EditText startTime;
    EditText endTime;
    Button submitRequest;
    ParseUser requestedUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_request_meeting);

        //Profile items
        tvUserName = findViewById(R.id.tvUserName);
        tvIndustry = findViewById(R.id.tvIndustry);
        tvDistance = findViewById(R.id.tvDistance);
        ivProfileImg = findViewById(R.id.ivProfileImg);
        btnMessage = findViewById(R.id.btnMessage);
        btnReturn = findViewById(R.id.btnReturn);
        //Meeting items
        request = findViewById(R.id.requestMeetingPrompt);
        meetingName = findViewById(R.id.meetingName);
        meetingDate = findViewById(R.id.meetingDate);
        startTime = findViewById(R.id.startTime);
        endTime = findViewById(R.id.mtgEndTime);
        submitRequest = findViewById(R.id.submitRequest);

        // grab the objectId of the requested User
        final String requestedUserId = getIntent().getStringExtra("requesteeId");
        ParseFile profileImg = null;

        // find the requested User in our Parse database
        ParseQuery<ParseUser> userParseQuery = new ParseQuery<>(ParseUser.class);
        try {
            requestedUser = userParseQuery.get(requestedUserId);
            request.setText("Request a Meeting");
            tvUserName.setText(requestedUser.fetchIfNeeded().getUsername());
            tvIndustry.setText((String) requestedUser.fetchIfNeeded().get("industry"));
            profileImg = (ParseFile) requestedUser.fetchIfNeeded().get("profileImg");

        }
        catch(ParseException e) {
            Log.e("RequestMeeting Activity", "Unable to get the name of the requested User!");
            e.printStackTrace();
        }
        if (profileImg != null) {
            Glide.with(getBaseContext()).load(profileImg.getUrl()).into(ivProfileImg);
        }


        meetingDate.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                DialogFragment datePicker = new DatePickerFragment(getDatePickerDoneListener(R.id.meetingDate));
                datePicker.show(getSupportFragmentManager(), "DatePicker");
                //prevents the keyboard from popping up
                return true;
            }
        });

        startTime.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                DialogFragment timePicker = new TimePickerFragment(getPickerDoneListener(R.id.startTime));
                timePicker.show(getSupportFragmentManager(), "TimePicker");
                return true;
            }
        });

        endTime.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                DialogFragment timePicker = new TimePickerFragment(getPickerDoneListener(R.id.mtgEndTime));
                timePicker.show(getSupportFragmentManager(), "TimePicker");
                return true;
            }
        });

        btnMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(RequestMeetingActivity.this, HomeActivity.class);
                startActivity(i);
            }
        });

        // onClick listener for the submit action
        submitRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // create Event for the requested meeting
                final Event event = new Event();
                event.put("startTime", startTime.getText().toString());
                event.put("endTime", endTime.getText().toString());
                event.put("name", meetingName.getText().toString());
                event.put("creator", ParseUser.getCurrentUser());
                event.put("pending", true);
                event.put("accepted", false);
                event.put("reconnect", true);
                event.put("date", Date.valueOf(meetingDate.getText().toString()));

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
                        Intent i = new Intent(RequestMeetingActivity.this, HomeActivity.class);
                        i.putExtra("meetingId", event.getObjectId());
                        startActivity(i);
                    }
                });
            }
        });
    }

    private RequestMeetingActivity.DatePickerDoneListener getDatePickerDoneListener(final int dialogId) {
        return new RequestMeetingActivity.DatePickerDoneListener() {
            @Override
            public void done(String dateText) {
                EditText meetingDate = findViewById(dialogId);
                meetingDate.setText(dateText);
            }
        };
    }

    private RequestMeetingActivity.TimePickerDoneListener getPickerDoneListener(final int dialogId) {
        return new RequestMeetingActivity.TimePickerDoneListener() {
            @Override
            public void done(String timeText) {
                EditText meetingTime = findViewById(dialogId);
                meetingTime.setText(timeText);
            }
        };
    }

    public interface DatePickerDoneListener {
        void done(String dateText);
    }

    public interface TimePickerDoneListener {
        void done(String timeText);
    }
}