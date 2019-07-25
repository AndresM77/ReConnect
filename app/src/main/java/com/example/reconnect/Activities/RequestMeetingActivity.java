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
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.example.reconnect.Dialogs.DatePickerFragment;
import com.example.reconnect.Dialogs.TimePickerFragment;
import com.example.reconnect.MySingleton;
import com.example.reconnect.R;
import com.example.reconnect.model.Event;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

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

    // Notifications
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
            if (!requestedUser.equals(ParseUser.getCurrentUser())) {
                tvUserName.setText(requestedUser.fetchIfNeeded().getUsername());
                tvIndustry.setText((String) requestedUser.fetchIfNeeded().get("industry"));
                profileImg = (ParseFile) requestedUser.fetchIfNeeded().get("profileImg");
            }
            else {
                TextView prompt = findViewById(R.id.requestMeetingPrompt);
                prompt.setText("Add personal event.");
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
        if (profileImg != null) {
            Glide.with(getBaseContext()).load(profileImg.getUrl()).circleCrop().into(ivProfileImg);
        }


        meetingDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment datePicker = new DatePickerFragment(getDatePickerDoneListener(R.id.meetingDate));
                datePicker.show(getSupportFragmentManager().beginTransaction(), "DatePicker");
            }
        });

        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment timePicker = new TimePickerFragment(getPickerDoneListener(R.id.startTime));
                timePicker.show(getSupportFragmentManager(), "TimePicker");
            }
        });

        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment timePicker = new TimePickerFragment(getPickerDoneListener(R.id.mtgEndTime));
                timePicker.show(getSupportFragmentManager(), "TimePicker");
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
                // Creates event under the user's profile section
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

                // THIS MIGHT SEND A MESSAGE
                //TODO check
                RemoteMessage.Builder remBuilder = new RemoteMessage.Builder(SENDER_ID + "@gcm.googleapis.com");
                remBuilder.addData("message","hello");
                remBuilder.addData("recipientId",event.getAttendee().getObjectId());
                FirebaseMessaging.getInstance().send(remBuilder.build());



//                TOPIC = "/topics/userABC"; //topic must match with what the receiver subscribed to
//                NOTIFICATION_TITLE = "From User 123";
//                NOTIFICATION_MESSAGE = "Hello! This is a test notification!";
//
//                JSONObject notification = new JSONObject();
//                JSONObject notifcationBody = new JSONObject();
//                try {
//                    notifcationBody.put("title", NOTIFICATION_TITLE);
//                    notifcationBody.put("message", NOTIFICATION_MESSAGE);
//
//                    notification.put("to", TOPIC);
//                    notification.put("data", notifcationBody);
//                } catch (JSONException e) {
//                    Log.e(TAG, "onCreate: " + e.getMessage() );
//                }
//                sendNotification(notification);
            }


        });
    }

//    private void sendNotification(JSONObject notification) {
//        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(FCM_API, notification,
//                new Response.Listener<JSONObject>() {
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        Log.i(TAG, "onResponse: " + response.toString());
//                    }
//                },
//                new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Toast.makeText(RequestMeetingActivity.this, "Request error", Toast.LENGTH_LONG).show();
//                        Log.i(TAG, "onErrorResponse: Didn't work");
//                    }
//                }){
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> params = new HashMap<>();
//                params.put("Authorization", serverKey);
//                params.put("Content-Type", contentType);
//                return params;
//            }
//        };
//        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
//    }

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