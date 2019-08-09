package com.example.reconnect.Adapters;

import android.content.Context;
import android.content.DialogInterface;
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

import com.example.reconnect.Activities.MessagesActivity;
import com.example.reconnect.R;
import com.example.reconnect.fragments.CalendarFragment;
import com.example.reconnect.model.Conversation;
import com.example.reconnect.model.DateTitle;
import com.example.reconnect.model.Event;
import com.example.reconnect.model.Message;
import com.example.reconnect.model.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        TextView pending;
        ImageView deny;
        ConstraintLayout eventLayout;
        CalendarFragment mFragment;
        FirebaseFunctions firebaseFunctions;

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

                final String meetingTitle = event.getName().equals("") ? "Meeting " : event.getName();
                Boolean isPersonalEvent = event.getAttendee().getUsername().equals(event.getCreator().getUsername());

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
                            pending.setVisibility(View.VISIBLE);
                        } else {
                            pending.setVisibility(View.GONE);
                            accept.setVisibility(View.VISIBLE);
                            deny.setVisibility(View.VISIBLE);


                            // implementation of accept or reject event functionality
                            accept.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    event.setPending(false);
                                    event.setAccepted(true);
                                    event.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            alertRequestAccepted(event);
                                        }
                                    });
                                    mFragment.eventQuery(view);

                                }
                            });

                            deny.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    event.setPending(false);
                                    event.setAccepted(false);
                                    event.saveInBackground();
                                    mFragment.eventQuery(view);
                                }
                            });
                        }
                    } else {
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

                // show messages on click
                eventLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(mContext, MessagesActivity.class);
                        i.putExtra("contact", event.getOtherUser());
                        mContext.startActivity(i);
                    }
                });

                // delete event functionality
                eventLayout.setOnLongClickListener(new View.OnLongClickListener() {
                    private void showDialogForUserSelection() {
                        final View view = LayoutInflater.from(mContext).inflate(R.layout.item_alert_delete_event, null);
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
                        alertDialogBuilder.setView(view);
                        final AlertDialog alertDialog = alertDialogBuilder.create();

                        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                try {
                                    event.delete();
                                    mFragment.eventQuery(view);
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

        private void updateStreaks(ParseUser attendee) {
            int attendeeStreaks = (Integer) attendee.get("streaks");

            attendee.put("streaks", attendeeStreaks++);

            attendee.saveInBackground();
        }


        public void showEventAsPersonal(String meetingTitle) {
            meetingName.setVisibility(View.INVISIBLE);
            accept.setVisibility(View.GONE);
            deny.setVisibility(View.GONE);
            pending.setVisibility(View.GONE);
            industry.setVisibility(View.INVISIBLE);
            // (1) set meeting title
            attendee.setText(meetingTitle);
        }

        public void createAttendeeDescription(Event event) throws ParseException {
            if (event.currUserIsAttendee()) {
                attendee.setText(User.getFullName(event.getCreator()));
            } else {
                attendee.setText(User.getFullName(event.getOtherUser()));
            }
            String attendeeIndustry = event.getAttendee().get("industry").toString();
            industry.setText(attendeeIndustry);
        }

        public void alertRequestAccepted(final Event event) {
            // Send message to other user
            final Message approvalMessage = new Message();
            Conversation.findConversation(event.getCreator(), event.getAttendee(), new FindCallback<Conversation>() {
                @Override
                public void done(final List<Conversation> objects, ParseException e) {
                    if (e != null) {
                        Log.e("Event Adapter", "Unable to find converstaion to send approval/reject message!");
                        e.printStackTrace();
                        return;
                    }
                    if (objects.size() == 0) {
                        final Conversation conversation = new Conversation();
                        conversation.put("converser", ParseUser.getCurrentUser());
                        conversation.put("conversee", event.getOtherUser());
                        conversation.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                approvalMessage.setConversation(conversation);
                                approvalMessage.setMessage(event.getAttendee().get("firstName") + " accepted your meeting request!");

                                approvalMessage.setRecipient(event.getCreator());
                                approvalMessage.setSender(ParseUser.getCurrentUser());
                                approvalMessage.setIsRequest(true);

                                approvalMessage.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e != null) {
                                            Log.d("Event Adapter", "Error sending approval message");
                                            e.printStackTrace();
                                            return;
                                        }
                                        Log.d("Event Adapter", "Success sending approval message");
                                        Conversation convo = conversation;
                                        convo.setLastMessage(approvalMessage);
                                        convo.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if (e != null) {
                                                    Log.d("Event Adapter", "Error sending approval convo");
                                                    e.printStackTrace();
                                                    return;
                                                }
                                                Log.d("Event Adapter", "Success sending approval convo");
                                                //Update the streaks
                                                updateStreaks(event.getAttendee());
                                                accept.setVisibility(View.GONE);
                                                deny.setVisibility(View.GONE);
                                            }
                                        });
                                    }
                                });
                            }
                        });

                    } else {
                        approvalMessage.setConversation(objects.get(0));
                        approvalMessage.setMessage(event.getAttendee().get("firstName") + " accepted your meeting request!");

                        approvalMessage.setRecipient(event.getCreator());
                        approvalMessage.setSender(ParseUser.getCurrentUser());
                        approvalMessage.setIsRequest(true);
                        approvalMessage.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e != null) {
                                    Log.d("Event Adapter", "Error sending approval message");
                                    e.printStackTrace();
                                    return;
                                }
                                Log.d("Event Adapter", "Success sending approval message");
                                Conversation convo = objects.get(0);
                                convo.setLastMessage(approvalMessage);
                                convo.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e != null) {
                                            Log.d("Event Adapter", "Error sending approval convo");
                                            e.printStackTrace();
                                            return;
                                        }
                                        Log.d("Event Adapter", "Success sending approval convo");
                                    }
                                });
                            }
                        });
                    }
                }
            });

            // Notify the other user that a message sent
            firebaseFunctions = FirebaseFunctions.getInstance();
            Task<String> result = notifyAccepted(event.getCreator().get("deviceId").toString(), User.getFullName(event.getAttendee()) + " sent you a new message");
        }

        private Task<String> notifyAccepted(String deviceId, String s) {
            Map<String,Object> data = new HashMap<>();
            data.put("text", s);
            data.put("token", deviceId);
            data.put("push", true);

            return firebaseFunctions.getHttpsCallable("sendMessage")
                    .call(data)
                    .continueWith(new Continuation<HttpsCallableResult, String>() {
                        @Override
                        public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                            return (String) task.getResult().getData();
                        }
                    });
        }
    }
}