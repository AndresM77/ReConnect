package com.example.reconnect.Activities;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.reconnect.R;
import com.example.reconnect.model.Connection;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";
    //Implementing Item view listeners
    private Button btnLogOut;
    private Button btnReturn;
    private Button btnUpload;
    private Button btnGetContacts;
    private List<ParseUser> mUsers;
    private List<Connection> mConnections;
    private List<String> cUserNames;
    private List<String> mPhones;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        btnLogOut = findViewById(R.id.btnLogOut);
        btnReturn = findViewById(R.id.btnReturn);
        btnUpload = findViewById(R.id.btnUpload);
        btnGetContacts = findViewById(R.id.btnGetContacts);
        mUsers = new ArrayList<>();
        mPhones = new ArrayList<>();
        mConnections = new ArrayList<>();
        cUserNames = new ArrayList<>();

        btnGetContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(SettingsActivity.this, AllUsersActivity.class);
                startActivity(i);
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Unleash the best and get the contacts
                getContactList();
                Toast.makeText(getApplicationContext(), "Check logs", Toast.LENGTH_LONG).show();
            }
        });

        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseUser.logOut();
                Intent i = new Intent(SettingsActivity.this, LoginActivity.class);
                startActivity(i);
            }
        });

        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(SettingsActivity.this, HomeActivity.class);
                startActivity(i);
            }
        });


        //query connections
        queryConnections();
    }

    private void getContactList() {
        mPhones.clear();
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));

                if (cur.getInt(cur.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        Log.i(TAG, "Name: " + name);
                        Log.i(TAG, "Phone Number: " + phoneNo);
                        String placeholder = phoneNo.substring(0,3) + phoneNo.substring(4,7) + phoneNo.substring(8);
                        Log.i(TAG, "Phone Number without dashes: " + placeholder);
                        mPhones.add(placeholder);
                    }
                    pCur.close();
                }
            }
        }
        if(cur!=null){
            cur.close();
        }
        checkForConnections();
    }

    private void queryConnections() {
        Connection.queryConnections(new FindCallback<Connection>() {
            @Override
            public void done(List<Connection> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error with Query");
                    e.printStackTrace();
                    return;
                }
                mConnections.clear();
                mConnections.addAll(objects);
                cUserNames.clear();
                for (int i = 0; i < objects.size(); i++) {
                    try {
                        cUserNames.add(objects.get(i).getOtherUser().fetchIfNeeded().getUsername());
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }
                }
                queryUsers();
            }
        });
    }

    private void queryUsers() {
        //Not the current user
        ParseQuery <ParseUser> query1 = new ParseQuery<ParseUser>(ParseUser.class);
        query1.whereNotEqualTo("username", ParseUser.getCurrentUser());
        //Not a connection
        ParseQuery <ParseUser> query2 = new ParseQuery<ParseUser>(ParseUser.class);
        query2.whereNotContainedIn("username", cUserNames);

        List<ParseQuery<ParseUser>> queries = new ArrayList<>();
        queries.add(query1);
        queries.add(query2);

        ParseQuery<ParseUser> mainQuery = ParseQuery.or(queries);
        mainQuery.addDescendingOrder("createdAt");
        mainQuery.setLimit(20);

        mainQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                mUsers.clear();
                mUsers.addAll(objects);
            }
        });
    }

    private void checkForConnections() {
        for (int i = 0; i < mUsers.size(); i++) {
            try {
                if (mPhones.contains(mUsers.get(i).fetchIfNeeded().get("phoneNumber"))) {
                    addConnection(mUsers.get(i));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private void addConnection(final ParseUser user) {
        Connection connection = new Connection();
        connection.put("user1", ParseUser.getCurrentUser());
        connection.put("user2", user);

        connection.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.d(TAG, "Error while saving");
                    e.printStackTrace();
                    return;
                }
                try {
                    Log.d(TAG, "Success, Added user:" + user.fetchIfNeeded().getUsername());
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

}
