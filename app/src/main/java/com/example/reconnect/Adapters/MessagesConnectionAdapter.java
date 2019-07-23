package com.example.reconnect.Adapters;

import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.reconnect.Activities.MessageContactsActivity;
import com.example.reconnect.R;
import com.example.reconnect.model.Connection;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import java.util.List;

public class MessagesConnectionAdapter extends RecyclerView.Adapter<MessagesConnectionAdapter.ViewHolder> {

    private Context context;
    private List<Connection> connections;
    private MessageContactsActivity.ContactClickListener listener;

    //pass down listener
    public MessagesConnectionAdapter(Context context, List<Connection> connections, MessageContactsActivity.ContactClickListener listener) {
        this.context = context;
        this.connections = connections;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MessagesConnectionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessagesConnectionAdapter.ViewHolder holder, int position) {
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

        public ViewHolder(final View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.contactName);
            distanceAwayV = itemView.findViewById(R.id.distanceAway);
            profileBtn = itemView.findViewById(R.id.ivProfileImg);
            // onClick listener to request a meeting
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onClick((Connection) name.getTag());
                }
            });

        }

        /* method that connects information to create item_contact for MapFragment's Recycler View */
        public void bind(Connection connection) {
            ParseFile profileImg = null;
            try {
                profileImg = (ParseFile) connection.getOtherUser().fetchIfNeeded().get("profileImg");
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (profileImg != null) {
                Glide.with(context).load(profileImg.getUrl()).into(profileBtn);
            }

            ParseUser contact = connection.getOtherUser();
            ParseGeoPoint position1 = ParseUser.getCurrentUser().getParseGeoPoint("location");

            try {
                name.setText(contact.fetchIfNeeded().getUsername());
            } catch (ParseException e) {
                e.printStackTrace();
            }

            ParseGeoPoint position2 = contact.getParseGeoPoint("location");

            Location loc = new Location("");

            loc.setLatitude(position1.getLatitude());

            loc.setLongitude(position1.getLongitude());

            Location loc2 = new Location("");

            loc2.setLatitude(position2.getLatitude());

            loc2.setLongitude(position2.getLongitude());

            Double distance = new Float(loc.distanceTo(loc2)).doubleValue();

            Double metersToMiles = 1609.344;

            distance /= metersToMiles;

            Math.round(distance);

            String out = distance + " miles away";

            distanceAwayV.setText(out);
        }

    }
}
