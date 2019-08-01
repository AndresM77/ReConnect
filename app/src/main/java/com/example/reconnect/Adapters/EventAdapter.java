package com.example.reconnect.Adapters;

import android.content.Context;
import android.content.DialogInterface;
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
            Event event = (Event) mEvents.get(position);
            ((ViewHolderEvent) holder).bind(event);
        } else if (typeInView == TITLE) {
            if (position + 1 < getItemCount()) {
                if (getItemViewType(position + 1) != TITLE) {
                    DateTitle title = (DateTitle) mEvents.get(position);
                    ((ViewHolderTitle) holder).bind(title);
                }
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mEvents.get(position) instanceof Event) {
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

        public ViewHolderTitle(@NonNull View itemView) {
            super(itemView);
            dateTitle = itemView.findViewById(R.id.dateTitle);
        }

        public void bind(DateTitle date) {
            dateTitle.setText(date.getmDisplayDate());
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
            try {

                String meetingTitle = event.getName().equals("") ? "Meeting " : event.getName();
                Boolean isPersonalEvent = event.getAttendee().fetchIfNeeded().getUsername().equals(event.getCreator().fetchIfNeeded().getUsername());

                // set all views with information that does not depend on the status of the event

                // meeting date assignment
                date.setText(event.getDateString());

                // meeting time assignment
                String timeSpan = event.get("startTime").toString() + " - " + event.get("endTime").toString();
                time.setText(timeSpan);

                if (isPersonalEvent) {
                    showEventAsPersonal(meetingTitle);
                } else {

                    // (1) set meeting title
                    meetingName.setText(meetingTitle + " with");

                    // (2) set the attendee
                    createAttendeeDescription(event);

                    // (3) show other views depending on status of the event
                    if (event.getPending()) {
                        if (!event.currUserIsAttendee()) {
                            accept.setVisibility(View.GONE);
                            deny.setVisibility(View.GONE);
                        } else {
                            pending.setVisibility(View.GONE);

                            // implementation of accept or reject event functionality
                            accept.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    event.setPending(false);
                                    event.setAccepted(true);
                                    event.saveInBackground();
                                    mFragment.eventQuery();
                                }
                            });
                            deny.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    event.setPending(false);
                                    event.setAccepted(false);
                                    event.saveInBackground();
                                    mFragment.eventQuery();
                                }
                            });
                        }
                    }
                    else {
                        if (event.getAccepted()) {
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

                // delete event functionality
                eventLayout.setOnLongClickListener(new View.OnLongClickListener() {
                    private void showDialogForUserSelection() {
                        View view = LayoutInflater.from(mContext).inflate(R.layout.item_alert_delete_event, null);
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
                        alertDialogBuilder.setView(view);
                        final AlertDialog alertDialog = alertDialogBuilder.create();

                        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                try {
                                    event.delete();
                                    mFragment.eventQuery();
                                    alertDialog.hide();
                                } catch (ParseException e) {
                                    Log.e("Event Adapter", "Unable to delete the event");
                                    e.printStackTrace();
                                }
                            }
                        });

                        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) { dialog.cancel(); }
                                });

                        alertDialog.show();
                    }

                    @Override
                    public boolean onLongClick(View view) {
                        showDialogForUserSelection();
                        return true;
                    }
                });

            }
            catch (ParseException e){
                Log.e("Event Adapter", "There was a problem fetching information to bind events together for calendar");
                e.printStackTrace();
            }
        }


        public void showEventAsPersonal(String meetingTitle) {
            meetingName.setVisibility(View.GONE);
            accept.setVisibility(View.GONE);
            deny.setVisibility(View.GONE);
            pending.setVisibility(View.GONE);
            industry.setVisibility(View.INVISIBLE);
            // (1) set meeting title
            attendee.setText(meetingTitle);

        }

        public void createAttendeeDescription(Event event) throws ParseException {
            if (event.currUserIsAttendee()) {
                attendee.setText(event.getCurrentUser().getUsername());
            } else {
                attendee.setText(event.getOtherUser().getUsername());
            }
            String attendeeIndustry = event.getAttendee().fetchIfNeeded().get("industry").toString();
            industry.setText(attendeeIndustry);
        }
    }
}