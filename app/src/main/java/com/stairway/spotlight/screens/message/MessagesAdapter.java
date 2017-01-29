package com.stairway.spotlight.screens.message;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.stairway.data.config.Logger;
import com.stairway.data.source.message.MessageResult;
import com.stairway.spotlight.R;
import com.stairway.spotlight.core.lib.MessageParser;
import com.stairway.spotlight.core.lib.RoundedCornerTransformation;
import com.stairway.spotlight.screens.message.view_models.TemplateButton;
import com.stairway.spotlight.screens.message.view_models.TemplateMessage;
import com.stairway.spotlight.screens.message.view_models.TextMessage;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import butterknife.Bind;
import butterknife.ButterKnife;
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

/**
 * Created by vidhun on 07/08/16.
 */
public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    //TODO: Change to cursor recycler view adapter.

    private Context context;
    private List<MessageResult> messageList;
    private SparseArray<Object> messageObjects;
    private List<String> quickReplies;

    private final int VIEW_TYPE_SEND_TEXT = 0;
    private final int VIEW_TYPE_RECV_TEXT = 1;
    private final int VIEW_TYPE_RECV_TEMPLATE_GENERIC = 2;
    private final int VIEW_TYPE_RECV_TEMPLATE_BUTTON = 3;
    private final int VIEW_TYPE_QUICK_REPLIES = 4;

    private PostbackClickListener postbackClickListener;
    private UrlClickListener urlClickListener;
    private QuickRepliesAdapter.QuickReplyClickListener quickReplyClickListener;

    public MessagesAdapter(Context context, PostbackClickListener postbackClickListener, UrlClickListener urlClickListener, QuickRepliesAdapter.QuickReplyClickListener qrListener) {
        this.quickReplyClickListener = qrListener;
        this.postbackClickListener = postbackClickListener;
        this.urlClickListener = urlClickListener;
        this.context = context;
        this.messageList = new ArrayList<>();
        this.messageObjects = new SparseArray<>();
    }

    public void setMessages(List<MessageResult> messages) {
        quickReplies = new ArrayList<>();
        this.messageList.clear();
        this.messageList.addAll(messages);
        this.notifyItemRangeInserted(0, messageList.size() - 1);
        setQuickReplies();
    }

    public void addMessage(MessageResult messageResult) {
        if(quickReplies!=null && quickReplies.size()>0) {
            this.notifyItemRemoved(messageList.size());
            quickReplies = null;
        }
        messageList.add(messageResult);
        this.notifyItemInserted(messageList.size()-1);
        this.notifyItemChanged(messageList.size()-2);
        setQuickReplies();
    }

    private void setQuickReplies() {
        try {
            MessageParser messageParser = new MessageParser(messageList.get(messageList.size()-1).getMessage());
            quickReplies = messageParser.parseQuickReplies();
            if(quickReplies.size()>=1)
                this.notifyItemInserted(messageList.size());
        } catch (ParseException e) {
            Logger.d(this, "Parse exception");
        }
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
                if (messageStatus == MessageResult.MessageStatus.SENT)
                    if (m.getMessageStatus() == MessageResult.MessageStatus.READ)
                        return;
//                    if (m.getMessageStatus() == MessageResult.MessageStatus.DELIVERED || m.getMessageStatus() == MessageResult.MessageStatus.READ)
//                        return;
//                if (messageStatus == MessageResult.MessageStatus.DELIVERED)
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
        if(position == messageList.size())
            return VIEW_TYPE_QUICK_REPLIES;

        try {
            MessageParser messageParser = new MessageParser(messageList.get(position).getMessage());
            messageObjects.put(position, messageParser.getMessageObject());

            if(messageList.get(position).isMe()) {
                if(messageParser.getMessageType() == MessageParser.MessageType.text)
                    return VIEW_TYPE_SEND_TEXT;
                else
                    Logger.e(this, messageParser.getMessageType().name()+" is not supported for send");
            } else {
                if (messageParser.getMessageType() == MessageParser.MessageType.template) {
                    TemplateMessage templateMessage = (TemplateMessage) messageObjects.get(position);

                    if(templateMessage.getType() == TemplateMessage.TemplateType.generic)
                        return VIEW_TYPE_RECV_TEMPLATE_GENERIC;
                    else if(templateMessage.getType() == TemplateMessage.TemplateType.button)
                        return VIEW_TYPE_RECV_TEMPLATE_BUTTON;
                }
                else if(messageParser.getMessageType() == MessageParser.MessageType.text)
                    return VIEW_TYPE_RECV_TEXT;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Logger.e(this, "ParseException Error parsing XML.");
            return VIEW_TYPE_RECV_TEXT;
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        if(quickReplies!=null && quickReplies.size()>=1)
            return messageList.size()+1;
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
            case VIEW_TYPE_RECV_TEMPLATE_GENERIC:
                View view3 = inflater.inflate(R.layout.item_message_receive_template_generic, parent, false);
                viewHolder = new ReceiveTemplateGenericViewHolder(view3);
                break;
            case VIEW_TYPE_RECV_TEMPLATE_BUTTON:
                View view4 = inflater.inflate(R.layout.item_message_receive_template_button, parent, false);
                viewHolder = new ReceiveTemplateButtonViewHolder(view4);
                break;
            case VIEW_TYPE_QUICK_REPLIES:
                View view5 = inflater.inflate(R.layout.item_quick_replies, parent, false);
                viewHolder = new QuickRepliesViewHolder(view5);
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
                TextMessage textMessage =(TextMessage)messageObjects.get(position);
                if(textMessage==null || textMessage.getText()==null) {
                    textMessage = new TextMessage(messageList.get(position).getMessage());
                    Logger.d(this, "Text message null");
                }
                sendViewHolder.renderItem(textMessage, messageList.get(position).getTime(), messageList.get(position).getMessageStatus(), bubbleType(position));
                break;
            case VIEW_TYPE_RECV_TEXT:
                ReceiveTextViewHolder receiveViewHolder = (ReceiveTextViewHolder) holder;
                TextMessage textMessage1 =(TextMessage)messageObjects.get(position);
                if(textMessage1==null || textMessage1.getText()==null)
                    textMessage1 = new TextMessage(messageList.get(position).getMessage());
                receiveViewHolder.renderItem(textMessage1, messageList.get(position).getTime(), hasProfileDP(position), bubbleType(position));
                break;
            case VIEW_TYPE_RECV_TEMPLATE_GENERIC:
                ReceiveTemplateGenericViewHolder receiveTemplateViewHolder = (ReceiveTemplateGenericViewHolder) holder;
                receiveTemplateViewHolder.renderItem((TemplateMessage)messageObjects.get(position), hasProfileDP(position), bubbleType(position));
                break;
            case VIEW_TYPE_RECV_TEMPLATE_BUTTON:
                ReceiveTemplateButtonViewHolder templateButtonVH = (ReceiveTemplateButtonViewHolder) holder;
                templateButtonVH.renderItem((TemplateMessage)messageObjects.get(position), hasProfileDP(position), bubbleType(position));
                break;
            case VIEW_TYPE_QUICK_REPLIES:
                QuickRepliesViewHolder qrVH = (QuickRepliesViewHolder) holder;
                qrVH.renderItem(quickReplies);
        }
    }

    private boolean hasProfileDP(int position) {
//        return !(position > 0 && messageList.get(position - 1).getChatId().equals(messageList.get(position - 1).getFromId()));
        return position == messageList.size()-1 || messageList.get(position+1).isMe();
    }

    private int bubbleType(int position) {
        final int START = 1, MIDDLE = 2, END = 3, FULL = 0;
        final boolean isMe = messageList.get(position).isMe();
        if((position==0 || isMe!=messageList.get(position-1).isMe()) && (position==messageList.size()-1 || isMe!=messageList.get(position+1).isMe()))
            return FULL;
        if(position!=0 && (position==messageList.size()-1 || isMe!=messageList.get(position+1).isMe()))
            return END;
        if((position!=0 && isMe==messageList.get(position-1).isMe()) && position!=messageList.size()-1)
            return MIDDLE;
        if((position==0 || isMe!=messageList.get(position-1).isMe()) && (position==messageList.size()-1 || isMe==messageList.get(position+1).isMe()))
            return START;
        return FULL;
    }

    class QuickRepliesViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.rv_quick_replies)
        RecyclerView quickRepliesListView;

        QuickRepliesViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void renderItem(List<String> quickReplies) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
            quickRepliesListView.setLayoutManager(layoutManager);
            quickRepliesListView.setAdapter(new QuickRepliesAdapter(quickReplyClickListener, quickReplies));
            OverScrollDecoratorHelper.setUpOverScroll(quickRepliesListView, OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL);
        }
    }

    class SendTextViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.tv_messageitem_message)
        TextView messageView;

        @Bind(R.id.iv_delivery_status)
        ImageView deliveryStatusView;

        @Bind(R.id.rl_bubble)
        RelativeLayout bubbleView;

        @Bind(R.id.message_send_text)
        RelativeLayout layout;

//        @Bind(R.id.tv_messageitem_time)
//        TextView timeView;
//
//        @Bind(R.id.tv_messageitem_status)
//        TextView statusView;

        SendTextViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void renderItem(TextMessage textMessage, String time, MessageResult.MessageStatus messageStatus, int bubbleType) {
            String deliveryStatus = "";
            switch (bubbleType) {
                case 0:
                    layout.setPadding(0, 0, 0, (int)context.getResources().getDimension(R.dimen.bubble_start_top_space));
                    bubbleView.setBackgroundResource(R.drawable.bg_msg_send_full);
                    break;
                case 1:
                    layout.setPadding(0, 0, 0, (int)context.getResources().getDimension(R.dimen.bubble_mid_top_space));
                    bubbleView.setBackgroundResource(R.drawable.bg_msg_send_top);
                    break;
                case 2:
                    layout.setPadding(0, 0, 0, (int)context.getResources().getDimension(R.dimen.bubble_mid_top_space));
                    bubbleView.setBackgroundResource(R.drawable.bg_msg_send_middle);
                    break;
                case 3:
                    layout.setPadding(0, 0, 0, (int)context.getResources().getDimension(R.dimen.bubble_start_top_space));
                    bubbleView.setBackgroundResource(R.drawable.bg_msg_send_bottom);
                    break;
            }
            messageView.setText(textMessage.getText());
            if(messageStatus == MessageResult.MessageStatus.NOT_SENT) {
                deliveryStatusView.setImageResource(R.drawable.ic_delivery_pending);
                deliveryStatus = "PENDING";
            }
            else if(messageStatus == MessageResult.MessageStatus.SENT) {
                deliveryStatusView.setImageResource(R.drawable.ic_delivery_sent);
                deliveryStatus = "SENT";
            }
            else if(messageStatus == MessageResult.MessageStatus.READ) {
                deliveryStatusView.setImageResource(R.drawable.ic_delivery_read);
                deliveryStatus = "SEEN";
            }
            else if(messageStatus == MessageResult.MessageStatus.DELIVERED)
                deliveryStatus = "DELIVERED";


//            String finalDeliveryStatus = deliveryStatus;
//            bubbleView.setOnClickListener(v -> {
//                if(bubbleView.isPressed()) {
//                    timeView.setVisibility(View.GONE);
//                    statusView.setVisibility(View.GONE);
//                    bubbleView.setPressed(false);
//                } else {
//                    timeView.setVisibility(View.VISIBLE);
//                    statusView.setVisibility(View.VISIBLE);
//                    timeView.setText(time);
//                    statusView.setText(finalDeliveryStatus);
//                    bubbleView.setPressed(true);
//                }
//            });
        }
    }

    class ReceiveTextViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.tv_messageitem_message)
        TextView messageView;

        @Bind(R.id.iv_profileImage)
        ImageView profileImageView;

        @Bind(R.id.rl_bubble)
        RelativeLayout bubbleView;

        @Bind(R.id.rl_message_receive_text)
        RelativeLayout bubbleLayout;

//        @Bind(R.id.tv_messageitem_time)
//        TextView timeView;
//
//        @Bind(R.id.tv_messageitem_status)
//        TextView statusView;

        ReceiveTextViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void renderItem(TextMessage textMessage, String time, boolean displayProfileDP, int bubbleType) {
            switch (bubbleType) {
                case 0:
                    bubbleLayout.setPadding(0, 0, 0, (int)context.getResources().getDimension(R.dimen.bubble_start_top_space));
                    bubbleView.setBackgroundResource(R.drawable.bg_msg_receive_full);
                    break;
                case 1:
                    bubbleLayout.setPadding(0, 0, 0, (int)context.getResources().getDimension(R.dimen.bubble_mid_top_space));
                    bubbleView.setBackgroundResource(R.drawable.bg_msg_receive_top);
                    break;
                case 2:
                    bubbleLayout.setPadding(0, 0, 0, (int)context.getResources().getDimension(R.dimen.bubble_mid_top_space));
                    bubbleView.setBackgroundResource(R.drawable.bg_msg_receive_middle);
                    break;
                case 3:
                    bubbleLayout.setPadding(0, 0, 0, (int)context.getResources().getDimension(R.dimen.bubble_start_top_space));
                    bubbleView.setBackgroundResource(R.drawable.bg_msg_receive_bottom);
                    break;
            }

            messageView.setText(textMessage.getText().trim());

//            timeView.setText("2:30 A.M.");
//            statusView.setText("SEEN");

//            bubbleView.setOnClickListener(v -> {
//                if(!bubbleView.isPressed()) {
//                    Logger.d(this, "UNPRESS");
//                    timeView.setVisibility(View.GONE);
//                    statusView.setVisibility(View.GONE);
//                    bubbleView.setPressed(false);
//                } else {
//                    Logger.d(this, "PRESS");
//                    timeView.setVisibility(View.VISIBLE);
//                    statusView.setVisibility(View.VISIBLE);
//                    timeView.setText(time);
//                    statusView.setText("SEEN");
//                    bubbleView.setPressed(true);
//                }
//            });

            if(displayProfileDP)
                profileImageView.setImageAlpha(255);
            else
                profileImageView.setImageAlpha(0);
        }
    }

    class ReceiveTemplateGenericViewHolder extends RecyclerView.ViewHolder {

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
        @Bind(R.id.ll_bubble)
        LinearLayout bubble;
        @Bind(R.id.rl_message_receive_generic)
        RelativeLayout bubbleLayout;

        ReceiveTemplateGenericViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void renderItem(TemplateMessage templateMessage, boolean displayProfileDP, int bubbleType) {
            switch (bubbleType) {
                case 0:
                    bubbleLayout.setPadding(0, 0, 0, (int)context.getResources().getDimension(R.dimen.bubble_start_top_space));
                    bubble.setBackgroundResource(R.drawable.bg_template_full);
                    break;
                case 1:
                    bubbleLayout.setPadding(0, 0, 0, (int)context.getResources().getDimension(R.dimen.bubble_mid_top_space));
                    bubble.setBackgroundResource(R.drawable.bg_template_top);
                    break;
                case 2:
                    bubbleLayout.setPadding(0, 0, 0, (int)context.getResources().getDimension(R.dimen.bubble_mid_top_space));
                    bubble.setBackgroundResource(R.drawable.bg_template_middle);
                    break;
                case 3:
                    bubbleLayout.setPadding(0, 0, 0, (int)context.getResources().getDimension(R.dimen.bubble_start_top_space));
                    bubble.setBackgroundResource(R.drawable.bg_template_bottom);
                    break;
            }

            button1.setVisibility(View.GONE);
            button2.setVisibility(View.GONE);
            button3.setVisibility(View.GONE);
            title.setText(templateMessage.getTitle());
            if(!templateMessage.getSubtitle().isEmpty() && templateMessage.getSubtitle()!=null)
                subtitle.setText(templateMessage.getSubtitle());
            if(!templateMessage.getUrl().isEmpty() && templateMessage.getUrl()!=null)
                url.setText(templateMessage.getUrl());
            if(!templateMessage.getImage().isEmpty() && templateMessage.getImage()!=null)
                Glide.with(context).load(templateMessage.getImage()).bitmapTransform(new RoundedCornerTransformation(context, 18, 0, RoundedCornerTransformation.CornerType.TOP)).into(templateImage);

            int i = 0;
            for (TemplateButton button : templateMessage.getButtons()) {
                Logger.d(this, button.toString());
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

    class ReceiveTemplateButtonViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_rcv_button1)
        TextView button1;
        @Bind(R.id.tv_rcv_button2)
        TextView button2;
        @Bind(R.id.tv_rcv_button3)
        TextView button3;
        @Bind(R.id.iv_profileImage)
        ImageView profileImage;
        @Bind(R.id.tv_rcv_message)
        TextView text;
        @Bind(R.id.ll_rcv_template_buttons)
        LinearLayout buttonLayout;
        @Bind(R.id.ll_bubble)
        LinearLayout bubble;
        @Bind(R.id.message_receive_button)
        RelativeLayout layout;

        ReceiveTemplateButtonViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void renderItem(TemplateMessage templateMessage, boolean displayDP, int bubbleType) {
            switch (bubbleType) {
                case 0:
                    layout.setPadding(0, 0, 0, (int)context.getResources().getDimension(R.dimen.bubble_start_top_space));
                    bubble.setBackgroundResource(R.drawable.bg_msg_receive_full);
                    buttonLayout.setBackgroundResource(R.drawable.bg_lower_template_bottom);
                    break;
                case 1:
                    layout.setPadding(0, 0, 0, (int)context.getResources().getDimension(R.dimen.bubble_mid_top_space));
                    bubble.setBackgroundResource(R.drawable.bg_msg_receive_top);
                    buttonLayout.setBackgroundResource(R.drawable.bg_lower_template_middle);
                    break;
                case 2:
                    layout.setPadding(0, 0, 0, (int)context.getResources().getDimension(R.dimen.bubble_mid_top_space));
                    bubble.setBackgroundResource(R.drawable.bg_msg_receive_middle);
                    buttonLayout.setBackgroundResource(R.drawable.bg_lower_template_middle);
                    break;
                case 3:
                    layout.setPadding(0, 0, 0, (int)context.getResources().getDimension(R.dimen.bubble_start_top_space));
                    bubble.setBackgroundResource(R.drawable.bg_msg_receive_bottom);
                    buttonLayout.setBackgroundResource(R.drawable.bg_lower_template_bottom);
                    break;
            }
            button1.setVisibility(View.GONE);
            button2.setVisibility(View.GONE);
            button3.setVisibility(View.GONE);

            if(!templateMessage.getText().isEmpty() && templateMessage.getText()!=null)
                text.setText(templateMessage.getText());

            if(templateMessage.getButtons().size()>=3)
                buttonLayout.setOrientation(LinearLayout.VERTICAL);

            int i = 0;
            for (TemplateButton button : templateMessage.getButtons()) {
                Logger.d(this, button.toString());
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
            if(displayDP)
                profileImage.setImageAlpha(255);
            else
                profileImage.setImageAlpha(0);
        }
    }

    interface PostbackClickListener {
        void sendPostbackMessage(String message);
    }

    interface UrlClickListener {
        void urlButtonClicked(String url);
    }
}