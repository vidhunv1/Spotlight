package com.stairway.spotlight.screens.message;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.stairway.data.config.Logger;
import com.stairway.data.source.message.MessageResult;
import com.stairway.spotlight.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by vidhun on 07/08/16.
 */
public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    //TODO: Change to cursor recycler view adapter.

    private Context context;
    private List<MessageResult> messageList;
    private final int VIEW_TYPE_SEND = 0;
    private final int VIEW_TYPE_RECV = 1;

    public MessagesAdapter(Context context) {
        this.context = context;
        this.messageList = new ArrayList<>();
    }

    public void setMessages(List<MessageResult> messages) {
        this.messageList.clear();
        this.messageList.addAll(messages);
        this.notifyItemRangeInserted(0, messageList.size() - 1);
    }

    public void addMessage(MessageResult messageResult) {
        messageList.add(messageResult);
        this.notifyItemInserted(messageList.size()-1);
    }

    public void updateMessage(MessageResult messageResult) {
        int position = 0;
        for(MessageResult m: messageList) {
            if(messageResult.getMessageId().equals(m.getMessageId())) {
                messageList.set(position, messageResult);
                this.notifyItemChanged(position);
                break;
            }
            position++;
        }
    }

    public void updateDeliveryStatus(String deliveryReceiptId, MessageResult.MessageStatus messageStatus) {
        // TODO: Might be inefficient
        MessageResult m;
        boolean isBeforeReceiptId = false;
        for(int i=messageList.size()-1; i>=0; i--) {
            if(messageStatus == MessageResult.MessageStatus.DELIVERED)
                return;
            m = messageList.get(i);
            if(m.getReceiptId()!=null && !m.getReceiptId().isEmpty() && m.getReceiptId().equals(deliveryReceiptId)) {
                isBeforeReceiptId = true;
            }
            if(isBeforeReceiptId) {
                if (m.getMessageStatus() == MessageResult.MessageStatus.READ)
                    return;
                if (messageStatus == MessageResult.MessageStatus.NOT_SENT)
                    return;
//                    if (m.getMessageStatus() == MessageResult.MessageStatus.DELIVERED || m.getMessageStatus() == MessageResult.MessageStatus.READ)
//                        return;
//                if (messageStatus == MessageResult.MessageStatus.DELIVERED)
                if (messageStatus == MessageResult.MessageStatus.SENT)
                    if (m.getMessageStatus() == MessageResult.MessageStatus.READ)
                        return;
//                    if (m.getMessageStatus() == MessageResult.MessageStatus.READ)
//                        return;

                if(!m.getChatId().equals(m.getFromId())) {
                    m.setMessageStatus(messageStatus);
                    messageList.set(i, m);
                    this.notifyItemChanged(i);
                }
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(messageList.get(position).getChatId().equals(messageList.get(position).getFromId()))
            return VIEW_TYPE_SEND;
        return VIEW_TYPE_RECV;
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case VIEW_TYPE_SEND:
                View view1 = inflater.inflate(R.layout.item_message_receive, parent, false);
                viewHolder = new ReceiveViewHolder(view1);
                break;
            case VIEW_TYPE_RECV:
                View view2 = inflater.inflate(R.layout.item_message_send, parent, false);
                viewHolder = new SendViewHolder(view2);
                break;
            default:
                return null;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_RECV:
                SendViewHolder sendViewHolder = (SendViewHolder) holder;
                sendViewHolder.renderItem(messageList.get(position));
                break;
            case VIEW_TYPE_SEND:
                ReceiveViewHolder receiveViewHolder = (ReceiveViewHolder) holder;
                if(position>0 && messageList.get(position-1).getChatId().equals(messageList.get(position-1).getFromId()))
                    receiveViewHolder.renderItem(messageList.get(position), false);
                else
                    receiveViewHolder.renderItem(messageList.get(position), true);
                break;
        }
    }

    public class SendViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.tv_messageitem_message)
        TextView message;

        @Bind(R.id.iv_delivery_status)
        ImageView deliveryStatus;

        public SendViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void renderItem(MessageResult messageResult) {
            message.setText(messageResult.getMessage().trim());
            if(messageResult.getMessageStatus() == MessageResult.MessageStatus.NOT_SENT)
                deliveryStatus.setImageResource(R.drawable.ic_delivery_pending);
            if(messageResult.getMessageStatus() == MessageResult.MessageStatus.SENT)
                deliveryStatus.setImageResource(R.drawable.ic_delivery_sent);
            else if(messageResult.getMessageStatus() == MessageResult.MessageStatus.READ)
                deliveryStatus.setImageResource(R.drawable.ic_delivery_read);
//            else if(messageResult.getMessageStatus() == MessageResult.MessageStatus.DELIVERED)
        }
    }

    public class ReceiveViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.tv_messageitem_message)
        TextView message;

        @Bind(R.id.iv_profileImage)
        ImageView profileImage;

        public ReceiveViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void renderItem(MessageResult messageResult, boolean displayProfileDP) {
            message.setText(messageResult.getMessage().trim());

            if(displayProfileDP)
                profileImage.setImageAlpha(255);
            else
                profileImage.setImageAlpha(0);
        }
    }
}