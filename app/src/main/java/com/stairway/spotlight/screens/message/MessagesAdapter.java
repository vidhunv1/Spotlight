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
import com.google.gson.JsonSyntaxException;
import com.stairway.spotlight.R;
import com.stairway.spotlight.core.GsonProvider;
import com.stairway.spotlight.core.Logger;
import com.stairway.spotlight.core.lib.RoundedCornerTransformation;
import com.stairway.spotlight.models.ButtonTemplate;
import com.stairway.spotlight.models.GenericTemplate;
import com.stairway.spotlight.models.Message;
import com.stairway.spotlight.models.MessageResult;
import com.stairway.spotlight.models.QuickReply;
import com.stairway.spotlight.models._Button;
import com.stairway.spotlight.models._DefaultAction;

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
    private SparseArray<Message> messageCache;
    private List<QuickReply> quickReplies;

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
        this.messageCache = new SparseArray<>();
    }

    public void setMessages(List<MessageResult> messages) {
        quickReplies = new ArrayList<>();
        this.messageList.clear();
        this.messageList.addAll(messages);
        this.notifyItemRangeChanged(0, messageList.size() - 1);
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
        if(messageList.isEmpty())
            return;
        try {
            if(messageCache.get((messageList.size()-1), null)==null) {
                messageCache.put((messageList.size()-1), GsonProvider.getGson().fromJson(messageList.get(messageList.size()-1).getMessage(), Message.class));
            }
            if(messageCache.get(messageList.size()-1).getQuickReplies()==null)
                return;
            quickReplies = messageCache.get(messageList.size()-1).getQuickReplies();
            if(quickReplies.size()>=1)
                this.notifyItemInserted(messageList.size());
        } catch (JsonSyntaxException e) {
            Logger.d(this, "JsonSyntaxError, do nothing");
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

        if(messageList.get(position).isMe()) {
            return VIEW_TYPE_SEND_TEXT;
        } else {
            Message parsedMessage;
            try {
                if(messageCache.get(position, null)==null) {
                    parsedMessage = GsonProvider.getGson().fromJson(messageList.get(position).getMessage(), Message.class);
                    messageCache.put(position, parsedMessage);
                } else {
                    parsedMessage = messageCache.get(position);
                }
            } catch (JsonSyntaxException e) {
                //TODO: Should fallback to text?
                parsedMessage = new Message();
                parsedMessage.setText(messageList.get(position).getMessage());
                messageCache.put(position, parsedMessage);
                Logger.e(this, "JsonSyntaxError, falling back to text");
            }

            if(parsedMessage.getMessageType() == Message.MessageType.generic_template)
                return VIEW_TYPE_RECV_TEMPLATE_GENERIC;
            else if(parsedMessage.getMessageType() == Message.MessageType.button_template)
                return VIEW_TYPE_RECV_TEMPLATE_BUTTON;
            else if(parsedMessage.getMessageType() == Message.MessageType.text)
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
                sendViewHolder.renderItem(messageList.get(position).getMessage(), messageList.get(position).getTime(), messageList.get(position).getMessageStatus(), bubbleType(position));
                break;
            case VIEW_TYPE_RECV_TEXT:
                ReceiveTextViewHolder receiveViewHolder = (ReceiveTextViewHolder) holder;
                receiveViewHolder.renderItem(messageCache.get(position).getText(), messageList.get(position).getTime(), hasProfileDP(position), bubbleType(position));
                break;
            case VIEW_TYPE_RECV_TEMPLATE_GENERIC:
                ReceiveTemplateGenericViewHolder receiveTemplateViewHolder = (ReceiveTemplateGenericViewHolder) holder;
                receiveTemplateViewHolder.renderItem(messageCache.get(position).getGenericTemplate(), hasProfileDP(position), bubbleType(position));
                break;
            case VIEW_TYPE_RECV_TEMPLATE_BUTTON:
                ReceiveTemplateButtonViewHolder templateButtonVH = (ReceiveTemplateButtonViewHolder) holder;
                templateButtonVH.renderItem(messageCache.get(position).getButtonTemplate(), hasProfileDP(position), bubbleType(position));
                break;
            case VIEW_TYPE_QUICK_REPLIES:
                QuickRepliesViewHolder qrVH = (QuickRepliesViewHolder) holder;
                Logger.d(this, messageCache.get(position-1).toString());
                qrVH.renderItem(messageCache.get(position-1).getQuickReplies());
                break;
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

        void renderItem(List<QuickReply> quickReplies) {
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

        void renderItem(String message, String time, MessageResult.MessageStatus messageStatus, int bubbleType) {
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
            messageView.setText(message);
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

        void renderItem(String message, String time, boolean displayProfileDP, int bubbleType) {
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

            messageView.setText(message);

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
        @Bind(R.id.iv_profileImage)
        ImageView profileImage;
        @Bind(R.id.ll_bubble)
        LinearLayout bubble;
        @Bind(R.id.rl_message_receive_generic)
        RelativeLayout bubbleLayout;
        @Bind(R.id.ll_rcv_template_text)
        LinearLayout textContent;

        TextView buttons[];

        ReceiveTemplateGenericViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            buttons = new TextView[3];
            buttons[0] = (TextView) itemView.findViewById(R.id.tv_rcv_button1);
            buttons[1] = (TextView) itemView.findViewById(R.id.tv_rcv_button2);
            buttons[2] = (TextView) itemView.findViewById(R.id.tv_rcv_button3);

            buttons[0].setVisibility(View.GONE);
            buttons[1].setVisibility(View.GONE);
            buttons[2].setVisibility(View.GONE);
        }

        void renderItem(GenericTemplate genericTemplate, boolean displayProfileDP, int bubbleType) {
            switch (bubbleType) {
                case 0:
                    bubbleLayout.setPadding(0, 0, 0, (int)context.getResources().getDimension(R.dimen.bubble_start_top_space));
                    bubble.setBackgroundResource(R.drawable.bg_template_full);
                    if(!genericTemplate.getImageUrl().isEmpty() && genericTemplate.getImageUrl()!=null)
                        Glide.with(context).load(genericTemplate.getImageUrl()).bitmapTransform(new RoundedCornerTransformation(context, 18, 0, RoundedCornerTransformation.CornerType.TOP)).into(templateImage);
                    break;
                case 1:
                    bubbleLayout.setPadding(0, 0, 0, (int)context.getResources().getDimension(R.dimen.bubble_mid_top_space));
                    bubble.setBackgroundResource(R.drawable.bg_template_top);
                    if(!genericTemplate.getImageUrl().isEmpty() && genericTemplate.getImageUrl()!=null)
                        Glide.with(context).load(genericTemplate.getImageUrl()).bitmapTransform(new RoundedCornerTransformation(context, 18, 0, RoundedCornerTransformation.CornerType.TOP)).into(templateImage);
                    break;
                case 2:
                    bubbleLayout.setPadding(0, 0, 0, (int)context.getResources().getDimension(R.dimen.bubble_mid_top_space));
                    bubble.setBackgroundResource(R.drawable.bg_template_middle);
                    if(!genericTemplate.getImageUrl().isEmpty() && genericTemplate.getImageUrl()!=null) {
                        Glide.with(context).load(genericTemplate.getImageUrl()).bitmapTransform(new RoundedCornerTransformation(context, 10, 0, RoundedCornerTransformation.CornerType.TOP_LEFT)).into(templateImage);
                        Glide.with(context).load(genericTemplate.getImageUrl()).bitmapTransform(new RoundedCornerTransformation(context, 18, 0, RoundedCornerTransformation.CornerType.TOP_RIGHT)).into(templateImage);
                    }
                    break;
                case 3:
                    bubbleLayout.setPadding(0, 0, 0, (int)context.getResources().getDimension(R.dimen.bubble_start_top_space));
                    bubble.setBackgroundResource(R.drawable.bg_template_bottom);
                    if(!genericTemplate.getImageUrl().isEmpty() && genericTemplate.getImageUrl()!=null) {
                        Glide.with(context).load(genericTemplate.getImageUrl()).bitmapTransform(new RoundedCornerTransformation(context, 10, 0, RoundedCornerTransformation.CornerType.TOP_LEFT)).into(templateImage);
                        Glide.with(context).load(genericTemplate.getImageUrl()).bitmapTransform(new RoundedCornerTransformation(context, 18, 0, RoundedCornerTransformation.CornerType.TOP_RIGHT)).into(templateImage);
                    }
                    break;
            }

            title.setText(genericTemplate.getTitle());
            if(!genericTemplate.getSubtitle().isEmpty() && genericTemplate.getSubtitle()!=null) {
                subtitle.setText(genericTemplate.getSubtitle());
            }
            if(genericTemplate.getDefaultAction().getType() == _DefaultAction.Type.web_url) {
                url.setText(genericTemplate.getDefaultAction().getUrl());
                templateImage.setOnClickListener(v -> {
                    if(urlClickListener!=null) {
                        urlClickListener.urlButtonClicked(genericTemplate.getDefaultAction().getUrl());
                    }
                });
                textContent.setOnClickListener(v -> {
                    if(urlClickListener!=null) {
                        urlClickListener.urlButtonClicked(genericTemplate.getDefaultAction().getUrl());
                    }
                });
            } else if (genericTemplate.getDefaultAction().getType() == _DefaultAction.Type.postback){
                templateImage.setOnClickListener(v -> {
                    if(postbackClickListener!=null) {
                        postbackClickListener.sendPostbackMessage(genericTemplate.getTitle());
                    }
                });
                textContent.setOnClickListener(v -> {
                    if(postbackClickListener!=null) {
                        postbackClickListener.sendPostbackMessage(genericTemplate.getTitle());
                    }
                });
            }

            for (int i = 0; i < genericTemplate.getButtons().size(); i++) {
                _Button btn = genericTemplate.getButtons().get(i);
                if (!btn.getTitle().isEmpty()) {
                    buttons[i].setVisibility(View.VISIBLE);
                    buttons[i].setText(btn.getTitle());

                    buttons[i].setOnClickListener(v -> {
                        if(postbackClickListener!=null && btn.getType() == _Button.Type.postback)
                            postbackClickListener.sendPostbackMessage(btn.getTitle());
                        else if(urlClickListener!=null && btn.getType() == _Button.Type.web_url)
                            urlClickListener.urlButtonClicked(btn.getUrl());
                    });
                }
            }
            if(displayProfileDP)
                profileImage.setImageAlpha(255);
            else
                profileImage.setImageAlpha(0);
        }
    }

    class ReceiveTemplateButtonViewHolder extends RecyclerView.ViewHolder {
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

        TextView buttons[];

        ReceiveTemplateButtonViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            buttons = new TextView[3];
            buttons[0] = (TextView) itemView.findViewById(R.id.tv_rcv_button1);
            buttons[1] = (TextView) itemView.findViewById(R.id.tv_rcv_button2);
            buttons[2] = (TextView) itemView.findViewById(R.id.tv_rcv_button3);

            buttons[0].setVisibility(View.GONE);
            buttons[1].setVisibility(View.GONE);
            buttons[2].setVisibility(View.GONE);
        }

        void renderItem(ButtonTemplate buttonTemplate, boolean displayDP, int bubbleType) {
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

            if(!buttonTemplate.getText().isEmpty() && buttonTemplate.getText()!=null)
                text.setText(buttonTemplate.getText());

            if(buttonTemplate.getButtons().size()>=3)
                buttonLayout.setOrientation(LinearLayout.VERTICAL);


            for (int i = 0; i < buttonTemplate.getButtons().size(); i++) {
                _Button btn = buttonTemplate.getButtons().get(i);
                if (!btn.getTitle().isEmpty()) {
                    buttons[i].setVisibility(View.VISIBLE);
                    buttons[i].setText(btn.getTitle());

                    buttons[i].setOnClickListener(v -> {
                        if(postbackClickListener!=null && btn.getType() == _Button.Type.postback)
                            postbackClickListener.sendPostbackMessage(btn.getTitle());
                        else if(urlClickListener!=null && btn.getType() == _Button.Type.web_url)
                            urlClickListener.urlButtonClicked(btn.getUrl());
                    });
                }
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