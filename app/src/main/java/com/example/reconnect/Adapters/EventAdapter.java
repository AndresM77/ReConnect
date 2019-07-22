package com.example.reconnect.Adapters;

import android.content.Context;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.example.reconnect.R;
import com.example.reconnect.model.Event;
import com.example.reconnect.model.DateTitle;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == EVENT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_event_invite, parent, false);
            return new ViewHolderEvent(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_date_title, parent, false);
            return new ViewHolderTitle(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ArrayList<Object> test = createViewOrderArray(mEvents);
        int typeInView = getItemViewType(position);
        if (typeInView == EVENT) {
            Event event = (Event)test.get(position);
            ((ViewHolderEvent)holder).bind(event);
        } else if (typeInView == TITLE){
            DateTitle title = (DateTitle)test.get(position);
            ((ViewHolderTitle)holder).bind(title);
        }
    }

    @Override
    public int getItemViewType(int position) {
        ArrayList test = createViewOrderArray(mEvents);
        if (test.get(position) instanceof Event){
            return EVENT;
        } else {
            return TITLE;
        }
    }

    /* creates an array that represents the order of title and event views we wish to display on the profile */
    public ArrayList<Object> createViewOrderArray(List<Event> events) {
        ArrayList<Object> toReturn = new ArrayList<>();
        Date dateTracker = events.get(0).getDate("date");
        toReturn.add(new DateTitle(dateTracker.toString()));

        for (int i = 0; i < events.size(); i++) {
            Event currEvent = events.get(i);
            Date currEventDate = currEvent.getDate("date");

            if (!currEventDate.equals(dateTracker)) {
                dateTracker = currEventDate;
                toReturn.add(new DateTitle(dateTracker.toString()));
            }
            toReturn.add( currEvent);
        }

        return toReturn;
    }

    @Override
    public int getItemCount() {
        return mEvents.size();
    }

    public class ViewHolderTitle extends ViewHolder {

        TextView dateTitle;
        ImageView addEvent;

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

    public class ViewHolderEvent extends ViewHolder {

        TextView meetingName;
        TextView attendee;
        TextView industry;
        TextView date;
        TextView time;
        ImageView accept;
        ImageView pending;
        ImageView deny;
        ConstraintLayout eventLayout;

        public ViewHolderEvent(@NonNull View itemView) {
            super(itemView);
            meetingName = itemView.findViewById(R.id.meetingNameInvite);
            attendee = itemView.findViewById(R.id.attendeeInvite);
            industry = itemView.findViewById(R.id.industryInvite);
            date = itemView.findViewById(R.id.meetingNameInvite);
            time = itemView.findViewById(R.id.timeInvite);
            accept = itemView.findViewById(R.id.ivAccept);
            pending = itemView.findViewById(R.id.ivPending);
            deny = itemView.findViewById(R.id.ivReject);
            eventLayout = itemView.findViewById(R.id.eventLayout);
        }

        public void bind(Event event) {

            /* Elements we want to show depending on the status of the invite */
            boolean stillPending = event.getPending();
            boolean hasBeenAccepted = event.getAccepted();
            boolean isAttendee = event.getAttendee().equals(ParseUser.getCurrentUser());

            // if waiting for response from invited person
            if (stillPending) {
                if (!isAttendee) {
                    // hide the accept and deny images
                    accept.setVisibility(View.GONE);
                    deny.setVisibility(View.GONE);
                }
                else {
                    // hide the pending icon
                    pending.setVisibility(View.GONE);
                }
            }

            else {
                if (hasBeenAccepted) {
                    // hide all status icons
                    accept.setVisibility(View.GONE);
                    deny.setVisibility(View.GONE);
                    pending.setVisibility(View.GONE);
                }
                else {
                    eventLayout.setVisibility(View.GONE);
                    return;
                }
            }

            //TODO implement onClick listeners where needed (in helper method, call these in above logic)

            /* Elements we always want to show */

            // meeting name assignment
            String meetingTitle;
            if (event.getName().equals("")) { meetingTitle = "Meeting"; }
            else { meetingTitle = event.getName(); }
            String meetingWith = meetingTitle + " with";
            meetingName.setText(meetingWith);

            // attendee assignment
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

            // meeting date assignment
            Calendar calendar = Calendar.getInstance();
            calendar.setTime((Date) event.get("date"));
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            String displayDate = month + "/" + day + "/" + year;
            date.setText(displayDate);

            // industry assignment
            try {
                industry.setText(event.getAttendee().fetchIfNeeded().get("industry").toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }

            /* if (event.getAttendee().equals(ParseUser.getCurrentUser())) {
                attendee.setText(event.getCreator().toString());
            } else if (event.getCreator().equals(ParseUser.getCurrentUser())) {
                attendee.setText(event.getAttendee().toString());
            } */

            // meeting time assignment
            String timeSpan = event.get("startTime").toString() + " - " + event.get("endTime").toString();
            time.setText(timeSpan);


        }
    }

}
