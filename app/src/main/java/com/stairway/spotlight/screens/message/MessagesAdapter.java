package com.stairway.spotlight.screens.message;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.stairway.data.manager.Logger;
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
        Logger.d("[MessagesAdapter] add all messages");
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
//        @Bind(R.id.tv_messageItem_contactName)
//        TextView contactName;

        @Bind(R.id.tv_messageitem_message)
        TextView message;

        @Bind(R.id.tv_messageitem_time)
        TextView time;

//        @Bind(R.id.tv_messageitem_deliverystatus)
//        TextView deliveryStatus;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void renderItem(MessageResult messageResult) {
//            contactName.setText(messageResult.getFromId());
//            if(messageResult.getMessage().length()%27==0)
//                messageResult.setMessage(messageResult.getMessage()+"\n");
//            else
//                messageResult.setMessage(messageResult.getMessage()+"        ");
//            message.setText(messageResult.getMessage());

            message.setText(messageResult.getMessage().trim() + "        ");
            time.setText("09:45");
//            if(messageResult.getDeliveryStatus() == null || messageResult.getDeliveryStatus() == MessageResult.DeliveryStatus.NOT_AVAILABLE)
//                deliveryStatus.setText("");
//            else
//                deliveryStatus.setText(String.valueOf(messageResult.getDeliveryStatus().ordinal()));
        }
    }
}
