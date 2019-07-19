package com.example.reconnect.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reconnect.R;
import com.example.reconnect.model.Event;
import com.example.reconnect.model.DateTitle;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<Event> mEvents;
    public final int TITLE = 1;
    public final int EVENT = 2;

    public EventAdapter(Context context, List<Event> events) {
        mContext = context;
        mEvents = events;
    }

    @NonNull
    @Override
    public EventAdapter.ViewHolderEvent onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_event, parent, false);
        return new ViewHolderEvent(view);
    }

    @Override
    public int getItemViewType(int position) {
        //TODO return TITLE if title return EVENT if event
        return 0; //placeholder
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        //TODO put logic here to make view show up use getItemViewType
    }

    /* creates an array that represents the order of title and event views we wish to display on the profile */
    public HashMap<String, Object> createViewOrderArray(List<Event> events) {
        HashMap<String, Object> toReturn = new HashMap<>();
        Date dateTracker = events.get(0).getDate("date");
        toReturn.put("Title", new DateTitle(dateTracker.toString()));

        for (int i = 0; i < events.size(); i++) {
            Event currEvent = events.get(i);
            Date currEventDate = currEvent.getDate("date");

            if (!currEventDate.equals(dateTracker)) {
                dateTracker = currEventDate;
                toReturn.put("Title", new DateTitle(dateTracker.toString()));
            }
            else {
                toReturn.put("Event", currEvent);
            }
        }

        return toReturn;
    }

    @Override
    public int getItemCount() {
        return mEvents.size();
    }

    public class ViewHolderEvent extends RecyclerView.ViewHolder {

        TextView meetingName;
        TextView attendee;
        TextView industry;
        TextView date;
        TextView time;

        public ViewHolderEvent(@NonNull View itemView) {
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

    public class ViewHolderTitle extends RecyclerView.ViewHolder {

        TextView dateTitle;
        Button addEvent;

        public ViewHolderTitle(@NonNull View itemView) {
            super(itemView);
            dateTitle = itemView.findViewById(R.id.dateTitle);
            addEvent = itemView.findViewById(R.id.addEvent);
        }

        public void bind(DateTitle date) {
            dateTitle.setText(date.getmDisplayDate());

            //TODO add onItemClick listener for the button
        }
    }
}
