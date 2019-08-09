package com.example.reconnect.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.reconnect.R;
import com.example.reconnect.model.Connection;
import com.example.reconnect.model.User;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

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

    @Override
    public int getItemCount() {
        return connections.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView name;
        private TextView distanceAwayV;
        private ImageView streakIcon;
        private ImageView profileBtn;
        private TextView streakNum;
        private ImageView followIcon;

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.contactName);
            distanceAwayV = itemView.findViewById(R.id.distanceAway);
            profileBtn = itemView.findViewById(R.id.ivProfileImg);
            streakIcon = itemView.findViewById(R.id.streakIcon);
            streakNum = itemView.findViewById(R.id.streakNum);
            followIcon = itemView.findViewById(R.id.followingIcon);

            // onClick listener to request a meeting
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // New intent to send User to RequestMeeting Activity after selecting
                    // a contact User name
                    Connection selectedConnection = (Connection) name.getTag();
                    User.showContactOptions(view.getContext(), selectedConnection.getOtherUser());
                }
            });



        }

        /* method that connects information to create item_contact for MapFragment's Recycler View */
        public void bind(final Connection connection) {
            itemView.setBackgroundColor(itemView.getResources().getColor(R.color.colorWhite));

            //set tag in order to get correct Connection to display later
            name.setTag(connection);

            streakNum.setText(connection.getOtherUser().get("streaks").toString());

            followIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ParseUser.getCurrentUser().equals(connection.getUser1())) {
                        if(!connection.getStarred12()) {
                            connection.put(Connection.KEY_STARRED_12, true);

                            Glide.with(context).load(R.drawable.baseline_star_black_48dp).into(followIcon);
                        } else {
                            connection.put(Connection.KEY_STARRED_12, false);

                            Glide.with(context).load(R.drawable.baseline_star_border_black_48dp).into(followIcon);
                        }
                    } else {
                        if(!connection.getStarred21()) {
                            connection.put(Connection.KEY_STARRED_21, true);

                            Glide.with(context).load(R.drawable.baseline_star_black_48dp).into(followIcon);
                        } else {
                            connection.put(Connection.KEY_STARRED_21, false);

                            Glide.with(context).load(R.drawable.baseline_star_border_black_48dp).into(followIcon);
                        }
                    }
                    connection.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            Toast.makeText(context, "Changed Following Status", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });

            // set the profile image
            ParseFile profileImg = profileImg = (ParseFile) connection.getOtherUser().get("profileImg");
            if (profileImg != null) {
                Glide.with(context).load(profileImg.getUrl()).circleCrop().into(profileBtn);
            }

            if (ParseUser.getCurrentUser().equals(connection.getUser1())) {
                if(connection.getStarred12()) {
                    Glide.with(context).load(R.drawable.baseline_star_black_48dp).into(followIcon);
                } else {
                    Glide.with(context).load(R.drawable.baseline_star_border_black_48dp).into(followIcon);
                }
            } else {
                if(connection.getStarred21()) {
                    Glide.with(context).load(R.drawable.baseline_star_black_48dp).into(followIcon);
                } else {
                    Glide.with(context).load(R.drawable.baseline_star_border_black_48dp).into(followIcon);
                }
            }

            // set the location away of the user
            ParseGeoPoint position1 = connection.getCurrentUser().getParseGeoPoint("location");
            ParseGeoPoint position2 = connection.getOtherUser().getParseGeoPoint("location");
            String out = Connection.getDistanceAway(position1, position2);
            distanceAwayV.setText(out);

            // set the name of the connection
            name.setText(User.getFullName(connection.getOtherUser()));

        }

    }
}
