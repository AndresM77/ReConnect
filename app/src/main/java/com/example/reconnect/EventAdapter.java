package com.example.reconnect;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reconnect.model.Event;
import com.parse.ParseObject;

import org.w3c.dom.Text;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {

    private Context context;
    private List<Event> events;

    @NonNull
    @Override
    public EventAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull EventAdapter.ViewHolder holder, int position) {
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView attendee;
        TextView industry;
        TextView date;
        TextView time;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            attendee = itemView.findViewById(R.id.attendee);
            industry = itemView.findViewById(R.id.industry);
            date = itemView.findViewById(R.id.meetingDate);
            time = itemView.findViewById(R.id.meetingTime);
        }

        public void bind(Event event) {
            //TODO
        }
    }
}
