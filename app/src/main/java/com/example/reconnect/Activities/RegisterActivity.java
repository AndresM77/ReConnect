package com.example.reconnect.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.reconnect.R;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

public class RegisterActivity extends AppCompatActivity {

    //Initializing fragment tag
    public final static String TAG = "RegisterActivity";
    //Initializing View objects
    private EditText etEmail;
    private EditText etUser;
    private EditText etPass;
    private Button btnSignUp;
    private List<ParseUser> mUsers;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etEmail = findViewById(R.id.tvEmail);
        etUser = findViewById(R.id.tvUserText);
        etPass = findViewById(R.id.tvPassText);
        btnSignUp = findViewById(R.id.btnCreate);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                queryUsers();
            }
        });
    }

    public void queryUsers() {
        ParseQuery<ParseUser> postQuery = new ParseQuery<>(ParseUser.class);
        postQuery.whereEqualTo("username",etUser.getText().toString());

        postQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> users, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error with Query");
                    e.printStackTrace();
                    return;
                }
                mUsers.clear();
                mUsers = users;
                if (mUsers.size() > 0) {
                    Toast.makeText(getApplicationContext(), "UserName exists", Toast.LENGTH_LONG).show();
                } else {
                    saveUser(etEmail.getText().toString(), etUser.getText().toString(), etPass.getText().toString());
                }
            }
        });
    }

    private void saveUser(String email, String user, String pass) {
        ParseUser newUser = new ParseUser();
        newUser.put("email", email);
        newUser.put("username", user);
        newUser.put("password", pass);
        try {
            newUser.signUp();
        } catch (ParseException e) {
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
                Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(i);
            }
        });
    }
}
