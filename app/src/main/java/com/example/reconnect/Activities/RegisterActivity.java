package com.example.reconnect.Activities;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.reconnect.R;
import com.example.reconnect.model.Connection;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class RegisterActivity extends AppCompatActivity {

    //Initializing fragment tag
    public final static String TAG = "RegisterActivity";
    //Initializing View objects
    private EditText etPhone;
    private EditText etUser;
    private EditText etPass;
    private EditText etFirstName;
    private EditText etLastName;
    private EditText etIndustry;
    private Button btnSignUp;
    private List<ParseUser> mUsers;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        createRegisterDisplay();

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etPhone.getText().length() > 10) {
                    Toast.makeText(RegisterActivity.this, "Phone Number is Invalid", Toast.LENGTH_LONG).show();
                } else {
                    queryUsers();
                }
            }
        });
    }

    public void createRegisterDisplay() {
        mUsers = new ArrayList<>();

        etPhone = findViewById(R.id.etPhone);
        etUser = findViewById(R.id.etUser);
        etPass = findViewById(R.id.etPass);
        etFirstName = findViewById(R.id.et_firstName);
        etLastName = findViewById(R.id.et_lastName);
        etIndustry = findViewById(R.id.et_industry);
        btnSignUp = findViewById(R.id.btnCreate);
    }

    public void queryUsers() {
        ParseQuery<ParseUser> postQuery = new ParseQuery<>(ParseUser.class);
        postQuery.whereEqualTo("username",etUser.getText().toString());
        postQuery.whereEqualTo("firstName", etFirstName.getText().toString());
        postQuery.whereEqualTo("lastName", etLastName.getText().toString());
        postQuery.whereEqualTo("industry", etIndustry.getText().toString());

        postQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> users, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error with Query");
                    e.printStackTrace();
                    return;
                }
                mUsers.clear();
                mUsers.addAll(users);
                if (mUsers.size() > 0) {
                    Toast.makeText(getApplicationContext(), "UserName exists", Toast.LENGTH_LONG).show();
                } else {
                    saveUser(etPhone.getText().toString(), etUser.getText().toString(), etPass.getText().toString(), etFirstName.getText().toString(), etLastName.getText().toString(), etIndustry.getText().toString());
                }
            }
        });
    }

    private void saveUser(String phoneNumber, String user, String pass, String firstName, String lastName, String industry) {
        ParseUser newUser = new ParseUser();
        newUser.put("phoneNumber", phoneNumber);
        newUser.put("username", user);
        newUser.put("firstName", firstName);
        newUser.put("lastName", lastName);
        newUser.put("password", pass);
        newUser.put("industry", industry);
        newUser.put("location", new ParseGeoPoint(0,0));
        try {
            newUser.signUp();
        } catch (ParseException e) {
            Log.e("Register Activity", "Unable to sign up user.");
            e.printStackTrace();
        }
        newUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e!=null) {
                    Log.d("RegisterActivity", "Error while saving");
                    e.printStackTrace();
                    return;
                }
                Log.d("RegisterActivity", "Success");
                Intent i = new Intent(RegisterActivity.this, HomeActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
