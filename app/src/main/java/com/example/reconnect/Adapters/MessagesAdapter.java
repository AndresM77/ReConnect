package com.example.reconnect.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reconnect.R;
import com.example.reconnect.model.Message;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {

    private Context context;
    private List<Message> messages;

    public MessagesAdapter(Context context, List<Message> messages) {
        this.context = context;
        this.messages = messages;
    }

    @NonNull
    @Override
    public MessagesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessagesAdapter.ViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvMessage;
        private TextView tvTimeStamp;
        private CardView cvMessage;

        public ViewHolder(View itemView) {
            super(itemView);

            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTimeStamp = itemView.findViewById(R.id.tvTimeStamp);
            cvMessage = itemView.findViewById(R.id.cvMessage);
        }

        // method that connects information to create item_contact for MapFragment's Recycler View
        public void bind(Message message) {
            tvMessage.setText(message.getMessage());
            tvTimeStamp.setText(message.getCreatedAt().toString());
            tvTimeStamp.setTextSize(tvMessage.getTextSize()/4);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

            try {
                if (!message.getSender().fetchIfNeeded().getUsername().equals(ParseUser.getCurrentUser().fetchIfNeeded().getUsername())) {
                    params.addRule(RelativeLayout.ALIGN_RIGHT);
                    cvMessage.setLayoutParams(params);
                    tvMessage.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                    tvTimeStamp.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                }
                else {
                    params.addRule(RelativeLayout.ALIGN_LEFT);
                    cvMessage.setLayoutParams(params);
                    tvMessage.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                    tvTimeStamp.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                }
            } catch (ParseException e) {
                Log.e("Messages Adapter", "Unable to determine the which side to show message on");
                e.printStackTrace();
            }
        }

    }
}
