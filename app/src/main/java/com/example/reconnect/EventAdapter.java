package com.example.reconnect;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reconnect.model.Event;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {

    private Context context;
    private List<Event> events;

    public EventAdapter(Context context, List<Event> events) {
        this.context = context;
        this.events = events;
    }

    @NonNull
    @Override
    public EventAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventAdapter.ViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event);
    }

    /* creates an array that represents the order of title and event views we wish to display on the profile */
    public ArrayList<Object> createViewOrderArray(List<Event> events) {
        ArrayList<Object> toReturn = new ArrayList<>();
        //TODO add title object to 0 index of toReturn
        Date dateTracker = events.get(0).getDate("date");

        for (int i = 0; i < events.size(); i++) {
            Event currEvent = events.get(i);
            Date currEventDate = currEvent.getDate("date");

            if (!currEventDate.equals(dateTracker)) {
                //TODO add title object to toReturn
                dateTracker = currEventDate;
            }

            toReturn.add(currEvent);
        }

        return toReturn;
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView meetingName;
        TextView attendee;
        TextView industry;
        TextView date;
        TextView time;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            meetingName = itemView.findViewById(R.id.meetingWith);
            attendee = itemView.findViewById(R.id.attendee);
            industry = itemView.findViewById(R.id.industry);
            date = itemView.findViewById(R.id.meetingDate);
            time = itemView.findViewById(R.id.meetingTime);
        }

        public void bind(Event event) {
            String meetingTitle;
            if (event.getName().equals("")) { meetingTitle = "Meeting"; }
            else { meetingTitle = event.getName(); }
            String meetingWith = meetingTitle + " with";
            meetingName.setText(meetingWith);
            String currentUserName;
            String attendeeUserName;
            try {
                currentUserName = ParseUser.getCurrentUser().fetchIfNeeded().getUsername();
                attendeeUserName = event.getAttendee().fetchIfNeeded().getUsername();

                if (attendeeUserName.equals(currentUserName)) {
                    attendee.setText(event.getCreator().fetchIfNeeded().getUsername());
                } else if (!attendeeUserName.equals(currentUserName)) {
                    attendee.setText(event.getAttendee().fetchIfNeeded().getUsername());
                }
            }
            catch (ParseException e){
                Log.e("EventAdapter", "Unable to retrieve attendee name");
                e.printStackTrace();
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime((Date) event.get("date"));
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            String displayDate = month + "/" + day + "/" + year;
            date.setText(displayDate);
            try {
                industry.setText(event.getAttendee().fetchIfNeeded().get("industry").toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (event.getAttendee().equals(ParseUser.getCurrentUser())) {
                attendee.setText(event.getCreator().toString());
            } else if (event.getCreator().equals(ParseUser.getCurrentUser())) {
                attendee.setText(event.getAttendee().toString());
            }
            String timeSpan = event.get("startTime").toString() + " - " + event.get("endTime").toString();
            time.setText(timeSpan);
        }
    }
}
