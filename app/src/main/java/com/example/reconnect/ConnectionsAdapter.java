package com.example.reconnect;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reconnect.model.Connection;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import java.util.List;

public class ConnectionsAdapter extends RecyclerView.Adapter<ConnectionsAdapter.ViewHolder> {

    private Context context;
    private List<Connection> connections;

    public ConnectionsAdapter(Context context, List<Connection> connections) {
        this.context = context;
        this.connections = connections;
    }

    @NonNull
    @Override
    public ConnectionsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConnectionsAdapter.ViewHolder holder, int position) {
        Connection connection = connections.get(position);
        holder.bind(connection);
    }

    @Override
    public int getItemCount() {
        return connections.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView name;
        private TextView distanceAwayV;

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.contactName);
            distanceAwayV = itemView.findViewById(R.id.distanceAway);


            // onClick listener to request a meeting
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // New intent to send User to RequestMeeting Activity after selecting
                    // a contact User name
                    Intent intent = new Intent(view.getContext(), RequestMeeting.class);

                    Connection selectedConnection = connections.get(view.getVerticalScrollbarPosition());

                    if (ParseUser.getCurrentUser().getUsername().equals(selectedConnection.getUser1().getUsername()))
                        intent.putExtra("requesteeId", selectedConnection.getUser2().getObjectId());
                    else {
                        intent.putExtra("requesteeId", selectedConnection.getUser1().getObjectId());
                    }

                    view.getContext().startActivity(intent);
                }
            });

        }

        /* method that connects information to create item_contact for MapFragment's Recycler View */
        public void bind(Connection connection) {
            Double latitudeC;
            Double longitudeC;
            Double latitude = ParseUser.getCurrentUser().getParseGeoPoint("location").getLatitude();
            Double longitude = ParseUser.getCurrentUser().getParseGeoPoint("location").getLongitude();
            final Double EARTH_RADIUS = 3961D;
            if (ParseUser.getCurrentUser().getUsername().equals(connection.getUser1().getUsername())) {
                try {
                    name.setText(connection.getUser2().fetchIfNeeded().getUsername());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                latitudeC = connection.getUser2().getParseGeoPoint("location").getLatitude();
                longitudeC = connection.getUser2().getParseGeoPoint("location").getLongitude();

            }
            else {
                try {
                    name.setText(connection.getUser2().fetchIfNeeded().getUsername());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                latitudeC = connection.getUser1().getParseGeoPoint("location").getLatitude();
                longitudeC = connection.getUser1().getParseGeoPoint("location").getLongitude();
            }

            //TODO fix this logic
            Double dlon = longitudeC - longitude;
            Double dlat = latitudeC - latitude;
            Double a = Math.pow((Math.sin(dlat/2)), 2) + Math.cos(latitude) * Math.cos(latitudeC) * Math.pow((Math.sin(dlon/2)), 2);
            Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
            Double d = EARTH_RADIUS * c;
            distanceAwayV.setText(d.toString() + " mi away");
        }

    }
}
