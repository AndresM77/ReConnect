package com.example.reconnect.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.reconnect.Adapters.MessagesAdapter;
import com.example.reconnect.R;
import com.example.reconnect.model.Conversation;
import com.example.reconnect.model.Message;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class MessagesActivity extends AppCompatActivity {

    //Initializing fragment tag
    public final static String TAG = "MessagesActivity";
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
    private Button btnSubmit;
    private Button btnReturn;
    private Conversation conversation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        conversation = getIntent().getParcelableExtra("conversation");

        //Setup view objects
        rvMessages = findViewById(R.id.rvMessages);
        //Instantiating connections list
        mMessage = new ArrayList<>();
        //Set up adapter
        adapter = new MessagesAdapter(this, mMessage);
        //Set adapter on recycler view
        rvMessages.setAdapter(adapter);
        //Set up linear layout manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        //Set layout manager on recycler view
        rvMessages.setLayoutManager(linearLayoutManager);
        tvContactName = findViewById(R.id.tvContactName);
        etMessage = findViewById(R.id.etMessage);
        btnSubmit = findViewById(R.id.btnMessage);
        btnReturn = findViewById(R.id.btnReturn);
        tvDistanceAway = findViewById(R.id.tvDistanceAway);
        tvIndustry = findViewById(R.id.tvIndustry);
        ivProfileImage = findViewById(R.id.ivProfileImg);

        ParseFile profileImg = null;

        try {
            tvContactName.setText(conversation.getConversee().fetchIfNeeded().getUsername());
            tvIndustry.setText((String) conversation.getConversee().fetchIfNeeded().get("industry"));
            profileImg = (ParseFile) conversation.getOtherUser().fetchIfNeeded().get("profileImg");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (profileImg != null) {
            Glide.with(getBaseContext()).load(profileImg.getUrl()).circleCrop().into(ivProfileImage);
        }

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etMessage.setText("");
                saveMessage();
            }
        });

        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MessagesActivity.this, HomeActivity.class);
                startActivity(i);
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
    }

    private void saveMessage() {
        final Message message = new Message();
        message.setConversation(conversation);
        message.setMessage(etMessage.getText().toString());
        message.setRecipient(conversation.getConversee());
        message.setSender(ParseUser.getCurrentUser());
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
                queryMessages(conversation);
            }
        });
    }

    public void queryMessages(Conversation conversation) {
        ParseQuery<Message> postQuery = new ParseQuery<Message>(Message.class);
        postQuery.include(Message.KEY_SENDER);
        postQuery.setLimit(20);
        postQuery.whereEqualTo(Message.KEY_SENDER, ParseUser.getCurrentUser());
        postQuery.whereEqualTo(Message.KEY_RECIPIENT, conversation.getConversee());
        postQuery.addDescendingOrder(Message.KEY_CREATED_AT);

        postQuery.findInBackground(new FindCallback<Message>() {
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
                for (int i = 0; i < mMessage.size(); i++) {
                    Message message = mMessage.get(i);
                    Log.d(TAG, "Sender: " + message.getSender().getUsername());
                }
            }
        });
    }
}
