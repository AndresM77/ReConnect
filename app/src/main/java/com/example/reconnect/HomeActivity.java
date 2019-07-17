package com.example.reconnect;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.reconnect.fragments.CalendarFragment;
import com.example.reconnect.fragments.ConversationsFragment;
import com.example.reconnect.fragments.MapFragment;
import com.example.reconnect.fragments.ReconnectFragment;
import com.example.reconnect.model.Connection;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseUser;

import java.util.List;

public class HomeActivity extends AppCompatActivity {

    public ParseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //If saved instance state is null set the timeline fragment to be the initial instance
        if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, MapFragment.newInstance())
                    .commit();
        }

        final FragmentManager fragmentManager = getSupportFragmentManager();

        //check if the meetingId sent by the intent is true...if not null do things
        if (getIntent().getStringExtra("meetingId") != null) {
            fragmentManager.beginTransaction().replace(R.id.container, new CalendarFragment()).commit();
        }


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Find the toolbar view inside the activity layout
        Toolbar toolbar = findViewById(R.id.toolbar);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);
        
        // Get the current user
        currentUser = ParseUser.getCurrentUser();
        // Query connections for the current user
        queryContacts(currentUser);

        // define your fragments here
        final Fragment fragment1 = new MapFragment();
        final Fragment fragment2 = new ReconnectFragment();
        final Fragment fragment3 = new CalendarFragment();
        final Fragment fragment4 = new ConversationsFragment();

        // handle navigation selection
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Fragment fragment;
                        switch (item.getItemId()) {
                            case R.id.action_map:
                                fragment = fragment1;
                                break;
                            case R.id.action_reconnect:
                                fragment = fragment2;
                                break;
                            case R.id.action_profile:
                                fragment = fragment3;
                                break;
                            case R.id.action_messages:
                                fragment = fragment4;
                                break;
                            default:
                                fragment = fragment1;
                                break;
                        }
                        fragmentManager.beginTransaction().replace(R.id.container, fragment).commit();
                        return true;
                    }
                });
        // Set default selection
        bottomNavigationView.setSelectedItemId(R.id.action_map);
    }

    private void queryContacts(ParseUser currentUser) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.ivSettings:
                //Take to settings activity
                Intent i = new Intent(HomeActivity.this, SettingsActivity.class);
                startActivity(i); // brings up the second activity
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
