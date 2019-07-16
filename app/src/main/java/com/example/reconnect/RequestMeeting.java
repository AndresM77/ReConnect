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
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class RequestMeeting extends AppCompatActivity {

    TextView requestee;
    EditText date;
    EditText startTime;
    EditText endTime;
    Button submitRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_meeting);

        requestee = findViewById(R.id.requesteeName);
        date = findViewById(R.id.requestDate);
        startTime = findViewById(R.id.startTime);
        endTime = findViewById(R.id.endTime);
        submitRequest = findViewById(R.id.submitRequest);

        // grab the objectId of the requested User
        String requestedUserId = getIntent().getStringExtra("requesteeId");

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
                Intent i = new Intent(RequestMeeting.this, CalendarFragment.class);

                // Need to pass the requested user, date,
                //TODO make sure i have all things to make event properly
            }
        });
    }
}
