package com.stairway.spotlight.screens.message;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.stairway.data.config.Logger;
import com.stairway.data.source.message.MessageResult;
import com.stairway.spotlight.R;
import com.stairway.spotlight.core.lib.MessageParser;
import com.stairway.spotlight.screens.message.view_models.TemplateButton;
import com.stairway.spotlight.screens.message.view_models.TemplateMessage;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by vidhun on 07/08/16.
 */
public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    //TODO: Change to cursor recycler view adapter.

    private Context context;
    private List<MessageResult> messageList;
    private SparseArray<Object> messageObjects;
    private final int VIEW_TYPE_SEND_TEXT = 0;
    private final int VIEW_TYPE_RECV_TEXT = 1;
    private final int VIEW_TYPE_RECV_TEMPLATE = 2;
    private PostbackClickListener postbackClickListener;
    private UrlClickListener urlClickListener;

    public MessagesAdapter(Context context, PostbackClickListener postbackClickListener, UrlClickListener urlClickListener) {
        this.postbackClickListener = postbackClickListener;
        this.urlClickListener = urlClickListener;
        this.context = context;
        this.messageList = new ArrayList<>();
        this.messageObjects = new SparseArray<>();
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

    private boolean hasProfileDP(int position) {
        if(position>0 && messageList.get(position-1).getChatId().equals(messageList.get(position-1).getFromId()))
            return false;
        return true;
    }

    @Override
    public int getItemViewType(int position) {
        MessageParser messageParser = new MessageParser(messageList.get(position).getMessage());

        try {
            messageObjects.put(position, messageParser.parseMessage());
        } catch (ParseException e) {
            Logger.e("ParseException Error parsing XML.");
            return -1;
        }
        if(messageList.get(position).getChatId().equals(messageList.get(position).getFromId())) {
            if (messageParser.getMessageType() == MessageParser.MessageType.template) {
                TemplateMessage templateMessage = (TemplateMessage) messageObjects.get(position);
                Logger.d(templateMessage.getButtons().get(0).toString());
                return VIEW_TYPE_RECV_TEMPLATE;
            }
            else if(messageParser.getMessageType() == MessageParser.MessageType.text)
                return VIEW_TYPE_RECV_TEXT;
            else
                Logger.e(messageParser.getMessageType().name()+" is not supported for receive");
        } else {
            if(messageParser.getMessageType() == MessageParser.MessageType.text)
                return VIEW_TYPE_SEND_TEXT;
            else
                Logger.e(messageParser.getMessageType().name()+" is not supported for send");
        }
        return -1;
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
            case VIEW_TYPE_RECV_TEXT:
                View view1 = inflater.inflate(R.layout.item_message_receive_text, parent, false);
                viewHolder = new ReceiveTextViewHolder(view1);
                break;
            case VIEW_TYPE_SEND_TEXT:
                View view2 = inflater.inflate(R.layout.item_message_send_text, parent, false);
                viewHolder = new SendTextViewHolder(view2);
                break;
            case VIEW_TYPE_RECV_TEMPLATE:
                View view3 = inflater.inflate(R.layout.item_message_receive_template, parent, false);
                viewHolder = new ReceiveTemplateViewHolder(view3);
                break;
            default:
                return null;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_SEND_TEXT:
                SendTextViewHolder sendViewHolder = (SendTextViewHolder) holder;
                sendViewHolder.renderItem(messageList.get(position));
                break;
            case VIEW_TYPE_RECV_TEXT:
                ReceiveTextViewHolder receiveViewHolder = (ReceiveTextViewHolder) holder;
                receiveViewHolder.renderItem(messageList.get(position), hasProfileDP(position));
                break;
            case VIEW_TYPE_RECV_TEMPLATE:
                ReceiveTemplateViewHolder receiveTemplateViewHolder = (ReceiveTemplateViewHolder) holder;
                receiveTemplateViewHolder.renderItem((TemplateMessage)messageObjects.get(position), hasProfileDP(position));
        }
    }

    public class SendTextViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.tv_messageitem_message)
        TextView message;

        @Bind(R.id.iv_delivery_status)
        ImageView deliveryStatus;

        public SendTextViewHolder(View itemView) {
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

    public class ReceiveTextViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.tv_messageitem_message)
        TextView message;

        @Bind(R.id.iv_profileImage)
        ImageView profileImage;

        public ReceiveTextViewHolder(View itemView) {
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

    public class ReceiveTemplateViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.iv_rcv_template_image)
        ImageView templateImage;

        @Bind(R.id.tv_rcv_template_title)
        TextView title;

        @Bind(R.id.tv_rcv_template_subtitle)
        TextView subtitle;

        @Bind(R.id.tv_rcv_template_url)
        TextView url;

        @Bind(R.id.tv_rcv_button1)
        TextView button1;

        @Bind(R.id.tv_rcv_button2)
        TextView button2;

        @Bind(R.id.tv_rcv_button3)
        TextView button3;

        @Bind(R.id.iv_profileImage)
        ImageView profileImage;

        public ReceiveTemplateViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void renderItem(TemplateMessage templateMessage, boolean displayProfileDP) {
            button1.setVisibility(View.GONE);
            button2.setVisibility(View.GONE);
            button3.setVisibility(View.GONE);
            title.setText(templateMessage.getTitle());
            if(!templateMessage.getSubtitle().isEmpty() && templateMessage.getSubtitle()!=null)
                subtitle.setText(templateMessage.getSubtitle());
            if(!templateMessage.getUrl().isEmpty() && templateMessage.getUrl()!=null)
                url.setText(templateMessage.getUrl());
            int i = 0;
            for (TemplateButton button : templateMessage.getButtons()) {
                Logger.d(button.toString());
                if(i==0)
                    if (!button.getTitle().isEmpty()) {
                        button1.setVisibility(View.VISIBLE);
                        button1.setText(button.getTitle());

                        button1.setOnClickListener(v -> {
                            if(postbackClickListener!=null && button.getType() == TemplateButton.Type.postback)
                                postbackClickListener.sendPostbackMessage(button.getTitle());
                            else if(urlClickListener!=null && button.getType() == TemplateButton.Type.web_url)
                                urlClickListener.urlButtonClicked(button.getUrl());
                        });
                    }
                if(i==1)
                    if (!button.getTitle().isEmpty()) {
                        button2.setVisibility(View.VISIBLE);
                        button2.setText(button.getTitle());

                        button2.setOnClickListener(v -> {
                            if(postbackClickListener!=null && button.getType() == TemplateButton.Type.postback)
                                postbackClickListener.sendPostbackMessage(button.getTitle());
                            else if(urlClickListener!=null && button.getType() == TemplateButton.Type.web_url)
                                urlClickListener.urlButtonClicked(button.getUrl());
                        });
                    }
                if(i==2)
                    if (!button.getTitle().isEmpty()) {
                        button3.setVisibility(View.VISIBLE);
                        button3.setText(button.getTitle());

                        button3.setOnClickListener(v -> {
                            if(postbackClickListener!=null && button.getType() == TemplateButton.Type.postback)
                                postbackClickListener.sendPostbackMessage(button.getTitle());
                            else if(urlClickListener!=null && button.getType() == TemplateButton.Type.web_url)
                                urlClickListener.urlButtonClicked(button.getUrl());
                        });
                    }
                i++;
            }
            if(displayProfileDP)
                profileImage.setImageAlpha(255);
            else
                profileImage.setImageAlpha(0);
        }
    }

    public interface PostbackClickListener {
        void sendPostbackMessage(String message);
    }

    public interface UrlClickListener {
        void urlButtonClicked(String url);
    }
}