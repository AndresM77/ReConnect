package com.example.reconnect.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.reconnect.R;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class LoginActivity extends AppCompatActivity {

    //Referencing login objects
    private EditText usernameInput;
    private EditText passwordInput;
    private Button loginBtn;
    private Button registerBtn;
    ParseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // show the signup or login screen
        super.onCreate(savedInstanceState);

        initObjects();

        if (currentUser != null) {
            // do stuff with the user
            final Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        } else {

            setUpLoginPage();

            loginBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final String username = usernameInput.getText().toString();
                    final String password = passwordInput.getText().toString();

                    login(username, password);
                }
            });

            registerBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                    startActivity(i);
                }
            });
        }

    }

    public void initObjects() {
        setContentView(R.layout.activity_login);
        //Persistence functionality
        currentUser = ParseUser.getCurrentUser();
    }

    public void setUpLoginPage() {
        usernameInput = findViewById(R.id.etPhone);
        passwordInput = findViewById(R.id.tvPassText);
        loginBtn = findViewById(R.id.btnLogIn);
        registerBtn = findViewById(R.id.btnSignUp);
    }

    private void login (final String username, String password) {
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e == null)
                {
                    Log.d("LoginActivity", "Login successful");

                    final Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Log.e("LoginActivity", "Login failure");
                    e.printStackTrace();
                }
            }
        });
    }
}

