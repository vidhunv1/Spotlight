package com.stairway.spotlight.screens.home.chats;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.stairway.spotlight.R;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {
    private Context context;
    private List<ChatListItemModel> chatList;
    private ChatClickListener chatClickListener;

    public ChatListAdapter(Context context, List<ChatListItemModel> chatList, ChatClickListener chatClickListener) {
        this.chatClickListener = chatClickListener;
        this.chatList = chatList;
        this.context = context;
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_chat_list, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.renderItem(chatList.get(position));
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.ll_chatItem_content)
        LinearLayout chatListContent;

        @Bind(R.id.iv_chatItem_profileImage)
        ImageView profileImage;

        @Bind(R.id.tv_chatItem_contactName)
        TextView contactName;

        @Bind(R.id.tv_chatItem_message)
        TextView lastMessage;

        @Bind(R.id.tv_chatItem_time)
        TextView time;

        @Bind(R.id.tv_chatlist_notification)
        TextView notificationCount;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            chatListContent.setOnClickListener(view -> {
                if(chatClickListener != null)
                    chatClickListener.onChatItemClicked(contactName.getTag().toString());
            });
        }

        public void renderItem(ChatListItemModel chatListItem) {
            contactName.setText(chatListItem.getChatName());
            lastMessage.setText(chatListItem.getLastMessage());
            time.setText(chatListItem.getTime());
            profileImage.setImageResource(R.drawable.default_profile_image);
            notificationCount.setText(Integer.toString(chatListItem.getNotificationCount()));

            // Set userId to pass through onClick.
            contactName.setTag(chatListItem.getChatId());
        }
    }

    public interface ChatClickListener {
        void onChatItemClicked(String userId);
    }
}
