package com.example.reconnect;

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MapAdapter extends RecyclerView.Adapter<MapAdapter.ViewHolder> {

    private Context context;
    private List<Contact> contacts;

    public MapAdapter(Context context, List<Contact> contacts) {
        this.context = context;
        this.contacts = contacts;
    }

    @NonNull
    @Override
    public MapAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MapAdapter.ViewHolder holder, int position) {
    }

    private TextView name;
    private TextView latitude;
    private TextView longitude;

    @Override
    public int getItemCount() {
        return 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.contactName);
            latitude = itemView.findViewById(R.id.contactLatitude);
            longitude = itemView.findViewById(R.id.contactLongitude);
        }


    }
}
