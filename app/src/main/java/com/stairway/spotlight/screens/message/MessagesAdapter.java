package com.stairway.spotlight.screens.message;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_message_list, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.renderItem(messageList.get(position));
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_messageItem_contactName)
        TextView contactName;

        @Bind(R.id.tv_messageitem_message)
        TextView message;

        @Bind(R.id.tv_messageitem_time)
        TextView time;

        @Bind(R.id.tv_messageitem_deliverystatus)
        TextView deliveryStatus;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void renderItem(MessageResult messageResult) {
            contactName.setText(messageResult.getFromId());
            message.setText(messageResult.getMessage());
            time.setText("09:45");
            if(messageResult.getDeliveryStatus() == null || messageResult.getDeliveryStatus() == MessageResult.DeliveryStatus.NOT_AVAILABLE)
                deliveryStatus.setText("");
            else
                deliveryStatus.setText(String.valueOf(messageResult.getDeliveryStatus().ordinal()));
        }
    }
}
