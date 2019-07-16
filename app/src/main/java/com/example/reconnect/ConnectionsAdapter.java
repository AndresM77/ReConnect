package com.example.reconnect;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reconnect.model.Connection;
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

    private TextView name;
    private TextView latitude;
    private TextView longitude;

    @Override
    public int getItemCount() {
        return connections.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.contactName);
            latitude = itemView.findViewById(R.id.contactLatitude);
            longitude = itemView.findViewById(R.id.contactLongitude);

            // onClick listener to request a meeting
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // New intent to send User to RequestMeeting Activity after selecting
                    // a contact User name
                    Intent intent = new Intent(view.getContext(), RequestMeeting.class);
                    intent.putExtra("currUserId", ParseUser.getCurrentUser().getObjectId());
                    intent.putExtra("requestedUserId", name.getText());

                    view.getContext().startActivity(intent);
                }
            });
        }

        // method that connects information to create item_contact for MapFragment's Recycler View
        public void bind(Connection connection) {
            if (ParseUser.getCurrentUser().equals(connection.getUser1())) {
                name.setText(connection.getUser1().getUsername());
                latitude.setText((int) connection.getUser1().getParseGeoPoint("location").getLatitude());
                longitude.setText((int) connection.getUser1().getParseGeoPoint("location").getLongitude());
            }
            else {
                name.setText(connection.getUser2().getUsername());
                latitude.setText((int) connection.getUser2().getParseGeoPoint("location").getLatitude());
                longitude.setText((int) connection.getUser2().getParseGeoPoint("location").getLongitude());
            }
        }

    }
}
