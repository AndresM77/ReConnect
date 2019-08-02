package com.example.reconnect.Adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.reconnect.Activities.RequestMeetingActivity;
import com.example.reconnect.R;
import com.example.reconnect.model.Connection;
import com.example.reconnect.model.User;
import com.parse.ParseException;
import com.parse.ParseFile;
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
        private ImageView profileBtn;

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.contactName);
            distanceAwayV = itemView.findViewById(R.id.distanceAway);
            profileBtn = itemView.findViewById(R.id.ivProfileImg);


            // onClick listener to request a meeting
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // New intent to send User to RequestMeeting Activity after selecting
                    // a contact User name
                    Connection selectedConnection = (Connection) name.getTag();
                    User.showContactOptions(view.getContext(), selectedConnection.getOtherUser());
                }
            });

        }

        /* method that connects information to create item_contact for MapFragment's Recycler View */
        public void bind(Connection connection) {
            try {
                //set tag in order to get correct Connection to display later
                name.setTag(connection);

                // set the profile image
                ParseFile profileImg = profileImg = (ParseFile) connection.getOtherUser().fetchIfNeeded().get("profileImg");
                if (profileImg != null) {
                    Glide.with(context).load(profileImg.getUrl()).circleCrop().into(profileBtn);
                }

                // set the location away of the user
                ParseGeoPoint position1 = connection.getCurrentUser().fetchIfNeeded().getParseGeoPoint("location");
                ParseGeoPoint position2 = connection.getOtherUser().getParseGeoPoint("location");
                String out = Connection.getDistanceAway(position1, position2);
                distanceAwayV.setText(out);

                // set the name of the connection
                name.setText(connection.getOtherUser().fetchIfNeeded().getUsername());

            } catch (ParseException e) {
                Log.e("Connections Adapter", "Unable to bind the required views together for each contact/connection");
                e.printStackTrace();
            }

        }

    }
}
