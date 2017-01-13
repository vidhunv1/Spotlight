package com.stairway.spotlight.screens.home;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.stairway.spotlight.R;
import com.stairway.spotlight.core.lib.MessageParser;
import com.stairway.spotlight.screens.message.view_models.TemplateMessage;
import com.stairway.spotlight.screens.message.view_models.TextMessage;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ChatListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private Context context;
    private List<ChatListItemModel> chatList;
    private List<ChatListItemModel> temp;
    private ChatClickListener chatClickListener;
    private final int VIEW_WITH_NOTIFICATION=0, VIEW_WITHOUT_NOTIFICATION=1;

    public ChatListAdapter(Context context, List<ChatListItemModel> chatList, ChatClickListener chatClickListener) {
        this.chatClickListener = chatClickListener;
        this.chatList = chatList;
        temp = new ArrayList<>();
        this.context = context;
    }

    public void newChatMessage(ChatListItemModel chatListItemModel){
        int i;
        for (i = 0; i < chatList.size(); i++) {
            if(chatListItemModel.getChatId().equals(chatList.get(i).getChatId())){
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
        if(i==chatList.size()) {
            chatList.add(0, chatListItemModel);
            notifyItemChanged(0);
        }
        notifyDataSetChanged();
    }

    public void setChatState(String fromId, String chatState){
        for (int i = 0; i < chatList.size(); i++) {
            if(fromId.equals(chatList.get(i).getChatId())){
                if(chatState.equals(chatList.get(i).getLastMessage()))
                    return;
                ChatListItemModel item = chatList.get(i);
                temp.add(new ChatListItemModel(
                        item.getChatId(),
                        item.getChatName(),
                        item.getLastMessage(),
                        item.getTime(),
                        item.getNotificationCount()
                ));
                item.setLastMessage(chatState);
                chatList.set(i, item);
                notifyItemChanged(i);
                return;
            }
        }
    }

    public void resetChatState(String fromId){
        ChatListItemModel tempItem, item;
        for (int i = 0; i < chatList.size() ; i++) {
            item = chatList.get(i);
            if (fromId.equals(item.getChatId())) {
                for (int j = 0; j < temp.size(); j++) {
                    tempItem = temp.get(j);
                    if(fromId.equals(tempItem.getChatId())) {
                        chatList.set(i, tempItem);
                        notifyItemChanged(i);
                        temp.remove(j);
                        return;
                    }
                }
            }
        }
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
                View notificationView = inflater.inflate(R.layout.item_chat_notification, parent, false);
                viewHolder = new NotificationViewHolder(notificationView);
                break;
            case VIEW_WITHOUT_NOTIFICATION:
                View withoutNotificationView = inflater.inflate(R.layout.item_chat, parent, false);
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

            try {
                MessageParser messageParser = new MessageParser(chatListItem.getLastMessage());
                Object messageObject = messageParser.parseMessage();

                if(messageParser.getMessageType() == MessageParser.MessageType.template) {
                    TemplateMessage msg = (TemplateMessage)messageObject;
                    lastMessage.setText(msg.getDisplayMessage());
                } else if(messageParser.getMessageType() == MessageParser.MessageType.text) {
                    TextMessage msg = (TextMessage)messageObject;
                    lastMessage.setText(msg.getDisplayMessage());
                }
            } catch(ParseException e) {
                lastMessage.setText(chatListItem.getLastMessage());
            }

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

            try {
                MessageParser messageParser = new MessageParser(chatListItem.getLastMessage());
                Object messageObject = messageParser.parseMessage();

                if(messageParser.getMessageType() == MessageParser.MessageType.template) {
                    TemplateMessage msg = (TemplateMessage)messageObject;
                    lastMessage.setText(msg.getDisplayMessage());
                } else if(messageParser.getMessageType() == MessageParser.MessageType.text) {
                    TextMessage msg = (TextMessage)messageObject;
                    lastMessage.setText(msg.getDisplayMessage());
                }
            } catch(ParseException e) {
                lastMessage.setText(chatListItem.getLastMessage());
            }

            time.setText(chatListItem.getTime());
            profileImage.setImageResource(R.drawable.default_profile_image);

            contactName.setTag(chatListItem.getChatId());
        }
    }

    public interface ChatClickListener {
        void onChatItemClicked(String userId);
    }
}
