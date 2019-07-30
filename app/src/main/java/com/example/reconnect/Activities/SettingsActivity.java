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

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";
    private static final int REQUEST_CODE = 20;
    //Implementing Item view listeners
    private Button btnLogOut;
    private Button btnReturn;
    private Button btnUpload;
    private Button btnGetContacts;
    private List<ParseUser> mUsers;
    private List<Connection> mConnections;
    private List<String> cUserNames;
    private List<Integer> mPhones;

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
                //query users
                queryUsers();
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
                        int areaCodeFactor = 10000000;
                        int secondCodeFactor = 10000;
                        int placeholder = Integer.parseInt(phoneNo.substring(0,2)) * areaCodeFactor;
                        placeholder += Integer.parseInt(phoneNo.substring(3,6)) * secondCodeFactor;
                        placeholder += Integer.parseInt(phoneNo.substring(7));
                        Log.i(TAG, "Phone Number in int: " + placeholder);
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
            }
        });
    }

    private void queryUsers() {
        ParseQuery <ParseUser> query1 = new ParseQuery<ParseUser>(ParseUser.class);
        query1.whereEqualTo("username", ParseUser.getCurrentUser());
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
                //for (int i = 0; i < objects.size(); i++) {
//                    for (int k = 0; k < mConnections.size(); k++) {
//                        try {
//                            if (mConnections.get(k).getOtherUser().fetchIfNeeded().getUsername().equals(objects.get(i).fetchIfNeeded().getUsername())) {
//                               if (!mUsers.contains(objects.get(i))) { mUsers.add(objects.get(i)); }
//                            }
//                        } catch (ParseException ee) {
//                            ee.printStackTrace();
//                        }
//                    }
//                }
                mUsers.addAll(objects);
            }
        });
    }

    private void checkForConnections() {
        for (int i = 0; i < mUsers.size(); i++) {

        }
    }

}
