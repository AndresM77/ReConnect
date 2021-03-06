package com.example.reconnect.Adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.reconnect.R;
import com.example.reconnect.fragments.ConversationsFragment;
import com.example.reconnect.model.Conversation;
import com.example.reconnect.model.Message;
import com.example.reconnect.model.User;
import com.parse.ParseException;
import com.parse.ParseFile;

import java.util.List;

public class ConversationsAdapter extends RecyclerView.Adapter<ConversationsAdapter.ViewHolder> {

    private Context context;
    private List<Conversation> conversations;
    private ConversationsFragment.ConversationClickListener listener;

    public ConversationsAdapter(Context context, List<Conversation> conversations, ConversationsFragment.ConversationClickListener listener) {
        this.context = context;
        this.conversations = conversations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ConversationsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_conversation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationsAdapter.ViewHolder holder, int position) {
        Conversation conversation = conversations.get(position);
        holder.bind(conversation);
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView name;
        private ImageView ibProfileButton;
        private TextView lastMessage;

        public ViewHolder(final View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvUserName);
            ibProfileButton = itemView.findViewById(R.id.ivProfileImg);
            lastMessage = itemView.findViewById(R.id.tvMessage);

            // onClick listener to request a meeting
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onClick((Conversation) name.getTag());
                    itemView.setBackgroundColor(itemView.getResources().getColor(R.color.colorAccent));
                }
            });
        }

        /* method that connects information to create item_conversation for ConversationsFragment's Recycler View */
        public void bind(Conversation conversation) {
            itemView.setBackgroundColor(itemView.getResources().getColor(R.color.colorWhite));

            ParseFile profileImg = null;

            profileImg = (ParseFile) conversation.getOtherUser().get("profileImg");

            if (profileImg != null) {
                Glide.with(context).load(profileImg.getUrl()).circleCrop().into(ibProfileButton);
            }

            name.setText(User.getFullName(conversation.getOtherUser()));
            name.setTag(conversation);
                Message message = null;
                message = (Message) conversation.getLastMessage();
                if (message != null && message.get("message") != null ){
                    lastMessage.setText(message.getMessage());
                }
                else {
                    lastMessage.setText("");
                }


        }
    }
}

