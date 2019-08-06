package com.example.reconnect.model;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.example.reconnect.Activities.HomeActivity;
import com.example.reconnect.Activities.MessagesActivity;
import com.example.reconnect.Activities.RequestMeetingActivity;
import com.example.reconnect.R;
import com.example.reconnect.fragments.ConversationsFragment;
import com.parse.FindCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

@ParseClassName("User")
public class User extends ParseUser {
    public final static String KEY_USER = "username";

    public static String getFullName(ParseUser user) {
        String first = user.get("firstName").toString();
        String last = user.get("lastName").toString();

        return first + " " + last;
    }

    public static void showContactOptions(final Context context, final ParseUser contact) {
        View messageView = LayoutInflater.from(context).inflate(R.layout.item_contact_alert, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(messageView);

        final AlertDialog alertDialog = alertDialogBuilder.create();

        //Configure Text
        TextView userName = messageView.findViewById(R.id.tvUserName);
        TextView industry = messageView.findViewById(R.id.tvIndustry);
        ImageView profileImg = messageView.findViewById(R.id.ivProfileImg);
        Button btnMessage = messageView.findViewById(R.id.btnMessage);
        Button btnMeeting = messageView.findViewById(R.id.btnMeeting);
        try {
            userName.setText(User.getFullName(contact));
            industry.setText((String) contact.fetchIfNeeded().get("industry"));
            if (contact.get("profileImg") != null) {
                ParseFile img = (ParseFile) contact.get("profileImg");
                Glide.with(context).load(img.getUrl()).circleCrop().into(profileImg);
            } else {
                Glide.with(context).load((R.drawable.baseline_account_circle_black_48)).circleCrop().into(profileImg);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        btnMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, MessagesActivity.class);
                i.putExtra("contact", contact);
                context.startActivity(i);
            }
        });

        btnMeeting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, RequestMeetingActivity.class);
                i.putExtra("requesteeId", contact.getObjectId());
                context.startActivity(i);
            }
        });

        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        // Display the dialog
        alertDialog.show();
    }

    //Querying our User class
    public static class Query extends ParseQuery<User> {

        public Query() {
            super(User.class);
        }

        public User.Query getTop() {
            setLimit(20);
            return this;
        }

        public User.Query withUser() {
            include("username");
            return this;
        }
    }
}
