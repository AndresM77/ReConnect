package com.example.reconnect.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.reconnect.Activities.MessagesActivity;
import com.example.reconnect.Activities.RequestMeetingActivity;
import com.example.reconnect.Adapters.CustomWindowAdapter;
import com.example.reconnect.R;
import com.example.reconnect.model.Connection;
import com.example.reconnect.model.Conversation;
import com.example.reconnect.model.User;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.PermissionUtils;

import static androidx.core.content.PermissionChecker.checkSelfPermission;
import static com.example.reconnect.model.Connection.queryConnections;
import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class MapFragment extends Fragment implements GoogleMap.OnMapLongClickListener{

    //Initializing fragment tag
    public final static String TAG = "MapFragment";

    //From MapActivity
    private double RANDOMIZATION_DISTANCE = 0.005;
    private int PIC_SIZE = 150;
    //private final String TAG = "MapActivity";
    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private static LocationRequest mLocationRequest;
    Location mCurrentLocation;
    private static long UPDATE_INTERVAL = 60000;  /* 60 secs */
    private static long FASTEST_INTERVAL = 5000; /* 5 secs */
    //Initializing view variables
    private Button switchBtn;
    private boolean centered;
    private List<Connection> mConnections;
    private List<Conversation> mConversations;
    private Connection contact;
    private Context context;

    //Permission variables
    private static final int REQUEST_GETMYLOCATION = 0;

    private static final String[] PERMISSION_GETMYLOCATION = new String[] {"android.permission.ACCESS_FINE_LOCATION","android.permission.ACCESS_COARSE_LOCATION"};

    private static final int REQUEST_STARTLOCATIONUPDATES = 1;

    private static final String[] PERMISSION_STARTLOCATIONUPDATES = new String[] {"android.permission.ACCESS_FINE_LOCATION","android.permission.ACCESS_COARSE_LOCATION"};

    private final static String KEY_LOCATION = "location";

    /*
     * Define a request code to send to Google Play services This code is
     * returned in Activity.onActivityResult
     */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        centered = false;

        context = getActivity().getApplicationContext();

        //Instantiating connections list
        mConnections = new ArrayList<>();
        mConversations = new ArrayList<>();

        if (TextUtils.isEmpty(getResources().getString(R.string.google_maps_api_key))) {
            throw new IllegalStateException("You forgot to supply a Google Maps API key");
        }

        if (savedInstanceState != null && savedInstanceState.keySet().contains(KEY_LOCATION)) {
            // Since KEY_LOCATION was found in the Bundle, we can be sure that mCurrentLocation
            // is not null.
            mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            onResume();
        }

        mapFragment = new SupportMapFragment();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.map, mapFragment).commit();
        //mapFragment = (SupportMapFragment) myContext.getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap map) {
                    loadMap(map, view);
                }
            });
        } else {
            //Toast.makeText(getActivity(), "Error - Map Fragment was null!!", Toast.LENGTH_SHORT).show();
        }
    }

    protected void loadMap(final GoogleMap googleMap, final View view) {
        map = googleMap;
        if (map != null && isAdded()) {
            // Map is ready
            //Toast.makeText(getActivity(), "Map Fragment was loaded properly!", Toast.LENGTH_SHORT).show();
            getInitialPermissions();
            getUpdatePermission();
            map.setOnMapLongClickListener(this);
            map.setInfoWindowAdapter(new CustomWindowAdapter(getLayoutInflater()));
            //On click logic for markers
            map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                public boolean onMarkerClick(Marker marker) {
                    showDialogForUserSelection(marker);
                    return true;
                    // Further info found here https://guides.codepath.org/android/Google-Maps-API-v2-Usage
                }
            });
            //Load markers of peoples positions on the map
            //mQueryConnections(googleMap);
            queryConnections(new FindCallback<Connection>() {

                @Override
                public void done(List<Connection> connections, ParseException e) {
                    if (e != null) {
                        Log.e("Connection", "Error with Query");
                        e.printStackTrace();
                        return;
                    }
                    mConnections.clear();
                    mConnections.addAll(connections);
                    loadMarkers(googleMap);
                    view.findViewById(R.id.progressMap).setVisibility(View.GONE);
                }
            });
        } else {
            //Toast.makeText(getActivity(), "Error - Map was null!!", Toast.LENGTH_SHORT).show();
        }
    }

    private void getInitialPermissions() {
        //Location Permission
        if (PermissionUtils.hasSelfPermissions(getContext(), PERMISSION_GETMYLOCATION)) {
            getMyLocation();
        } else {
            requestPermissions(PERMISSION_GETMYLOCATION, REQUEST_GETMYLOCATION);
        }
    }

    private void getUpdatePermission() {
        //Location Updates Permission
        if (PermissionUtils.hasSelfPermissions(getContext(), PERMISSION_STARTLOCATIONUPDATES)) {
            startLocationUpdates();
        } else {
            requestPermissions(PERMISSION_STARTLOCATIONUPDATES, REQUEST_STARTLOCATIONUPDATES);
        }
    }

    public static void onRequestPermissionsResult(MapFragment target, int requestCode,
                                                  int[] grantResults) {
        switch (requestCode) {
            case REQUEST_GETMYLOCATION:
                if (PermissionUtils.verifyPermissions(grantResults)) {
                    target.getMyLocation();
                }
                break;
            case REQUEST_STARTLOCATIONUPDATES:
                if (PermissionUtils.verifyPermissions(grantResults)) {
                    target.startLocationUpdates();
                }
                break;
            default:
                break;
        }
    }

    private void loadMarkers(GoogleMap googleMap) {

        for (int i = 0; i < mConnections.size(); i++) {
            ParseGeoPoint geo = null;
            ParseFile profileImg = null;

            profileImg = (ParseFile) mConnections.get(i).getOtherUser().get("profileImg");
            geo = (ParseGeoPoint) mConnections.get(i).getOtherUser().get("location");

            String conName = "";

            conName = mConnections.get(i).getOtherUser().getUsername();

            LatLng pos = new LatLng(geo.getLatitude() + randomizer(), geo.getLongitude() + randomizer());
            // Define custom marker
            BitmapDescriptor customMarker = BitmapDescriptorFactory.fromResource(R.drawable.map_user_marker);
            if (profileImg != null) {
                customMarker = BitmapDescriptorFactory.fromBitmap(resizeMapIcons(profileImg, PIC_SIZE, PIC_SIZE));
            }
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(conName)
                    .icon(customMarker));
            marker.setTag(mConnections.get(i));
            //Toast.makeText(getActivity(), "Making markers", Toast.LENGTH_LONG).show();
        }
    }

    public Bitmap resizeMapIcons(ParseFile parseFile, int width, int height){
        Bitmap imageBitmap = null;
        try {
            imageBitmap = BitmapFactory.decodeFile(parseFile.getFile().getAbsolutePath());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (imageBitmap == null) {
            return null;
        }
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return getCroppedBitmap(resizedBitmap);
    }

    public Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        return output;
    }

    private double randomizer() {
        return Math.random() * (RANDOMIZATION_DISTANCE) - (RANDOMIZATION_DISTANCE/2);
    }

    @SuppressLint("NeedOnRequestPermissionsResult")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @SuppressWarnings({"MissingPermission"})
    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    public void getMyLocation() {
        map.setMyLocationEnabled(true);

        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this.getContext());
        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            onLocationChanged(location);
                            //Send location stuff
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("MapDemoActivity", "Error trying to get last GPS location");
                        e.printStackTrace();
                    }
                });
    }

    /*
     * Called when the Activity becomes visible.
     */
    @Override
    public void onStart() {
        super.onStart();
    }

    /*
     * Called when the Activity is no longer visible.
     */
    @Override
    public void onStop() {
        super.onStop();
    }

    private boolean isGooglePlayServicesAvailable() {
        // Check that Google Play services is available
        int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getContext());
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Location Updates", "Google Play services is available.");
            return true;
        } else {
            // Get the error dialog from Google Play services
            Dialog errorDialog = GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), resultCode, CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                MapFragment.ErrorDialogFragment errorFragment = new MapFragment.ErrorDialogFragment();
                errorFragment.setDialog(errorDialog);
                //errorFragment.show(getSupportFragmentManager(), "Location Updates");
            }

            return false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map != null && isAdded()) {
            // Display the connection status
            if (mCurrentLocation != null) {
                //Toast.makeText(getActivity(), "GPS location was found!", Toast.LENGTH_SHORT).show();
                LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
                map.moveCamera(cameraUpdate);
            } else {
                //Toast.makeText(getActivity(), "Current location was null, enable GPS on emulator!", Toast.LENGTH_SHORT).show();
            }
            getUpdatePermission();
        }
    }

    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    public void startLocationUpdates() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        SettingsClient settingsClient = LocationServices.getSettingsClient(getContext());
        settingsClient.checkLocationSettings(locationSettingsRequest);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //noinspection MissingPermission
            if (checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
        }
        getFusedLocationProviderClient(getContext()).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        onLocationChanged(locationResult.getLastLocation());
                    }
                },
                Looper.myLooper());
    }

    public void onLocationChanged(Location location) {
        // GPS may be turned off
        if (location == null) {
            return;
        }

        // Report to the UI that the location was updated

        mCurrentLocation = location;
        try {
            String msg = "Updated Location: " +
                    Double.toString(location.getLatitude()) + "," +
                    Double.toString(location.getLongitude());//Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }

        updateUser();

        if (!centered) {
            onResume();
            centered = true;
        }
    }

    public void updateUser() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        ParseGeoPoint pos = new ParseGeoPoint();
        pos.setLatitude(mCurrentLocation.getLatitude());
        pos.setLongitude(mCurrentLocation.getLongitude());
        currentUser.put("location", pos);
        currentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
            }
        });
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(KEY_LOCATION, mCurrentLocation);
        super.onSaveInstanceState(savedInstanceState);
    }

    // Fires when a long press happens on the map
    @Override
    public void onMapLongClick(final LatLng point) {
        //Toast.makeText(getActivity(), "Long Press", Toast.LENGTH_LONG).show();
        // Custom code here...
        showAlertDialogForPoint(point);
    }

    private void showDialogForUserSelection(final Marker userMarker) {
        View messageView = LayoutInflater.from(getContext()).inflate(R.layout.item_contact_alert, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialogBuilder.setView(messageView);

        final AlertDialog alertDialog = alertDialogBuilder.create();

        contact = (Connection) userMarker.getTag();

        //Configure Text
        TextView userName = messageView.findViewById(R.id.tvUserName);
        TextView industry = messageView.findViewById(R.id.tvIndustry);
        ImageView profileImg = messageView.findViewById(R.id.ivProfileImg);
        Button btnMessage = messageView.findViewById(R.id.btnMessage);
        Button btnMeeting = messageView.findViewById(R.id.btnMeeting);

        userName.setText(User.getFullName(contact.getOtherUser()));
        industry.setText((String) contact.getOtherUser().get("industry"));
        ParseFile img = (ParseFile) contact.getOtherUser().get("profileImg");
        if (img != null) {
            Glide.with(getContext()).load(img.getUrl()).circleCrop().into(profileImg);
        } else {
            Glide.with(getContext()).load((R.drawable.baseline_account_circle_black_48)).circleCrop().into(profileImg);
        }

        btnMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                query(contact);
            }
        });

        btnMeeting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(), RequestMeetingActivity.class);
                i.putExtra("requesteeId", contact.getOtherUser().getObjectId());
                startActivity(i);
            }
        });

        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        // Display the dialog
        alertDialog.show();
    }

    public void createConversation(final Connection connection) {
        final Conversation conversation = new Conversation();
        conversation.setConverser(ParseUser.getCurrentUser());
        conversation.setConversee(connection.getOtherUser());
        conversation.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e!=null) {
                    Log.d(TAG, "Error while saving");
                    e.printStackTrace();
                    return;
                }
                Log.d(TAG, "Success");
                Intent i = new Intent(getContext(), MessagesActivity.class);
                i.putExtra("conversation", conversation);
                startActivity(i);
            }
        });
    }

    private void goToConversation(Conversation conversation) {
        Intent i = new Intent(context, MessagesActivity.class);
        i.putExtra("conversation", conversation);
        startActivity(i);
    }

    private void query(final Connection contact) {
        Conversation.findConversation(contact.getUser1(), contact.getUser2(), new FindCallback<Conversation>() {
            @Override
            public void done(List<Conversation> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error with Query");
                    e.printStackTrace();
                    return;
                }
                if (objects.size() == 0) {
                    createConversation(contact);
                } else {
                    goToConversation(objects.get(0));
                }
            }
        });
    }

    // Display the alert that adds the marker
    private void showAlertDialogForPoint(final LatLng point) {
        // inflate message_item.xml view
        View messageView = LayoutInflater.from(getContext()).
                inflate(R.layout.item_alert_message, null);
        // Create alert dialog builder
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        // set message_item.xml to AlertDialog builder
        alertDialogBuilder.setView(messageView);

        // Create alert dialog
        final AlertDialog alertDialog = alertDialogBuilder.create();

        // Configure dialog button (OK)
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Define custom marker
                        BitmapDescriptor customMarker =
                                BitmapDescriptorFactory.fromResource(R.drawable.map_user_marker);

                        // Extract content from alert dialog
                        String title = ((EditText) alertDialog.findViewById(R.id.etTitle)).
                                getText().toString();
                        String snippet = ((EditText) alertDialog.findViewById(R.id.etSnippet)).
                                getText().toString();
                        // Creates and adds marker to the map
                        Marker marker = map.addMarker(new MarkerOptions()
                                .position(point)
                                .title(title)
                                .snippet(snippet)
                                .icon(customMarker));
                        // Animate marker using drop effect
                        // --> Call the dropPinEffect method here
                        dropPinEffect(marker);
                    }
                });

        // Configure dialog button (Cancel)
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) { dialog.cancel(); }
                });

        // Display the dialog
        alertDialog.show();
    }

    // Falling pin animation
    private void dropPinEffect(final Marker marker) {
        // Handler allows us to repeat a code block after a specified delay
        final android.os.Handler handler = new android.os.Handler();
        final long start = SystemClock.uptimeMillis();
        final long duration = 1500;

        // Use the bounce interpolator
        final android.view.animation.Interpolator interpolator =
                new BounceInterpolator();

        // Animate marker with a bounce updating its position every 15ms
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                // Calculate t for bounce based on elapsed time
                float t = Math.max(
                        1 - interpolator.getInterpolation((float) elapsed
                                / duration), 0);
                // Set the anchor
                marker.setAnchor(0.5f, 1.0f + 14 * t);

                if (t > 0.0) {
                    // Post this event again 15ms from now.b
                    handler.postDelayed(this, 15);
                } else { // done elapsing, show window
                    marker.showInfoWindow();
                }
            }
        });
    }


    // Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends androidx.fragment.app.DialogFragment {

        // Global field to contain the error dialog
        private Dialog mDialog;

        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

}
