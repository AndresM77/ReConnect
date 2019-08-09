package com.example.reconnect.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.reconnect.R;
import com.example.reconnect.fragments.CalendarFragment;
import com.example.reconnect.fragments.ConversationsFragment;
import com.example.reconnect.fragments.MapFragment;
import com.example.reconnect.fragments.ReconnectFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.parse.ParseUser;


public class HomeActivity extends AppCompatActivity {

    private static final int MENU_ITEM_ITEM1 = 2;
    //Initializing fragment tag
    public final static String TAG = "HomeActivity";
    public ParseUser currentUser;
    public Fragment fragment;
    // define your fragments here
    public Fragment fragment1;
    public Fragment fragment2;
    public Fragment fragment3;
    public Fragment fragment4;
    public FragmentManager fragmentManager;
    public Menu actionMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initObjects();
        initFragmentManager(savedInstanceState);
        initToolbar();
        grabTokenId();
    }

    private void grabTokenId() {
        // get the token id for the current user
        //TODO check
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("FCM token", "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        ParseUser.getCurrentUser().put("deviceId", token);
                        ParseUser.getCurrentUser().saveInBackground();

                        // Log and toast
                        Log.d("FCM token", token);
                    }
                });
    }

    private void initObjects() {
        setContentView(R.layout.activity_home);
        currentUser = ParseUser.getCurrentUser();
    }

    private void initToolbar() {
        // Find the toolbar view inside the activity layout
        Toolbar toolbar = findViewById(R.id.toolbar);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);
    }

    private void initFragmentManager(Bundle savedInstanceState) {
        //If saved instance state is null set the timeline fragment to be the initial instance
        if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, MapFragment.newInstance())
                    .commit();
        }

        fragmentManager = getSupportFragmentManager();

        //check if the meetingId sent by the intent is true...if not null do things
        if (getIntent().hasExtra("sendToCalendar")) {
            fragmentManager.beginTransaction().replace(R.id.container, new CalendarFragment()).commit();
        }
    }

    private void setBottomNavView() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // define your fragments here
        fragment1 = new MapFragment();
        fragment2 = new ReconnectFragment();
        fragment3 = new CalendarFragment();
        fragment4 = new ConversationsFragment();

        // handle navigation selection
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_map:
                                fragment = fragment1;
                                //TODO - Make function names better
                                mapFragmentInteraction(actionMenu);
                                break;
                            case R.id.action_profile:
                                fragment = fragment3;
                                calFragmentInteraction(actionMenu);
                                break;
                            case R.id.action_messages:
                                fragment = fragment4;
                                mesFragmentInteraction(actionMenu);
                                break;
                            default:
                                fragment = fragment1;
                                mapFragmentInteraction(actionMenu);
                                break;
                        }
                        fragmentManager.beginTransaction().replace(R.id.container, fragment).commit();
                        return true;
                    }
                });
        if (!getIntent().hasExtra("sendToCalendar")) {
            bottomNavigationView.setSelectedItemId(R.id.action_map);
        }
    }

    private void recFragmentInteraction(Menu menu) {
        menu.findItem(R.id.ivSettings).setVisible(true);
        menu.findItem(R.id.addToCalendar).setVisible(false);
        menu.findItem(R.id.newConversation).setVisible(false);
        menu.findItem(R.id.ivSwitchButton).setVisible(true);
    }

    private void mesFragmentInteraction(Menu menu) {
        menu.findItem(R.id.ivSettings).setVisible(true);
        menu.findItem(R.id.addToCalendar).setVisible(false);
        menu.findItem(R.id.newConversation).setVisible(true);
        menu.findItem(R.id.ivSwitchButton).setVisible(false);
    }

    private void mapFragmentInteraction(Menu menu) {
        menu.findItem(R.id.ivSettings).setVisible(true);
        menu.findItem(R.id.addToCalendar).setVisible(false);
        menu.findItem(R.id.newConversation).setVisible(false);
        menu.findItem(R.id.ivSwitchButton).setVisible(true);
        findViewById(R.id.ivSwitchButton).setSelected(false);
    }

    public void calFragmentInteraction(Menu menu) {
        menu.findItem(R.id.ivSettings).setVisible(true);
        menu.findItem(R.id.addToCalendar).setVisible(true);
        menu.findItem(R.id.newConversation).setVisible(false);
        menu.findItem(R.id.ivSwitchButton).setVisible(false);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        actionMenu = menu;
        // Called after options menu to ensure menu is populated
        setBottomNavView();

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
            case R.id.addToCalendar:
                Intent i2 = new Intent(this, RequestMeetingActivity.class);
                i2.putExtra("requesteeId", ParseUser.getCurrentUser().getObjectId());
                startActivity(i2);
                return true;
            case R.id.newConversation:
                selectRecipient();
                return true;
            case R.id.ivSwitchButton:
                if (!findViewById(R.id.ivSwitchButton).isSelected()) {
                    fragmentManager.beginTransaction().replace(R.id.container, fragment2).commit();
                    findViewById(R.id.ivSwitchButton).setSelected(true);
                } else {
                    fragmentManager.beginTransaction().replace(R.id.container, fragment1).commit();
                    findViewById(R.id.ivSwitchButton).setSelected(false);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void selectRecipient(){
        Intent i = new Intent(getApplicationContext(), MessageContactsActivity.class);
        startActivity(i);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ParseUser.getCurrentUser().logOut();
        finish();
    }
}
