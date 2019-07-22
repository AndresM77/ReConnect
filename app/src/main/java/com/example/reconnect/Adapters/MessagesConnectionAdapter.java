package com.example.reconnect.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reconnect.Activities.MessageContactsActivity;
import com.example.reconnect.R;
import com.example.reconnect.model.Connection;
import com.parse.ParseException;

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


        public ViewHolder(final View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.contactName);
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
            try {
                name.setText(connection.getOtherUser().fetchIfNeeded().getUsername());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            name.setTag(connection);
        }

    }
}
