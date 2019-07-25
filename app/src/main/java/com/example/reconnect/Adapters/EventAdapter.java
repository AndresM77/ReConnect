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
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.example.reconnect.Activities.RequestMeetingActivity;
import com.example.reconnect.R;
import com.example.reconnect.fragments.CalendarFragment;
import com.example.reconnect.model.DateTitle;
import com.example.reconnect.model.Event;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<Object> mEvents;
    private CalendarFragment mFragment;
    public final int TITLE = 1;
    public final int EVENT = 2;

    public EventAdapter(Context context, List<Object> events, CalendarFragment fragment) {
        mContext = context;
        mEvents = events;
        mFragment = fragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == EVENT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_event_invite, parent, false);
            return new ViewHolderEvent(view, mFragment);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_date_title, parent, false);
            return new ViewHolderTitle(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int typeInView = getItemViewType(position);
        if (typeInView == EVENT) {
            Event event = (Event)mEvents.get(position);
            ((ViewHolderEvent)holder).bind(event);
        } else if (typeInView == TITLE){
            if (position + 1 < getItemCount()) {
                if (getItemViewType(position + 1)!=TITLE) {
                    DateTitle title = (DateTitle)mEvents.get(position);
                    ((ViewHolderTitle)holder).bind(title);
                }
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mEvents.get(position) instanceof Event){
            return EVENT;
        } else {
            return TITLE;
        }
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
            addEvent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(mContext, RequestMeetingActivity.class);
                    i.putExtra("requesteeId", ParseUser.getCurrentUser().getObjectId());
                    mContext.startActivity(i);
                }
            });
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
        CalendarFragment mFragment;

        public ViewHolderEvent(@NonNull View itemView, CalendarFragment fragment) {
            super(itemView);
            meetingName = itemView.findViewById(R.id.meetingNameInvite);
            attendee = itemView.findViewById(R.id.attendeeInvite);
            industry = itemView.findViewById(R.id.industryInvite);
            date = itemView.findViewById(R.id.dateInvite);
            time = itemView.findViewById(R.id.timeInvite);
            accept = itemView.findViewById(R.id.ivAccept);
            pending = itemView.findViewById(R.id.ivPending);
            deny = itemView.findViewById(R.id.ivReject);
            eventLayout = itemView.findViewById(R.id.eventLayout);

            mFragment = fragment;
        }

        public void bind(final Event event) {

            boolean isPersonalEvent;
            try {
                isPersonalEvent = event.getAttendee().fetchIfNeeded().getUsername().equals(event.getCreator().fetchIfNeeded().getUsername());

            /* Elements we always want to show */

            String meetingTitle;
            if (event.getName().equals("")) { meetingTitle = "Meeting"; }
            else { meetingTitle = event.getName(); }

            if (isPersonalEvent) {
                meetingName.setVisibility(View.INVISIBLE);
                industry.setVisibility(View.INVISIBLE);
                attendee.setText(meetingTitle);
                accept.setVisibility(View.GONE);
                deny.setVisibility(View.GONE);
                pending.setVisibility(View.GONE);
            }
            else {
                // meeting name assignment
                String meetingWith = meetingTitle + " with";
                meetingName.setText(meetingWith);

                // attendee assignment
                String currentUserName;
                String attendeeUserName;
                Boolean isAttendee;
                try {
                    currentUserName = ParseUser.getCurrentUser().fetchIfNeeded().getUsername();
                    attendeeUserName = event.getAttendee().fetchIfNeeded().getUsername();
                    isAttendee = attendeeUserName.equals(currentUserName);
                    if (isAttendee) {
                        attendee.setText(event.getCreator().fetchIfNeeded().getUsername());
                    } else {
                        attendee.setText(event.getAttendee().fetchIfNeeded().getUsername());
                    }
                } catch (ParseException e) {
                    Log.e("EventAdapter", "Unable to retrieve attendee name");
                    e.printStackTrace();
                }

                // industry assignment
                try {
                    industry.setText(event.getAttendee().fetchIfNeeded().get("industry").toString());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            /* Elements we want to show depending on the status of the invite */
            boolean stillPending = event.getPending();
            boolean hasBeenAccepted = event.getAccepted();
            try {
                Boolean isAttendee = event.getAttendee().fetchIfNeeded().getUsername().equals(ParseUser.getCurrentUser().fetchIfNeeded().getUsername());

                if (stillPending) {
                    if (!isAttendee) {
                        // hide the accept and deny images
                        accept.setVisibility(View.GONE);
                        deny.setVisibility(View.GONE);
                    }
                    else {
                        // hide the pending icon
                        pending.setVisibility(View.GONE);

                        // implementation of accept or reject event functionality
                        accept.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                event.setPending(false);
                                event.setAccepted(true);
                                event.saveInBackground();
                                mFragment.queryEvents();
                            }
                        });

                        deny.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                event.setPending(false);
                                event.setAccepted(false);
                                event.saveInBackground();
                                mFragment.queryEvents();
                            }
                        });
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
                        event.delete();
                        return;
                    }
                }
            }

            catch (ParseException e) {
                Log.e("Event Adapter", "Unable to tell if current user is attendee of event");
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


            // meeting time assignment
            String timeSpan = event.get("startTime").toString() + " - " + event.get("endTime").toString();
            time.setText(timeSpan);

            /* delete event functionality */
            eventLayout.setOnLongClickListener(new View.OnLongClickListener() {
                private void showDialogForUserSelection() {
                    View view = LayoutInflater.from(mContext).inflate(R.layout.item_alert_delete_event, null);
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
                    alertDialogBuilder.setView(view);

                    final AlertDialog alertDialog = alertDialogBuilder.create();

                    //Configure Text
                    ImageView yes = view.findViewById(R.id.deleteYes);
                    ImageView no = view.findViewById(R.id.deleteNo);

                    yes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try {
                                event.delete();
                                mFragment.queryEvents();
                                alertDialog.hide();
                            } catch (ParseException e) {
                                Log.e("Event Adapter", "Unable to delete the event");
                                e.printStackTrace();
                            }
                        }
                    });

                    no.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertDialog.hide();
                        }
                    });

                    // Display the dialog
                    alertDialog.show();
                }

                @Override
                public boolean onLongClick(View view) {
                    showDialogForUserSelection();
                    return true;
                }
            });

        }
    }

}

