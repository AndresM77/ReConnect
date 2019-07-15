package com.example.reconnect;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class RegisterActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etUser;
    private EditText etPass;
    private EditText etHandle;
    private Button btnSignUp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etEmail = findViewById(R.id.tvEmail);
        etUser = findViewById(R.id.tvUserText);
        etPass = findViewById(R.id.tvPassText);
        btnSignUp = findViewById(R.id.btnCreate);
        etHandle = findViewById(R.id.tvHandle);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUser(etEmail.getText().toString(), etUser.getText().toString(), etPass.getText().toString(), etHandle.getText().toString());
            }
        });
    }

    private void saveUser(String email, String user, String pass, String handle) {
        ParseUser newUser = new ParseUser();
        newUser.put("email", email);
        newUser.put("username", user);
        newUser.put("password", pass);
        newUser.put("handle", handle);
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
