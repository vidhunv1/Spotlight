package com.stairway.spotlight.screens.home;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonSyntaxException;
import com.stairway.spotlight.R;
import com.stairway.spotlight.core.GsonProvider;
import com.stairway.spotlight.core.lib.ImageUtils;
import com.stairway.spotlight.models.Message;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ChatListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private Context context;
    private List<ChatItem> chatList;
    private List<ChatItem> temp;
    private ChatClickListener chatClickListener;
    private final int VIEW_WITH_NOTIFICATION=0, VIEW_WITHOUT_NOTIFICATION=1;

    public ChatListAdapter(Context context, ChatClickListener chatClickListener) {
        this.chatClickListener = chatClickListener;
        this.chatList = new ArrayList<>();
        temp = new ArrayList<>();
        this.context = context;
    }

    public void setChatList(List<ChatItem> chatItems) {
        this.chatList = chatItems;
        this.notifyItemRangeChanged(0, chatItems.size());
    }

    public void newChatMessage(ChatItem chatItem){
        int i;
        for (i = 0; i < chatList.size(); i++) {
            if(chatItem.getChatId().equals(chatList.get(i).getChatId())){
                chatItem.setNotificationCount(chatItem.getNotificationCount() + chatList.get(i).getNotificationCount());
                chatItem.setChatName(chatList.get(i).getChatName());

                if(i==0){
                    chatList.set(0, chatItem);
                    notifyItemChanged(0);
                } else {
                    chatList.remove(i);
                    notifyItemRemoved(i);
                    chatList.add(0, chatItem);
                    notifyItemInserted(0);
                }
                break;
            }
        }
        if(i==chatList.size()) {
            chatList.add(0, chatItem);
            notifyItemChanged(0);
        }
    }

    public void setChatState(String fromId, String chatState){
        for (int i = 0; i < chatList.size(); i++) {
            if(fromId.equals(chatList.get(i).getChatId())){
                if(chatState.equals(chatList.get(i).getLastMessage()))
                    return;
                ChatItem item = chatList.get(i);
                temp.add(new ChatItem(
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
        ChatItem tempItem, item;
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
                if(position < (chatList.size()-1)) {
                    notificationViewHolder.renderItem(chatList.get(position), true);
                } else {
                    notificationViewHolder.renderItem(chatList.get(position), false);
                }
                break;
            case VIEW_WITHOUT_NOTIFICATION:
                WithoutNotificationViewHolder withoutNotificationViewHolder = (WithoutNotificationViewHolder) holder;
                if(position < (chatList.size()-1)) {
                    withoutNotificationViewHolder.renderItem(chatList.get(position), true);
                } else {
                    withoutNotificationViewHolder.renderItem(chatList.get(position), false);
                }
                break;
            default:
                break;
        }
    }

    private String getFormattedTime(DateTime time) {
        DateTime timeNow = DateTime.now();
        DateTimeFormatter timeFormat = DateTimeFormat.forPattern("h:mm a");
        if(timeNow.getDayOfMonth() == time.getDayOfMonth()) {
            return time.toString(timeFormat).toUpperCase().replace(".", "");
        } else if(time.getDayOfMonth() > (timeNow.getDayOfMonth()-7)) {
            return time.dayOfWeek().getAsShortText().toUpperCase();
        } else if(timeNow.getYear() == time.getYear()) {
            return time.monthOfYear().getAsShortText()+" "+time.getDayOfMonth();
        } else {
            return time.monthOfYear().getAsShortText()+" "+time.getDayOfMonth()+" AT "+time.toString(timeFormat)+" "+time.getYear();
        }
    }

    public class NotificationViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.ll_item_chat_notification)
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

        @Bind(R.id.view_contactItem_divider)
        View dividerLine;

        public NotificationViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            chatListContent.setOnClickListener(view -> {
                if(chatClickListener != null)
                    chatClickListener.onChatItemClicked(contactName.getTag().toString());
            });

            chatListContent.setOnLongClickListener(v -> {
                if(chatClickListener != null)
                    chatClickListener.onChatItemLongClicked(contactName.getTag().toString());
                return true;
            });
        }

        public void renderItem(ChatItem chatListItem, boolean isLineVisible) {
            contactName.setText(chatListItem.getChatName());

            if(isLineVisible) {
                dividerLine.setVisibility(View.VISIBLE);
            } else {
                dividerLine.setVisibility(View.GONE);
            }

            try {
                Message message = GsonProvider.getGson().fromJson(chatListItem.getLastMessage(), Message.class);

                if(message.getMessageType() == Message.MessageType.generic_template) {
                    lastMessage.setText(message.getGenericTemplate().getTitle());
                } else if(message.getMessageType() == Message.MessageType.button_template) {
                    lastMessage.setText(message.getButtonTemplate().getText());
                } else if(message.getMessageType() == Message.MessageType.text) {
                    lastMessage.setText(message.getText());
                }
            } catch(JsonSyntaxException e) {
                lastMessage.setText(chatListItem.getLastMessage());
            }

            time.setText(getFormattedTime(chatListItem.getTime()));
            profileImage.setImageDrawable(ImageUtils.getDefaultProfileImage(chatListItem.getChatName(), chatListItem.getChatId(), 18));
            notificationCount.setText(Integer.toString(chatListItem.getNotificationCount()));

            // Set userId to pass through onClick.
            contactName.setTag(chatListItem.getChatId());
        }
    }

    public class WithoutNotificationViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.ll_item_chat)
        LinearLayout chatListContent;

        @Bind(R.id.iv_chatItem_profileImage)
        ImageView profileImage;

        @Bind(R.id.tv_chatItem_contactName)
        TextView contactName;

        @Bind(R.id.tv_chatItem_message)
        TextView lastMessage;

        @Bind(R.id.tv_chatItem_time)
        TextView time;

        @Bind(R.id.view_contactItem_divider)
        View dividerLine;

        public WithoutNotificationViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            chatListContent.setOnClickListener(view -> {
                if(chatClickListener != null)
                    chatClickListener.onChatItemClicked(contactName.getTag().toString());
            });

            chatListContent.setOnLongClickListener(v -> {
                if(chatClickListener != null)
                    chatClickListener.onChatItemLongClicked(contactName.getTag().toString());
                return true;
            });
        }

        public void renderItem(ChatItem chatListItem, boolean isLineVisible) {
            contactName.setText(chatListItem.getChatName());

            if(isLineVisible) {
                dividerLine.setVisibility(View.VISIBLE);
            } else {
                dividerLine.setVisibility(View.GONE);
            }

            try {
                Message message = GsonProvider.getGson().fromJson(chatListItem.getLastMessage(), Message.class);

                if(message.getMessageType() == Message.MessageType.generic_template) {
                    lastMessage.setText(message.getGenericTemplate().getTitle());
                } else if(message.getMessageType() == Message.MessageType.button_template) {
                    lastMessage.setText(message.getButtonTemplate().getText());
                } else if(message.getMessageType() == Message.MessageType.text) {
                    lastMessage.setText(message.getText());
                }
            } catch(JsonSyntaxException e) {
                lastMessage.setText(chatListItem.getLastMessage());
            }
            time.setText(getFormattedTime(chatListItem.getTime()));
            profileImage.setImageDrawable(ImageUtils.getDefaultProfileImage(chatListItem.getChatName(), chatListItem.getChatId(), 18));
            contactName.setTag(chatListItem.getChatId());
        }
    }

    public interface ChatClickListener {
        void onChatItemClicked(String username);
        void onChatItemLongClicked(String username);
    }
}
