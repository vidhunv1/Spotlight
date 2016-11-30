package com.stairway.spotlight.screens.message;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder>{
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
            m = messageList.get(i);
            if(m.getReceiptId()!=null && !m.getReceiptId().isEmpty() && m.getReceiptId().equals(deliveryReceiptId)) {
                isBeforeReceiptId = true;
            }
            if(isBeforeReceiptId) {
                if (m.getMessageStatus() == messageStatus)
                    return;
                if (messageStatus == MessageResult.MessageStatus.NOT_SENT)
                    if (m.getMessageStatus() == MessageResult.MessageStatus.DELIVERED || m.getMessageStatus() == MessageResult.MessageStatus.READ)
                        return;
                if (messageStatus == MessageResult.MessageStatus.DELIVERED)
                    if (m.getMessageStatus() == MessageResult.MessageStatus.READ)
                        return;

                if(!m.getChatId().equals(m.getFromId())) {
                    m.setMessageStatus(messageStatus);
                    messageList.set(i, m);
                    this.notifyItemChanged(i);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder viewHolder = null;
        switch (viewType) {
            case VIEW_TYPE_SEND:
                View view1 = LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.item_message_receive, parent, false);
                viewHolder = new ViewHolder(view1);
                break;
            case VIEW_TYPE_RECV:
                View view2 = LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.item_message_send, parent, false);
                viewHolder = new ViewHolder(view2);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.renderItem(messageList.get(position));
    }

    @Override
    public int getItemViewType(int position) {
        if(messageList.get(position).getChatId().equals(messageList.get(position).getFromId()))
            return VIEW_TYPE_SEND;
        return VIEW_TYPE_RECV;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.tv_messageitem_message)
        TextView message;

        @Bind(R.id.tv_messageitem_deliverystatus)
        TextView deliveryStatus;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void renderItem(MessageResult messageResult) {
            message.setText(messageResult.getMessage().trim());
            Logger.d("MsgResult : "+messageResult.getMessageStatus().name());
            if(messageResult.getMessageStatus() == MessageResult.MessageStatus.NOT_SENT)
                deliveryStatus.setText("  X");
            else if(messageResult.getMessageStatus() == MessageResult.MessageStatus.SENT)
                deliveryStatus.setText("  S");
            else if(messageResult.getMessageStatus() == MessageResult.MessageStatus.DELIVERED)
                deliveryStatus.setText("  D");
            else if(messageResult.getMessageStatus() == MessageResult.MessageStatus.READ)
                deliveryStatus.setText("  R");
            else
                deliveryStatus.setText("");
        }
    }
}
