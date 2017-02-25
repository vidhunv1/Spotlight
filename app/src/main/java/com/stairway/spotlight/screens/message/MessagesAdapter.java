package com.stairway.spotlight.screens.message;

import android.content.Context;
import android.graphics.drawable.Drawable;
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
import com.stairway.spotlight.core.lib.AndroidUtils;
import com.stairway.spotlight.core.lib.ImageUtils;
import com.stairway.spotlight.core.lib.RoundedCornerTransformation;
import com.stairway.spotlight.models.ButtonTemplate;
import com.stairway.spotlight.models.GenericTemplate;
import com.stairway.spotlight.models.Message;
import com.stairway.spotlight.models.MessageResult;
import com.stairway.spotlight.models.QuickReply;
import com.stairway.spotlight.models._Button;
import com.stairway.spotlight.models._DefaultAction;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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
    private final int VIEW_TYPE_SEND_EMOTICON = 5;
    private final int VIEW_TYPE_RECV_EMOTICON = 6;

    private PostbackClickListener postbackClickListener;
    private UrlClickListener urlClickListener;
    private QuickRepliesAdapter.QuickReplyClickListener quickReplyClickListener;
    private Drawable textProfileDrawable;

    private int lastClickedPosition = -1;

    public MessagesAdapter(Context context, String chatUserName, String chatContactName, PostbackClickListener postbackClickListener, UrlClickListener urlClickListener, QuickRepliesAdapter.QuickReplyClickListener qrListener) {
        this.quickReplyClickListener = qrListener;
        this.postbackClickListener = postbackClickListener;
        this.urlClickListener = urlClickListener;
        this.context = context;
        this.messageList = new ArrayList<>();
        this.messageCache = new SparseArray<>();
        this.textProfileDrawable = ImageUtils.getDefaultProfileImage(chatContactName, chatUserName, 16);
    }

    public void setMessages(List<MessageResult> messages) {
        Logger.d(this, "setting messages "+messages.size());
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
            if(isAllEmoticon(messageList.get(position).getMessage())) {
                return VIEW_TYPE_SEND_EMOTICON;
            } else {
                return VIEW_TYPE_SEND_TEXT;
            }
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
                if(isAllEmoticon(parsedMessage.getText())) {
                    return VIEW_TYPE_RECV_EMOTICON;
                } else {
                    return VIEW_TYPE_RECV_TEXT;
                }
            }

            if(parsedMessage.getMessageType() == Message.MessageType.generic_template)
                return VIEW_TYPE_RECV_TEMPLATE_GENERIC;
            else if(parsedMessage.getMessageType() == Message.MessageType.button_template)
                return VIEW_TYPE_RECV_TEMPLATE_BUTTON;
            else if(parsedMessage.getMessageType() == Message.MessageType.text) {
                if(isAllEmoticon(parsedMessage.getText())) {
                    return VIEW_TYPE_RECV_EMOTICON;
                } else {
                    return VIEW_TYPE_RECV_TEXT;
                }
            }
            else if(parsedMessage.getMessageType() == Message.MessageType.unknown) {
                parsedMessage = new Message();
                parsedMessage.setText(messageList.get(position).getMessage());
                messageCache.put(position, parsedMessage);
                if(isAllEmoticon(parsedMessage.getText())) {
                    return VIEW_TYPE_RECV_EMOTICON;
                } else {
                    return VIEW_TYPE_RECV_TEXT;
                }
            }
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
    public long getItemId(int position) {
        return  messageList.get(position).getTime().getMillis();
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
            case VIEW_TYPE_RECV_EMOTICON:
                View view6 = inflater.inflate(R.layout.item_message_receive_emoticon, parent, false);
                viewHolder = new ReceiveEmoticonViewHolder(view6);
                break;
            case VIEW_TYPE_SEND_EMOTICON:
                View view7 = inflater.inflate(R.layout.item_message_send_emoticon, parent, false);
                viewHolder = new SendEmoticonViewHolder(view7);
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
                sendViewHolder.renderItem(messageList.get(position).getMessage(), getFormattedTime(messageList.get(position).getTime()), messageList.get(position).getMessageStatus(), position);
                break;
            case VIEW_TYPE_RECV_TEXT:
                ReceiveTextViewHolder receiveViewHolder = (ReceiveTextViewHolder) holder;
                receiveViewHolder.renderItem(messageCache.get(position).getText(), getFormattedTime(messageList.get(position).getTime()), position);
                break;
            case VIEW_TYPE_RECV_TEMPLATE_GENERIC:
                ReceiveTemplateGenericViewHolder receiveTemplateViewHolder = (ReceiveTemplateGenericViewHolder) holder;
                receiveTemplateViewHolder.renderItem(messageCache.get(position).getGenericTemplate(), position);
                break;
            case VIEW_TYPE_RECV_TEMPLATE_BUTTON:
                ReceiveTemplateButtonViewHolder templateButtonVH = (ReceiveTemplateButtonViewHolder) holder;
                templateButtonVH.renderItem(messageCache.get(position).getButtonTemplate(), position);
                break;
            case VIEW_TYPE_QUICK_REPLIES:
                QuickRepliesViewHolder qrVH = (QuickRepliesViewHolder) holder;
                Logger.d(this, messageCache.get(position-1).toString());
                qrVH.renderItem(messageCache.get(position-1).getQuickReplies());
                break;
            case VIEW_TYPE_RECV_EMOTICON:
                ReceiveEmoticonViewHolder receiveEmoticonViewHolder = (ReceiveEmoticonViewHolder) holder;
                receiveEmoticonViewHolder.renderItem(messageCache.get(position).getText(), getFormattedTime(messageList.get(position).getTime()), position);
                break;
            case VIEW_TYPE_SEND_EMOTICON:
                SendEmoticonViewHolder sendEmoticonViewHolder = (SendEmoticonViewHolder) holder;
                sendEmoticonViewHolder.renderItem(messageList.get(position).getMessage(), getFormattedTime(messageList.get(position).getTime()), messageList.get(position).getMessageStatus(), position);
        }
    }

    private boolean isAllEmoticon(String message) {
        final String emo_regex = "(^[\\u20a0-\\u32ff\\ud83c\\udc00-\\ud83d\\udeff\\udbb9\\udce5-\\udbb9\\udcee ]+$)";
        boolean isAll = Pattern.compile(emo_regex).matcher(message).find();
        return isAll;
    }

    private boolean hasProfileDP(int position) {
//        return !(position > 0 && messageList.get(position - 1).getChatId().equals(messageList.get(position - 1).getFromId()));
        return !isSameConversation(position, position+1);
    }

    private String getFormattedTime(DateTime time) {
        DateTime timeNow = DateTime.now();
        DateTimeFormatter timeFormat = DateTimeFormat.forPattern("h:mm a");
        if(timeNow.getDayOfMonth() == time.getDayOfMonth()) {
            return time.toString(timeFormat).toUpperCase();
        } else if((time.getDayOfMonth() > timeNow.getDayOfMonth()-7)) {
            return time.dayOfWeek().getAsShortText().toUpperCase()+" AT "+time.toString(timeFormat);
        } else if(timeNow.getMonthOfYear() == time.getMonthOfYear()) {
            return time.getDayOfMonth()+" "+time.monthOfYear().getAsShortText().toUpperCase()+" AT "+time.toString(timeFormat).toUpperCase();
        } else {
            return time.getYear()+", "+time.monthOfYear().getAsShortText()+" "+time.getDayOfMonth()+" AT "+time.toString(timeFormat).toUpperCase();
        }
    }

    private int bubbleType(int position) {
        final int START = 1, MIDDLE = 2, END = 3, FULL = 0;

        if(isSameConversation(position, position+1) && !isSameConversation(position, position-1))
            return START;
        else if(isSameConversation(position, position+1) && isSameConversation(position, position-1))
            return MIDDLE;
        else if(!isSameConversation(position, position+1) && isSameConversation(position, position-1))
            return END;
        else
            return FULL;
    }

    private boolean isSameConversation(int pos1, int pos2) {
        if(pos1<0 || pos1==messageList.size() || pos2<0 || pos2==messageList.size())
            return false;

        boolean isLt1Min;
        if(pos1<pos2)
            isLt1Min = messageList.get(pos1).getTime().isAfter(messageList.get(pos2).getTime().minusMinutes(1));
        else
            isLt1Min = messageList.get(pos2).getTime().isAfter(messageList.get(pos1).getTime().minusMinutes(1));

        return messageList.get(pos1).isMe() == messageList.get(pos2).isMe() && isLt1Min;
    }

    private boolean shouldShowTime(int position) {
        if (position==0)
            return true;

        return !messageList.get(position - 1).getTime().isAfter(messageList.get(position).getTime().minusMinutes(10));
    }

    private void setMessageClicked(int position, boolean isClicked) {
        Logger.d(this, "Position: "+position+", "+isClicked+" "+ lastClickedPosition);
        if(!isClicked) {
            lastClickedPosition = -1;
        } else {
            if(lastClickedPosition >= 0) {

                notifyItemChanged(lastClickedPosition);
            }
            lastClickedPosition = position;
        }
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

    class SendEmoticonViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_messageitem_message)
        TextView messageView;
        @Bind(R.id.iv_delivery_status)
        ImageView deliveryStatusView;
        @Bind(R.id.message_send_text)
        RelativeLayout layout;
        @Bind(R.id.tv_time)
        TextView timeView;
        @Bind(R.id.tv_delivery_status)
        TextView deliveryStatusText;
        @Bind(R.id.rl_bubble)
        RelativeLayout bubbleView;

        public SendEmoticonViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void renderItem(String message, String time, MessageResult.MessageStatus messageStatus, int position) {
            int bubbleType = bubbleType(position);
            boolean shouldShowTime = shouldShowTime(position);
            messageView.setText(message);

            timeView.setText(time);
            deliveryStatusText.setVisibility(View.GONE);
            if(shouldShowTime) {
                timeView.setVisibility(View.VISIBLE);
                timeView.setPadding(0,(int)AndroidUtils.px(22),0,(int)AndroidUtils.px(2));
            } else {
                timeView.setVisibility(View.GONE);
                timeView.setPadding(0,(int)AndroidUtils.px(2),0,0);
            }

            if(messageStatus == MessageResult.MessageStatus.NOT_SENT) {
                deliveryStatusView.setImageResource(R.drawable.ic_delivery_pending);
            }
            else if(messageStatus == MessageResult.MessageStatus.SENT) {
                deliveryStatusView.setImageResource(R.drawable.ic_delivery_sent);
            }
            else if(messageStatus == MessageResult.MessageStatus.READ) {
                deliveryStatusView.setImageResource(R.drawable.ic_delivery_read);
            }

            deliveryStatusText.setText(MessageResult.getDeliveryStatusText(messageStatus));

            switch (bubbleType) {
                case 0:
                    layout.setPadding(0, 0, 0, (int)context.getResources().getDimension(R.dimen.bubble_start_top_space));
                    break;
                case 1:
                    layout.setPadding(0, 0, 0, (int)context.getResources().getDimension(R.dimen.bubble_mid_top_space));
                    break;
                case 2:
                    layout.setPadding(0, 0, 0, (int)context.getResources().getDimension(R.dimen.bubble_mid_top_space));
                    break;
                case 3:
                    layout.setPadding(0, 0, 0, (int)context.getResources().getDimension(R.dimen.bubble_start_top_space));
                    break;
            }

            bubbleView.setOnClickListener(v -> {
                if(deliveryStatusText.getVisibility() == View.VISIBLE) {
                    if(!shouldShowTime) {
                        timeView.setVisibility(View.GONE);
                    }
                    deliveryStatusText.setVisibility(View.GONE);
                    setMessageClicked(position, false);
                } else {
                    timeView.setVisibility(View.VISIBLE);
                    deliveryStatusText.setVisibility(View.VISIBLE);
                    setMessageClicked(position, true);
                }
            });
        }
    }

    class ReceiveEmoticonViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_messageitem_message)
        TextView messageView;
        @Bind(R.id.iv_profileImage)
        ImageView profileImageView;
        @Bind(R.id.ll_message_receive_text)
        LinearLayout layout;
        @Bind(R.id.tv_time)
        TextView timeView;
        @Bind(R.id.rl_bubble)
        RelativeLayout bubbleView;

        ReceiveEmoticonViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void renderItem(String message, String time, int position) {
            int bubbleType = bubbleType(position);
            boolean displayProfileDP = hasProfileDP(position);
            boolean shouldShowTime = shouldShowTime(position);
            timeView.setText(time);
            messageView.setText(message);

            if(shouldShowTime) {
                timeView.setVisibility(View.VISIBLE);
                timeView.setPadding(0,(int)AndroidUtils.px(22),0,(int)AndroidUtils.px(2));
            } else {
                timeView.setVisibility(View.GONE);
                timeView.setPadding(0,(int)AndroidUtils.px(2),0,0);
            }

            if(displayProfileDP) {
                profileImageView.setVisibility(View.VISIBLE);
                profileImageView.setImageDrawable(textProfileDrawable);
            } else {
                profileImageView.setVisibility(View.INVISIBLE);
            }

            switch (bubbleType) {
                case 0:
                    layout.setPadding(0, 0, 0, (int)context.getResources().getDimension(R.dimen.bubble_start_top_space));
                    break;
                case 1:
                    layout.setPadding(0, 0, 0, (int)context.getResources().getDimension(R.dimen.bubble_mid_top_space));
                    break;
                case 2:
                    layout.setPadding(0, 0, 0, (int)context.getResources().getDimension(R.dimen.bubble_mid_top_space));
                    break;
                case 3:
                    layout.setPadding(0, 0, 0, (int)context.getResources().getDimension(R.dimen.bubble_start_top_space));
                    break;
            }

            bubbleView.setOnClickListener(v -> {
                if(!shouldShowTime) {
                    if(timeView.getVisibility() == View.VISIBLE) {
                        timeView.setVisibility(View.GONE);
                        setMessageClicked(position, false);
                    } else {
                        timeView.setVisibility(View.VISIBLE);
                        setMessageClicked(position, true);
                    }
                }
            });
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
        @Bind(R.id.tv_time)
        TextView timeView;
        @Bind(R.id.tv_delivery_status)
        TextView deliveryStatusText;

        SendTextViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void renderItem(String message, String time, MessageResult.MessageStatus messageStatus, int position) {
            int bubbleType = bubbleType(position);
            boolean shouldShowTime = shouldShowTime(position);

            timeView.setText(time);
            deliveryStatusText.setVisibility(View.GONE);
            if(shouldShowTime) {
                timeView.setVisibility(View.VISIBLE);
                timeView.setPadding(0, (int)AndroidUtils.px(22),0,(int)AndroidUtils.px(2));
            } else {
                timeView.setVisibility(View.GONE);
                timeView.setPadding(0,(int)AndroidUtils.px(2),0,0);
            }

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
            }
            else if(messageStatus == MessageResult.MessageStatus.SENT) {
                deliveryStatusView.setImageResource(R.drawable.ic_delivery_sent);
            }
            else if(messageStatus == MessageResult.MessageStatus.READ) {
                deliveryStatusView.setImageResource(R.drawable.ic_delivery_read);
            }

            deliveryStatusText.setText(MessageResult.getDeliveryStatusText(messageStatus));

            bubbleView.setOnClickListener(v -> {
                if(deliveryStatusText.getVisibility() == View.VISIBLE) {
                    if(!shouldShowTime) {
                        timeView.setVisibility(View.GONE);
                    }
                    deliveryStatusText.setVisibility(View.GONE);
                    setMessageClicked(position, false);
                } else {
                    timeView.setVisibility(View.VISIBLE);
                    deliveryStatusText.setVisibility(View.VISIBLE);
                    setMessageClicked(position, true);
                }
            });
        }
    }

    class ReceiveTextViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_messageitem_message)
        TextView messageView;
        @Bind(R.id.iv_profileImage)
        ImageView profileImageView;
        @Bind(R.id.rl_bubble)
        RelativeLayout bubbleView;
        @Bind(R.id.ll_message_receive_text)
        LinearLayout bubbleLayout;
        @Bind(R.id.tv_time)
        TextView timeView;

        ReceiveTextViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void renderItem(String message, String time, int position) {
            int bubbleType = bubbleType(position);
            boolean displayProfileDP = hasProfileDP(position);
            boolean shouldShowTime = shouldShowTime(position);

            timeView.setText(time);
            if(shouldShowTime) {
                timeView.setVisibility(View.VISIBLE);
                timeView.setPadding(0,(int)AndroidUtils.px(22),0,(int)AndroidUtils.px(2));
            } else {
                timeView.setVisibility(View.GONE);
                timeView.setPadding(0,(int)AndroidUtils.px(2),0,0);
            }

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

            if(displayProfileDP) {
                profileImageView.setVisibility(View.VISIBLE);
                profileImageView.setImageDrawable(textProfileDrawable);
            } else {
                profileImageView.setVisibility(View.INVISIBLE);
            }

            bubbleView.setOnClickListener(v -> {
                if(!shouldShowTime) {
                    if(timeView.getVisibility() == View.VISIBLE) {
                        timeView.setVisibility(View.GONE);
                        setMessageClicked(position, false);
                    } else {
                        timeView.setVisibility(View.VISIBLE);
                        setMessageClicked(position, true);
                    }
                }
            });
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

        void renderItem(GenericTemplate genericTemplate, int position) {
            int bubbleType = bubbleType(position);
            boolean displayProfileDP = hasProfileDP(position);

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
                        Glide.with(context)
                                .load(genericTemplate.getImageUrl())
                                .bitmapTransform(new RoundedCornerTransformation(context, 10, 0, RoundedCornerTransformation.CornerType.TOP_LEFT))
                                .bitmapTransform(new RoundedCornerTransformation(context, 18, 0, RoundedCornerTransformation.CornerType.TOP_RIGHT))
                                .into(templateImage);
                    }
                    break;
                case 3:
                    bubbleLayout.setPadding(0, 0, 0, (int)context.getResources().getDimension(R.dimen.bubble_start_top_space));
                    bubble.setBackgroundResource(R.drawable.bg_template_bottom);
                    if(!genericTemplate.getImageUrl().isEmpty() && genericTemplate.getImageUrl()!=null) {
                        Glide.with(context)
                                .load(genericTemplate.getImageUrl())
                                .bitmapTransform(new RoundedCornerTransformation(context, 10, 0, RoundedCornerTransformation.CornerType.TOP_LEFT))
                                .bitmapTransform(new RoundedCornerTransformation(context, 18, 0, RoundedCornerTransformation.CornerType.TOP_RIGHT))
                                .into(templateImage);
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
            if(displayProfileDP) {
                profileImage.setVisibility(View.VISIBLE);
                profileImage.setImageDrawable(textProfileDrawable);
            }
            else {
                profileImage.setVisibility(View.INVISIBLE);
            }
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

        void renderItem(ButtonTemplate buttonTemplate, int position) {
            int bubbleType = bubbleType(position);
            boolean displayDP = hasProfileDP(position);
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

            if(buttonTemplate.getButtons().size()>=3) {
                buttonLayout.setOrientation(LinearLayout.VERTICAL);
            } else {
                buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
            }


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
            if(displayDP) {
                profileImage.setVisibility(View.VISIBLE);
                profileImage.setImageDrawable(textProfileDrawable);
            } else {
                profileImage.setVisibility(View.INVISIBLE);
            }
        }
    }

    interface PostbackClickListener {
        void sendPostbackMessage(String message);
    }

    interface UrlClickListener {
        void urlButtonClicked(String url);
    }
}