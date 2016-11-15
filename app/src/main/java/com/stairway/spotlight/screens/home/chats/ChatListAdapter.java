package com.stairway.spotlight.screens.home.chats;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.stairway.data.manager.Logger;
import com.stairway.spotlight.R;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ChatListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<ChatListItemModel> chatList;
    private ChatClickListener chatClickListener;
    private final int VIEW_WITH_NOTIFICATION=0, VIEW_WITHOUT_NOTIFICATION=1;

    public ChatListAdapter(Context context, List<ChatListItemModel> chatList, ChatClickListener chatClickListener) {
        this.chatClickListener = chatClickListener;
        this.chatList = chatList;
        this.context = context;
    }

    public void newChatMessage(ChatListItemModel chatListItemModel){
        for (int i = 0; i < chatList.size(); i++) {
            if(chatListItemModel.getChatId().equals(chatList.get(i).getChatId())){
                Logger.d("Adapter "+chatListItemModel.toString());
                Logger.d("Adapter "+chatList.get(i).toString());
                chatListItemModel.setNotificationCount(chatListItemModel.getNotificationCount() + chatList.get(i).getNotificationCount());

                if(i==0){
                    chatList.set(0, chatListItemModel);
                    notifyItemChanged(0);
                } else {
                    chatList.remove(i);
                    notifyItemRemoved(i);
                    chatList.add(0, chatListItemModel);
                    notifyItemInserted(0);
                }
                break;
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if(chatList.get(position).getNotificationCount()==0) {
            return VIEW_WITHOUT_NOTIFICATION;
        } else {
            return VIEW_WITH_NOTIFICATION;
        }
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case VIEW_WITH_NOTIFICATION:
                View notificationView = inflater.inflate(R.layout.item_chat_list_notification, parent, false);
                viewHolder = new NotificationViewHolder(notificationView);
                break;
            case VIEW_WITHOUT_NOTIFICATION:
                View withoutNotificationView = inflater.inflate(R.layout.item_chat_list, parent, false);
                viewHolder = new WithoutNotificationViewHolder(withoutNotificationView);
                break;
            default:
                return null;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case VIEW_WITH_NOTIFICATION:
                NotificationViewHolder notificationViewHolder = (NotificationViewHolder) holder;
                notificationViewHolder.renderItem(chatList.get(position));
                break;
            case VIEW_WITHOUT_NOTIFICATION:
                WithoutNotificationViewHolder withoutNotificationViewHolder = (WithoutNotificationViewHolder) holder;
                withoutNotificationViewHolder.renderItem(chatList.get(position));
                break;
            default:
                break;
        }
    }

    //    @Override
//    public void onBindViewHolder(ViewHolder holder, int position) {
//        holder.renderItem(chatList.get(position));
//
//        Logger.d("ChatList: "+chatList.get(position).toString());
//    }

    public class NotificationViewHolder extends RecyclerView.ViewHolder {

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

        public NotificationViewHolder(View itemView) {
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

    public class WithoutNotificationViewHolder extends RecyclerView.ViewHolder {

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

        public WithoutNotificationViewHolder(View itemView) {
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

            contactName.setTag(chatListItem.getChatId());
        }
    }

    public interface ChatClickListener {
        void onChatItemClicked(String userId);
    }
}
