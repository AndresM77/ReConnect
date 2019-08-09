package com.example.reconnect.Activities;

import android.annotation.SuppressLint;
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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.reconnect.Adapters.MessagesAdapter;
import com.example.reconnect.NotificationHandler;
import com.example.reconnect.R;
import com.example.reconnect.VerticalSpaceItemDecoration;
import com.example.reconnect.model.Connection;
import com.example.reconnect.model.Conversation;
import com.example.reconnect.model.Message;
import com.example.reconnect.model.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseLiveQueryClient;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SubscriptionHandling;

import java.util.ArrayList;
import java.util.List;

public class MessagesActivity extends AppCompatActivity {

    //Initializing fragment tag
    public final static String TAG = "MessagesActivity";
    public final static int SPACING = 20;
    private static final String MESSAGE_1 = "Can't wait to meet you!";
    private static final String MESSAGE_2 = "Looking forward to Relinking!";
    public FirebaseFunctions mFunctions;
    //Initializing variables necessary for recycler view
    private RecyclerView rvMessages;
    private MessagesAdapter adapter;
    private List<Message> mMessage;
    private SwipeRefreshLayout swipeContainer;
    private TextView tvContactName;
    private TextView tvIndustry;
    private TextView tvDistanceAway;
    private ImageView ivProfileImage;
    private EditText etMessage;
    private ImageView btnSubmit;
    private Conversation conversation;
    private Button btnPopulate;
    private Button btnPopulate2;
    LinearLayoutManager linearLayoutManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        initViewComp();


    }

    private void initViewComp() {
        if (getIntent().getParcelableExtra("contact") != null) {
            Conversation.findConversation(ParseUser.getCurrentUser(), (ParseUser) getIntent().getParcelableExtra("contact"), new FindCallback<Conversation>() {
                @Override
                public void done(List<Conversation> objects, ParseException e) {
                    if (e != null) {
                        Log.e("Messages Activity", "There was a problem finding the conversation to open");
                        e.printStackTrace();
                        return;
                    }
                    if (objects.size() > 0) {
                        conversation = objects.get(0);
                        displayConversation();

                    }
                    else {
                        conversation = new Conversation();
                        conversation.put("converser", ParseUser.getCurrentUser());
                        conversation.put("conversee", getIntent().getParcelableExtra("contact"));
                        conversation.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                displayConversation();
                            }
                        });
                    }

                }
            });
        }

    }

    @SuppressLint("NewApi")
    private void displayConversation() {
        //Setup view objects
        rvMessages = findViewById(R.id.rvMessages);
        //Instantiating connections list
        mMessage = new ArrayList<>();
        //Set up adapter
        adapter = new MessagesAdapter(this, mMessage);
        //Set adapter on recycler view
        rvMessages.setAdapter(adapter);
        //Set up linear layout manager
        linearLayoutManager = new LinearLayoutManager(this);
        //Set layout manager on recycler view
        rvMessages.setLayoutManager(linearLayoutManager);
        tvContactName = findViewById(R.id.tvContactName);
        etMessage = findViewById(R.id.etMessage);
        btnSubmit = findViewById(R.id.btnSendMessage);
        tvDistanceAway = findViewById(R.id.tvDistanceAway);
        tvIndustry = findViewById(R.id.tvIndustry);
        ivProfileImage = findViewById(R.id.ivProfileImg);
        btnPopulate = findViewById(R.id.btnPopulate);
        btnPopulate2 = findViewById(R.id.btnPopulate2);

        ParseFile profileImg = null;

        tvContactName.setText(User.getFullName(conversation.getOtherUser()));
        tvIndustry.setText((String) conversation.getOtherUser().get("industry"));
        tvDistanceAway.setText(Connection.getDistanceAway(conversation.getOtherUser().getParseGeoPoint("location"), ParseUser.getCurrentUser().getParseGeoPoint("location")));
        profileImg = (ParseFile) conversation.getOtherUser().get("profileImg");


        if (profileImg != null) {
            Glide.with(getBaseContext()).load(profileImg.getUrl()).circleCrop().into(ivProfileImage);
        }

        btnPopulate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etMessage.setText(MESSAGE_1);
            }
        });

        btnPopulate2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etMessage.setText(MESSAGE_2);
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etMessage.getText().length() < 1) {
                    Toast.makeText(getApplicationContext(),"Invalid Message", Toast.LENGTH_LONG).show();
                } else {
                    saveMessage();

                    // send notification
                    NotificationHandler nHandler = new NotificationHandler(FirebaseFunctions.getInstance());
                    Task<String> result = nHandler.sendNotifications(conversation.getOtherUser().get("deviceId").toString(),
                            User.getFullName(conversation.getOtherUser()) + " sent you a message");
                }
            }
        });

        // onClick listener to request a meeting
        ivProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // New intent to send User to RequestMeeting Activity after selecting
                // a contact User name
                Intent intent = new Intent(view.getContext(), RequestMeetingActivity.class);

                intent.putExtra("requesteeId", conversation.getOtherUser().getObjectId());

                view.getContext().startActivity(intent);
            }
        });
        // Find the toolbar view inside the activity layout
        Toolbar toolbar = findViewById(R.id.messageToolbar);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        etMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                linearLayoutManager.scrollToPosition(mMessage.size() - 1);
            }
        });

        // Lookup the swipe container view
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                swipeContainer.setRefreshing(false);
                queryMessages(conversation);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        //query posts
        queryMessages(conversation);

        ParseLiveQueryClient parseLiveQueryClient = ParseLiveQueryClient.Factory.getClient();

        ParseQuery<Message> parseQuery = ParseQuery.getQuery(Message.class);

        SubscriptionHandling<Message> subscriptionHandling = parseLiveQueryClient.subscribe(parseQuery);

        subscriptionHandling.handleEvents(new SubscriptionHandling.HandleEventsCallback<Message>() {
            @Override
            public void onEvents(ParseQuery<Message> query, SubscriptionHandling.Event event, Message object) {
                queryMessages(conversation);
            }
        });

        VerticalSpaceItemDecoration dividerItemDecoration = new VerticalSpaceItemDecoration(SPACING);
        rvMessages.addItemDecoration(dividerItemDecoration);
    }

    private void saveMessage() {
        final Message message = new Message();
        message.setConversation(conversation);
        message.setMessage(etMessage.getText().toString());
        message.setRecipient(conversation.getOtherUser());
        message.setSender(ParseUser.getCurrentUser());
        message.setIsRequest(false);
        message.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e!=null) {
                    Log.d(TAG, "Error while saving");
                    e.printStackTrace();
                    return;
                }
                Log.d(TAG, "Success");
                conversation.setLastMessage(message);
                conversation.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e!=null) {
                            Log.d(TAG, "Error while saving");
                            e.printStackTrace();
                            return;
                        }
                        Log.d(TAG, "Success");
                    }
                });
                etMessage.setText("");
                queryMessages(conversation);
            }
        });
    }

    // for toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_message, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void queryMessages(Conversation conversation) {
        ParseQuery<Message> query1 = new ParseQuery<Message>(Message.class);
        query1.whereEqualTo(Message.KEY_SENDER, ParseUser.getCurrentUser());
        query1.whereEqualTo(Message.KEY_RECIPIENT, conversation.getOtherUser());

        ParseQuery<Message> query2 = new ParseQuery<Message>(Message.class);
        query2.whereEqualTo(Message.KEY_SENDER, conversation.getOtherUser());
        query2.whereEqualTo(Message.KEY_RECIPIENT, ParseUser.getCurrentUser());

        List<ParseQuery<Message>> queries = new ArrayList<>();
        queries.add(query1);
        queries.add(query2);

        ParseQuery<Message> mainQuery = ParseQuery.or(queries);
        mainQuery.include("sender");
        mainQuery.include("recipient");
        mainQuery.include("conversation");
        mainQuery.include("message");
        mainQuery.addAscendingOrder(Message.KEY_CREATED_AT);
        mainQuery.setLimit(20);



        mainQuery.findInBackground(new FindCallback<Message>() {
            @Override
            public void done(List<Message> messages, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error with Query");
                    e.printStackTrace();
                    return;
                }
                mMessage.clear();
                mMessage.addAll(messages);
                adapter.notifyDataSetChanged();
                linearLayoutManager.scrollToPosition(mMessage.size() - 1);
                findViewById(R.id.progressMessages).setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
