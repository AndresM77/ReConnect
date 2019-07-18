package com.example.reconnect;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.reconnect.Dialogs.DatePickerFragment;
import com.example.reconnect.Dialogs.TimePickerFragment;
import com.example.reconnect.model.Event;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.sql.Date;

public class RequestMeeting extends AppCompatActivity {

    TextView request;
    EditText meetingName;
    EditText meetingDate;
    EditText startTime;
    EditText endTime;
    Button submitRequest;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_meeting);

        request = findViewById(R.id.requestMeetingPrompt);
        meetingName = findViewById(R.id.meetingName);
        meetingDate = findViewById(R.id.meetingDate);
        startTime = findViewById(R.id.startTime);
        endTime = findViewById(R.id.mtgEndTime);
        submitRequest = findViewById(R.id.submitRequest);

        // grab the objectId of the requested User
        final String requestedUserId = getIntent().getStringExtra("requesteeId");

        // find the requested User in our Parse database
        ParseQuery<ParseUser> userParseQuery = new ParseQuery<>(ParseUser.class);
        try {
            ParseUser requestedUser = userParseQuery.get(requestedUserId);
            request.setText("Request a meeting with " + requestedUser.getUsername() + ".");
        }
        catch(ParseException e) {
            Log.e("RequestMeeting Activity", "Unable to get the name of the requested User!");
            e.printStackTrace();
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
                        Intent i = new Intent(RequestMeeting.this, HomeActivity.class);
                        i.putExtra("meetingId", event.getObjectId());
                        startActivity(i);
                    }
                });
            }
        });
    }

    private DatePickerDoneListener getDatePickerDoneListener(final int dialogId) {
        return new DatePickerDoneListener() {
            @Override
            public void done(String dateText) {
                EditText meetingDate = findViewById(dialogId);
                meetingDate.setText(dateText);
            }
        };
    }

    private TimePickerDoneListener getPickerDoneListener(final int dialogId) {
        return new TimePickerDoneListener() {
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
