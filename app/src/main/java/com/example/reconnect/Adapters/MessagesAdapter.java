package com.example.reconnect.Adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
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
        private ConstraintLayout messageBubble;
        private CardView cvMessage;

        public ViewHolder(View itemView) {
            super(itemView);

            tvMessage = itemView.findViewById(R.id.tvMessage);
            messageBubble = itemView.findViewById(R.id.messageBubble);
            cvMessage = itemView.findViewById(R.id.cvMessage);
        }

        // method that connects information to create item_contact for MapFragment's Recycler View
        public void bind(Message message) {
            tvMessage.setText(message.getMessage());

            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(cvMessage.getLayoutParams());

            try {
                if (message.getSender().getUsername().equals(ParseUser.getCurrentUser().fetchIfNeeded().getUsername())) {
                    params.endToEnd = R.id.clContainer;
                    cvMessage.setLayoutParams(params);
                    tvMessage.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                    messageBubble.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
                }
                else {
                    params.startToStart = R.id.clContainer;
                    cvMessage.setLayoutParams(params);
                    cvMessage.setForegroundGravity(Gravity.LEFT);
                    tvMessage.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                    messageBubble.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent3));
                }
            } catch (ParseException e) {
                Log.e("Messages Adapter", "Unable to determine the which side to show message on");
                e.printStackTrace();
            }

            if (message.getIsRequest()) {
                tvMessage.setTypeface(null, Typeface.BOLD_ITALIC);
                tvMessage.setTextColor(ContextCompat.getColor(context, R.color.colorAccent3));
                messageBubble.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
            }


        }

    }
}
