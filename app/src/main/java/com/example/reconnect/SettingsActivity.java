package com.example.reconnect;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.parse.ParseUser;

public class SettingsActivity extends AppCompatActivity {

    //Implementing Item view listeners
    private Button btnLogOut;
    private Button btnReturn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        btnLogOut = findViewById(R.id.btnLogOut);
        btnReturn = findViewById(R.id.btnReturn);

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
    }
}
