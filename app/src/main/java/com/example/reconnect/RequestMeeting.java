package com.example.reconnect;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.reconnect.fragments.CalendarFragment;
import com.example.reconnect.model.Event;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class RequestMeeting extends AppCompatActivity {

    TextView requestee;
    EditText meetingName;
    EditText date;
    EditText startTime;
    EditText endTime;
    Button submitRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_meeting);

        requestee = findViewById(R.id.requesteeName);
        meetingName = findViewById(R.id.meetingName);
        date = findViewById(R.id.requestDate);
        startTime = findViewById(R.id.startTime);
        endTime = findViewById(R.id.endTime);
        submitRequest = findViewById(R.id.submitRequest);

        // grab the objectId of the requested User
        final String requestedUserId = getIntent().getStringExtra("requesteeId");

        // find the requested User in our Parse database
        ParseQuery<ParseUser> userParseQuery = new ParseQuery<>(ParseUser.class);
        try {
            ParseUser requestedUser = userParseQuery.get(requestedUserId);
            requestee.setText(requestedUser.getUsername());
        }
        catch(ParseException e) {
            Log.e("RequestMeeting Activity", "Unable to get the name of the requested User!");
            e.printStackTrace();
        }

        // onClick listener for the submit action
        submitRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // create Event for the requested meeting
                final Event event = new Event();
                event.put("startTime", startTime.getText());
                event.put("endTime", endTime.getText());
                event.put("name", meetingName.getText());
                event.put("creator", ParseUser.getCurrentUser().getObjectId());
                event.put("pending", true);
                event.put("reconnect", true);
                event.put("date", date.getText());

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
                        Intent i = new Intent(RequestMeeting.this, CalendarFragment.class);
                        i.putExtra("meetingId", event.getObjectId());
                        startActivity(i);
                    }
                });
            }
        });
    }
}
