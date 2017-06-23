package com.chat.ichat.screens.message;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.resource.SimpleResource;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.chat.ichat.components.AudioMessageView;
import com.chat.ichat.core.lib.CircleTransformation;
import com.chat.ichat.core.lib.RoundedCornerTransformation;
import com.chat.ichat.models.ImageMessage;
import com.chat.ichat.models.LocationMessage;
import com.chat.ichat.screens.image_viewer.ImageViewerActivity;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.JsonSyntaxException;
import com.chat.ichat.R;
import com.chat.ichat.config.AnalyticsConstants;
import com.chat.ichat.core.GsonProvider;
import com.chat.ichat.core.Logger;
import com.chat.ichat.core.lib.AndroidUtils;
import com.chat.ichat.core.lib.ImageUtils;
import com.chat.ichat.db.MessageStore;
import com.chat.ichat.models.ButtonTemplate;
import com.chat.ichat.models.GenericTemplate;
import com.chat.ichat.models.Message;
import com.chat.ichat.models.MessageResult;
import com.chat.ichat.models.QuickReply;
import com.chat.ichat.models._Button;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
/**
 * Created by vidhun on 07/08/16.
 */
public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    //TODO: Change to cursor recycler view adapter.
    private Context context;
    private List<MessageResult> messageList;
    private SparseArray<Message> messageCache;
    private List<QuickReply> quickReplies;
    private String chatUserName;

    private int lastPosition = -1;

    private final int VIEW_TYPE_SEND_TEXT = 0;
    private final int VIEW_TYPE_RECV_TEXT = 1;
    private final int VIEW_TYPE_RECV_TEMPLATE_GENERIC = 2;
    private final int VIEW_TYPE_RECV_TEMPLATE_BUTTON = 3;
    private final int VIEW_TYPE_QUICK_REPLIES = 4;
    private final int VIEW_TYPE_SEND_EMOTICON = 5;
    private final int VIEW_TYPE_RECV_EMOTICON = 6;
    private final int VIEW_TYPE_SEND_LOCATION = 7;
    private final int VIEW_TYPE_RECV_LOCATION = 9;
    private final int VIEW_TYPE_TYPING = 8;
    private final int VIEW_TYPE_SEND_IMAGE = 10;
    private final int VIEW_TYPE_RECV_IMAGE = 11;
    private final int VIEW_TYPE_SEND_AUDIO = 12;
    private final int VIEW_TYPE_RECV_AUDIO = 13;
    private final int VIEW_TYPE_NO_MESSAGES = 14;
    private final int VIEW_TYPE_CONTACT_UNKNOWN_FOOTER = 15;

    private PostbackClickListener postbackClickListener;
    private UrlClickListener urlClickListener;
    private QuickReplyActionListener quickReplyActionListener;
    private Drawable textProfileDrawable;
    private DrawableRequestBuilder dp;
    private boolean isContactAdded;
    private boolean isAllMessageReceived;
    private ContactActionListener contactActionListener;

    private boolean isTyping;
    private int lastClickedPosition;
    private Animation insertAnimation;

    private FirebaseAnalytics firebaseAnalytics;
    public MessagesAdapter(Context context, String chatUserName, String chatContactName, String dpUrl, boolean isContactAdded, PostbackClickListener postbackClickListener, UrlClickListener urlClickListener, QuickReplyActionListener qrActionListener, ContactActionListener contactActionListener) {
        this.postbackClickListener = postbackClickListener;
        this.urlClickListener = urlClickListener;
        this.quickReplyActionListener = qrActionListener;
        this.contactActionListener = contactActionListener;
        insertAnimation = AnimationUtils.loadAnimation(context, R.anim.push_bottom_up);

        this.context = context;
        this.messageList = new ArrayList<>();
        this.messageCache = new SparseArray<>();
        this.textProfileDrawable = ImageUtils.getDefaultProfileImage(chatContactName, chatUserName, 16);
        this.chatUserName = chatUserName;
        this.isTyping = false;
        this.isContactAdded = isContactAdded;

        if(dpUrl!=null && !dpUrl.isEmpty()) {
            dp = Glide.with(context)
                    .load(dpUrl.replace("https://", "http://"))
                    .crossFade()
                    .bitmapTransform(new CenterCrop(context), new CircleTransformation(context))
                    .diskCacheStrategy(DiskCacheStrategy.ALL);
        }

        lastClickedPosition=-1;
        this.firebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    public void setMessages(List<MessageResult> messages) {
        lastPosition = messages.size() - 1;
        Logger.d(this, "setting messages "+messages.size());
        quickReplies = new ArrayList<>();
        this.messageList.clear();
        this.messageList.addAll(messages);
        if(messageList.size()>0)
            this.notifyItemRangeChanged(0, messageList.size() - 1);
        else
            this.notifyItemChanged(0);
        setQuickReplies();

        if(!isContactAdded && messages.size()==0)
            contactActionListener.showAddBlock();

        isAllMessageReceived = true;
        for (MessageResult message : messages) {
            if(message.isMe()) {
                isAllMessageReceived = false;
                if(!isContactAdded)
                    contactActionListener.showAddBlock();
            }
        }
    }

    public void setContactAdded(boolean a) {
        this.isContactAdded = a;
        notifyDataSetChanged();
    }

    public boolean isAllMessageReceived() {
        return isAllMessageReceived;
    }

    public void addMessage(MessageResult messageResult) {
        this.setTyping(false);
        if(quickReplies!=null && quickReplies.size()>0) {
            this.notifyItemRemoved(messageList.size());
            quickReplies = null;
        }
        messageList.add(messageResult);
        this.notifyItemInserted(messageList.size()-1);
        this.notifyItemChanged(messageList.size()-2);
        setQuickReplies();
        if(messageResult.isMe()) {
            if(!isContactAdded)
                contactActionListener.showAddBlock();
            isAllMessageReceived = false;
            if(!isTyping && messageCache.get(messageList.size()-1).getQuickReplies()==null) {
                this.notifyItemChanged(messageList.size());
            } else {
                this.notifyItemChanged(messageList.size()+1);
            }
            this.notifyItemChanged(0);
        }
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

    public void updateDeliveryStatus(String messageId, String deliveryReceiptId, MessageResult.MessageStatus messageStatus) {
        Logger.d(this, "MessgeAdapter, updating."+messageStatus+", "+deliveryReceiptId+" "+messageId);
        // TODO: Might be inefficient
        MessageResult m;
        boolean isBeforeReceiptId = false;
        for(int i=messageList.size()-1; i>=0; i--) {
            m = messageList.get(i);
            if (m.getMessageStatus() == MessageResult.MessageStatus.READ) {
                return;
            }
            if (messageStatus == MessageResult.MessageStatus.NOT_SENT) {
                return;
            } else if (messageStatus == MessageResult.MessageStatus.SENT) {
                if (m.getMessageStatus() == MessageResult.MessageStatus.READ) {
                    return;
                }
            } else if(messageStatus == MessageResult.MessageStatus.DELIVERED) {
                messageStatus = MessageResult.MessageStatus.SENT;
            }
            if(m.getReceiptId()!=null && !m.getReceiptId().isEmpty() && m.getReceiptId().equals(deliveryReceiptId)) {
                isBeforeReceiptId = true;
            } else if(m.getReceiptId() == null) {
                if(!messageId.isEmpty() && m.getMessageId().equals(messageId)) {
                    m.setReceiptId(deliveryReceiptId);
                    m.setMessageStatus(messageStatus);
                    messageList.set(i, m);
                    this.notifyItemChanged(i);
                }
            }
            if(isBeforeReceiptId) {
                if(!m.getChatId().equals(m.getFromId())) {
                    Logger.d(this, "Setting message status[Adapter] "+messageStatus);
                    m.setMessageStatus(messageStatus);
                    messageList.set(i, m);
                    this.notifyItemChanged(i);
                }
            }
        }
    }

    public void setTyping(boolean isTyping) {
        if(this.isTyping == isTyping)
            return;
        this.isTyping = isTyping;
        this.notifyItemChanged(messageList.size()-1);

        if (quickReplies != null && quickReplies.size() >= 1) {
            this.notifyItemChanged(messageList.size());
        } else {
            if(isTyping) {
                this.notifyItemInserted(messageList.size());
            } else {
                this.notifyItemRemoved(messageList.size());
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(messageList.size() == 0) {
            return VIEW_TYPE_NO_MESSAGES;
        }
        if(position == messageList.size()) {
            if(isTyping) {
                return VIEW_TYPE_TYPING;
            } else if(messageCache.get(messageList.size()-1).getQuickReplies()!=null){
                return VIEW_TYPE_QUICK_REPLIES;
            } else {
                return VIEW_TYPE_CONTACT_UNKNOWN_FOOTER;
            }
        }
        if(position == messageList.size()+1) {
            return VIEW_TYPE_CONTACT_UNKNOWN_FOOTER;
        }

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

        if(messageList.get(position).isMe()) { // SEND MESSAGE TYPES
            if(parsedMessage.getMessageType() == Message.MessageType.text) {
                if(isAllEmoticon(parsedMessage.getText())) {
                    return VIEW_TYPE_SEND_EMOTICON;
                } else {
                    return VIEW_TYPE_SEND_TEXT;
                }
            } else if(parsedMessage.getMessageType() == Message.MessageType.location) {
                return VIEW_TYPE_SEND_LOCATION;
            } else if(parsedMessage.getMessageType() == Message.MessageType.image) {
                return VIEW_TYPE_SEND_IMAGE;
            }  else if(parsedMessage.getMessageType() == Message.MessageType.audio) {
                return VIEW_TYPE_SEND_AUDIO;
            } else if(parsedMessage.getMessageType() == Message.MessageType.unknown) {
                parsedMessage = new Message();
                parsedMessage.setText(messageList.get(position).getMessage());
                messageCache.put(position, parsedMessage);
                if(isAllEmoticon(parsedMessage.getText())) {
                    return VIEW_TYPE_SEND_EMOTICON;
                } else {
                    return VIEW_TYPE_SEND_TEXT;
                }
            }
        } else { // RECEIVE MESSAGE TYPES
            if(parsedMessage.getMessageType() == Message.MessageType.generic_template) {
                return VIEW_TYPE_RECV_TEMPLATE_GENERIC;
            } else if(parsedMessage.getMessageType() == Message.MessageType.button_template) {
                return VIEW_TYPE_RECV_TEMPLATE_BUTTON;
            } else if(parsedMessage.getMessageType() == Message.MessageType.text) {
                if(isAllEmoticon(parsedMessage.getText())) {
                    return VIEW_TYPE_RECV_EMOTICON;
                } else {
                    return VIEW_TYPE_RECV_TEXT;
                }
            } else if(parsedMessage.getMessageType() == Message.MessageType.location) {
                return VIEW_TYPE_RECV_LOCATION;
            } else if(parsedMessage.getMessageType() == Message.MessageType.image) {
                return VIEW_TYPE_RECV_IMAGE;
            }   else if(parsedMessage.getMessageType() == Message.MessageType.audio) {
                return VIEW_TYPE_RECV_AUDIO;
            } else if(parsedMessage.getMessageType() == Message.MessageType.unknown) {
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
        Logger.d(this, "isadded"+isContactAdded+", "+isAllMessageReceived);
        int add = !isContactAdded && isAllMessageReceived?1:0;
        Logger.d(this, "Add: "+add);
        if(messageList.size() == 0) {
            return 1 + add;
        }
        if(quickReplies!=null && quickReplies.size()>=1 || isTyping)
            return messageList.size()+1 + add;
        return messageList.size() + add;
    }

    @Override
    public long getItemId(int position) {
        if(messageList.size()>0)
            return  messageList.get(position).getTime().getMillis();
        else
            return 1;
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
                View view3 = inflater.inflate(R.layout.item_message_receive_template, parent, false);
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
            case VIEW_TYPE_SEND_LOCATION:
                View view9 = inflater.inflate(R.layout.item_message_send_location, parent, false);
                viewHolder = new SendLocationViewHolder(view9);
                break;
            case VIEW_TYPE_TYPING:
                View view8 = inflater.inflate(R.layout.item_typing, parent, false);
                viewHolder = new TypingViewHolder(view8);
                break;
            case VIEW_TYPE_RECV_LOCATION:
                View view10 = inflater.inflate(R.layout.item_message_receive_location, parent, false);
                viewHolder = new ReceiveLocationViewHolder(view10);
                break;
            case VIEW_TYPE_SEND_IMAGE:
                View view11 = inflater.inflate(R.layout.item_message_send_image, parent, false);
                viewHolder = new SendImageViewHolder(view11);
                break;
            case VIEW_TYPE_RECV_IMAGE:
                View view12 = inflater.inflate(R.layout.item_message_receive_image, parent, false);
                viewHolder = new ReceiveImageViewHolder(view12);
                break;
            case VIEW_TYPE_SEND_AUDIO:
                View view13 = inflater.inflate(R.layout.item_message_send_audio, parent, false);
                viewHolder = new SendAudioViewHolder(view13);
                break;
            case VIEW_TYPE_RECV_AUDIO:
                View view14 = inflater.inflate(R.layout.item_message_receive_audio, parent, false);
                viewHolder = new ReceiveAudioViewHolder(view14);
                break;
            case VIEW_TYPE_NO_MESSAGES:
                View view15 = inflater.inflate(R.layout.item_message_no_messages, parent, false);
                viewHolder = new NoMessagesViewHolder(view15);
                break;
            case VIEW_TYPE_CONTACT_UNKNOWN_FOOTER:
                View view16 = inflater.inflate(R.layout.item_message_info_unknown, parent, false);
                viewHolder = new NoContactViewHolderFooter(view16);
                break;
//            case VIEW_TYPE_CONTACT_UNKNOWN_HEADER:
//                View view17 = inflater.inflate(R.layout.item_message_info_unknown_header, parent, false);
//                viewHolder = new NoContactViewHolderHeader(view17);
//                break;
            default:
                return null;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (quickReplies!=null && messageList!=null && position > lastPosition && messageList.size()>0 && quickReplies.size()==0 && getItemViewType(position)!=VIEW_TYPE_CONTACT_UNKNOWN_FOOTER) {

            holder.itemView.startAnimation(insertAnimation);
            lastPosition = position;
        }
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_SEND_TEXT:
                SendTextViewHolder sendViewHolder = (SendTextViewHolder) holder;
                sendViewHolder.renderItem(messageCache.get(position).getText(), getFormattedTime(messageList.get(position).getTime()), messageList.get(position).getMessageStatus(), position);
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
                qrVH.renderItem(messageCache.get(position-1).getQuickReplies());
                break;
            case VIEW_TYPE_RECV_EMOTICON:
                ReceiveEmoticonViewHolder receiveEmoticonViewHolder = (ReceiveEmoticonViewHolder) holder;
                receiveEmoticonViewHolder.renderItem(messageCache.get(position).getText(), getFormattedTime(messageList.get(position).getTime()), position);
                break;
            case VIEW_TYPE_SEND_EMOTICON:
                SendEmoticonViewHolder sendEmoticonViewHolder = (SendEmoticonViewHolder) holder;
                sendEmoticonViewHolder.renderItem(messageCache.get(position).getText(), getFormattedTime(messageList.get(position).getTime()), messageList.get(position).getMessageStatus(), position);
                break;
            case VIEW_TYPE_SEND_LOCATION:
                SendLocationViewHolder sendLocationViewHolder = (SendLocationViewHolder) holder;
                sendLocationViewHolder.renderItem(messageCache.get(position).getLocationMessage(), getFormattedTime(messageList.get(position).getTime()), messageList.get(position).getMessageStatus(), position);
                break;
            case VIEW_TYPE_RECV_LOCATION:
                ReceiveLocationViewHolder receiveLocationViewHolder = (ReceiveLocationViewHolder) holder;
                receiveLocationViewHolder.renderItem(messageCache.get(position).getLocationMessage(), getFormattedTime(messageList.get(position).getTime()), position);
                break;
            case VIEW_TYPE_SEND_IMAGE:
                SendImageViewHolder sendImageViewHolder = (SendImageViewHolder) holder;
                if(messageCache.get(position).getImageMessage().getDataType() == ImageMessage.ImageType.gif) {
                    Logger.d(this, "Gif");
                    sendImageViewHolder.renderGif(messageCache.get(position).getImageMessage().getImageUrl(), messageCache.get(position).getImageMessage().getWidth(), messageCache.get(position).getImageMessage().getHeight(), getFormattedTime(messageList.get(position).getTime()), messageList.get(position).getMessageStatus(), position);
                } else {
                    Logger.d(this, "Image");
                    sendImageViewHolder.renderImage(new File(messageCache.get(position).getImageMessage().getFileUri()), getFormattedTime(messageList.get(position).getTime()), messageList.get(position).getMessageStatus(), position);
                }
                break;
            case VIEW_TYPE_RECV_IMAGE:
                ReceiveImageViewHolder receiveImageViewHolder = (ReceiveImageViewHolder) holder;
                if(messageCache.get(position).getImageMessage().getDataType() == ImageMessage.ImageType.gif) {
                    receiveImageViewHolder.renderGif(messageCache.get(position).getImageMessage().getImageUrl(), messageCache.get(position).getImageMessage().getWidth(), messageCache.get(position).getImageMessage().getHeight(), getFormattedTime(messageList.get(position).getTime()), position);
                } else {
                    receiveImageViewHolder.renderItem(messageCache.get(position).getImageMessage().getImageUrl(), getFormattedTime(messageList.get(position).getTime()), position);
                }
                break;
            case VIEW_TYPE_SEND_AUDIO:
                SendAudioViewHolder sendAudioViewHolder = (SendAudioViewHolder) holder;
                sendAudioViewHolder.renderItem(messageCache.get(position).getAudioMessage().getFileUri(), getFormattedTime(messageList.get(position).getTime()), messageList.get(position).getMessageStatus(), position);
                break;
            case VIEW_TYPE_RECV_AUDIO:
                ReceiveAudioViewHolder receiveAudioViewHolder = (ReceiveAudioViewHolder) holder;
                receiveAudioViewHolder.renderItem(messageCache.get(position).getAudioMessage().getAudioUrl(), getFormattedTime(messageList.get(position).getTime()), position);
                break;
            case VIEW_TYPE_TYPING:
                TypingViewHolder typingViewHolder = (TypingViewHolder) holder;
                typingViewHolder.renderItem();
                break;
            case VIEW_TYPE_NO_MESSAGES:
                NoMessagesViewHolder noMessagesViewHolder = (NoMessagesViewHolder) holder;
                noMessagesViewHolder.renderItem("No messages here yet...");
                break;
            case VIEW_TYPE_CONTACT_UNKNOWN_FOOTER:
                NoContactViewHolderFooter noContactViewHolderFooter = (NoContactViewHolderFooter) holder;
                noContactViewHolderFooter.renderItem();
                break;
//            case VIEW_TYPE_CONTACT_UNKNOWN_HEADER:
//                NoContactViewHolderHeader noContactViewHolderHeader = (NoContactViewHolderHeader) holder;
//                noContactViewHolderHeader.renderItem();
//                break;
        }
    }

    private boolean isAllEmoticon(String message) {
        final String emo_regex = "(^[\\u20a0-\\u32ff\\ud83c\\udc00-\\ud83d\\udeff\\udbb9\\udce5-\\udbb9\\udcee ]+$)";
        return Pattern.compile(emo_regex).matcher(message).find();
    }

    private boolean hasProfileDP(int position) {
        return !(position == (messageList.size() - 1) && isTyping) && !isSameConversation(position, position + 1);
    }

    private String getFormattedTime(DateTime time) {
        DateTime timeNow = DateTime.now();
        DateTimeFormatter timeFormat = DateTimeFormat.forPattern("h:mm a");
        if(timeNow.getDayOfMonth() == time.getDayOfMonth()) {
            return time.toString(timeFormat).toUpperCase();
        } else if((time.getDayOfMonth() > timeNow.getDayOfMonth()-7)) {
            return time.dayOfWeek().getAsShortText().toUpperCase()+" AT "+time.toString(timeFormat).toUpperCase();
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
        return position == 0 || !messageList.get(position - 1).getTime().isAfter(messageList.get(position).getTime().minusMinutes(10));

    }

    private void toggleMessagePressed(int position, boolean isClicked) {
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

    private boolean isMessagePressed(int position) {
        return lastClickedPosition == position && lastClickedPosition>=0;
    }

    private void handleMessageClick(RecyclerView.ViewHolder viewHolder, int position, String text) {
        Bundle bundle = new Bundle();
        bundle.putString(AnalyticsConstants.Param.RECIPIENT_USER_NAME, this.chatUserName);
        bundle.putString(AnalyticsConstants.Param.MESSAGE, text);
        firebaseAnalytics.logEvent(AnalyticsConstants.Event.MESSAGE_CLICK_MSG, null);
    }

    private void handleMessageLongClick(RecyclerView.ViewHolder viewHolder, int position, String text) {
        Bundle bundle = new Bundle();
        bundle.putString(AnalyticsConstants.Param.RECIPIENT_USER_NAME, this.chatUserName);
        bundle.putString(AnalyticsConstants.Param.MESSAGE, text);
        firebaseAnalytics.logEvent(AnalyticsConstants.Event.MESSAGE_LONG_CLICK_MSG, bundle);

        TextView copyTextAction, deleteAction, detailsAction;
        LinearLayout parent = new LinearLayout(context);

        parent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
        parent.setOrientation(LinearLayout.VERTICAL);
        parent.setPadding((int)AndroidUtils.px(24), (int)AndroidUtils.px(8), 0, (int)AndroidUtils.px(8));

        copyTextAction = new TextView(context);
        copyTextAction.setText("Copy Text");
        copyTextAction.setTextColor(ContextCompat.getColor(context, R.color.textColor));
        copyTextAction.setTextSize(16);
        copyTextAction.setHeight((int)AndroidUtils.px(48));
        copyTextAction.setGravity(Gravity.CENTER_VERTICAL);

        deleteAction = new TextView(context);
        deleteAction.setText("Delete");
        deleteAction.setTextColor(ContextCompat.getColor(context, R.color.textColor));
        deleteAction.setTextSize(16);
        deleteAction.setHeight((int)AndroidUtils.px(48));
        deleteAction.setGravity(Gravity.CENTER_VERTICAL);

        detailsAction = new TextView(context);
        detailsAction.setText("Details");
        detailsAction.setTextColor(ContextCompat.getColor(context, R.color.textColor));
        detailsAction.setTextSize(16);
        detailsAction.setHeight((int)AndroidUtils.px(48));
        detailsAction.setGravity(Gravity.CENTER_VERTICAL);

        parent.addView(copyTextAction);
        parent.addView(deleteAction);
        parent.addView(detailsAction);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Message");
        builder.setView(parent);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        alertDialog.setOnDismissListener(dialog -> {
            firebaseAnalytics.logEvent(AnalyticsConstants.Event.POPUP_MESSAGE_ACTION_DISMISS, bundle);
        });

        copyTextAction.setOnClickListener(v -> {
            firebaseAnalytics.logEvent(AnalyticsConstants.Event.POPUP_MESSAGE_ACTION_COPY, bundle);
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("messageText", text);
            clipboard.setPrimaryClip(clip);
            alertDialog.dismiss();
        });
        deleteAction.setOnClickListener(v -> {
            firebaseAnalytics.logEvent(AnalyticsConstants.Event.POPUP_MESSAGE_ACTION_DELETE, bundle);
            Logger.d(this, "Deleting message at:" +position);
            MessageStore.getInstance().deleteMessage(messageList.get(position).getReceiptId())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<Boolean>() {
                        @Override
                        public void onCompleted() {}
                        @Override
                        public void onError(Throwable e) {}

                        @Override
                        public void onNext(Boolean aBoolean) {
                        }
                    });
            messageList.remove(position);
            messageCache.remove(position);
            notifyDataSetChanged();
            alertDialog.dismiss();
        });
        detailsAction.setOnClickListener(v -> {
            firebaseAnalytics.logEvent(AnalyticsConstants.Event.POPUP_MESSAGE_ACTION_DETAILS, bundle);
            if(viewHolder instanceof SendTextViewHolder) {
                ((SendTextViewHolder) viewHolder).onMessageClicked();
            }
            alertDialog.dismiss();
        });
    }

    class NoContactViewHolderFooter extends RecyclerView.ViewHolder {
        NoContactViewHolderFooter(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void renderItem() {
        }

        @OnClick(R.id.tv_line2)
        public void onReportSpamClicked() {
            contactActionListener.onContactReportSpamClicked();
        }

        @OnClick(R.id.tv_line3)
        public void onBlockClicked() {
            contactActionListener.onContactBlockedClicked();
        }

        @OnClick(R.id.tv_line4)
        public void onAddContactClicked() {
            contactActionListener.onContactAddClicked();
        }
    }

//    class NoContactViewHolderHeader extends RecyclerView.ViewHolder {
//        NoContactViewHolderHeader(View itemView) {
//            super(itemView);
//        }
//
//        void renderItem() {
//        }
//    }

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
            quickRepliesListView.setAdapter(new QuickRepliesAdapter(context, postbackClickListener, quickReplyActionListener, quickReplies));
            OverScrollDecoratorHelper.setUpOverScroll(quickRepliesListView, OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL);
        }
    }

    class NoMessagesViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_info)
        TextView info;

        NoMessagesViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void renderItem(String text) {
            info.setText(text);
        }
    }

    class ReceiveTemplateGenericViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.rv_template)
        RecyclerView templateView;
        @Bind(R.id.iv_profileImage)
        ImageView profileImage;
        @Bind(R.id.fl_profileImageLayout)
        FrameLayout profileImageLayout;

        ReceiveTemplateGenericViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void renderItem(List<GenericTemplate> genericTemplates, int position) {
            int bubbleType = bubbleType(position);
            boolean displayProfileDP = hasProfileDP(position);

            if(displayProfileDP) {
                profileImage.setVisibility(View.VISIBLE);
                if(dp!=null) {
                    dp.into(profileImage);
                } else {
                    profileImage.setImageDrawable(textProfileDrawable);
                }
            } else {
                profileImage.setVisibility(View.INVISIBLE);
            }

            switch (bubbleType) {
                case 0:
                    profileImageLayout.setPadding(0, 0, 0, (int)context.getResources().getDimension(R.dimen.bubble_start_top_space));
                    break;
                case 1:
                    profileImageLayout.setPadding(0, 0, 0, (int)context.getResources().getDimension(R.dimen.bubble_mid_top_space));
                    break;
                case 2:
                    profileImageLayout.setPadding(0, 0, 0, (int)context.getResources().getDimension(R.dimen.bubble_mid_top_space));
                    break;
                case 3:
                    profileImageLayout.setPadding(0, 0, 0, (int)context.getResources().getDimension(R.dimen.bubble_start_top_space));
                    break;
            }

            LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
            templateView.setLayoutManager(layoutManager);
            templateView.setAdapter(new GenericTemplateAdapter(context, genericTemplates, bubbleType(position), postbackClickListener, urlClickListener));
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

        private int position;

        SendEmoticonViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void renderItem(String message, String time, MessageResult.MessageStatus messageStatus, int position) {
            this.position = position;
            int bubbleType = bubbleType(position);
            boolean shouldShowTime = shouldShowTime(position);
            messageView.setText(message);

            timeView.setText(time);
            deliveryStatusText.setVisibility(View.GONE);
            if(shouldShowTime) {
                timeView.setVisibility(View.VISIBLE);
                timeView.setPadding(0, (int)AndroidUtils.px(15.75f),0,(int)AndroidUtils.px(9f));
            } else {
                timeView.setVisibility(View.GONE);
                timeView.setPadding(0,0 ,0,(int)AndroidUtils.px(2));
            }

            if(messageStatus == MessageResult.MessageStatus.NOT_SENT) {
                deliveryStatusView.setImageResource(R.drawable.ic_delivery_pending);
            } else if(messageStatus == MessageResult.MessageStatus.SENT) {
                deliveryStatusView.setImageResource(R.drawable.ic_delivery_sent);
            } else if(messageStatus == MessageResult.MessageStatus.DELIVERED) {
                deliveryStatusView.setImageResource(R.drawable.ic_delivery_delivered);
            } else if(messageStatus == MessageResult.MessageStatus.READ) {
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
                handleMessageClick(this, position, messageView.getText().toString());
                if(deliveryStatusText.getVisibility() == View.VISIBLE) {
                    if(!shouldShowTime) {
                        timeView.setVisibility(View.GONE);
                    }
                    deliveryStatusText.setVisibility(View.GONE);
                    toggleMessagePressed(position, false);
                } else {
                    timeView.setVisibility(View.VISIBLE);
                    deliveryStatusText.setVisibility(View.VISIBLE);
                    toggleMessagePressed(position, true);
                }
            });
        }

        @OnLongClick(R.id.rl_bubble)
        public boolean onMessageLongClicked() {
            handleMessageLongClick(this, position, messageView.getText().toString());
            return true;
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

        private int postition;

        ReceiveEmoticonViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void renderItem(String message, String time, int position) {
            this.postition = position;
            int bubbleType = bubbleType(position);
            boolean displayProfileDP = hasProfileDP(position);
            boolean shouldShowTime = shouldShowTime(position);
            timeView.setText(time);
            messageView.setText(message);

            if(shouldShowTime) {
                timeView.setVisibility(View.VISIBLE);
                timeView.setPadding(0, (int)AndroidUtils.px(15.75f),0,(int)AndroidUtils.px(9f));
            } else {
                timeView.setVisibility(View.GONE);
                timeView.setPadding(0,0 ,0,(int)AndroidUtils.px(2));
            }

            if(displayProfileDP) {
                profileImageView.setVisibility(View.VISIBLE);
                if(dp!=null) {
                    dp.into(profileImageView);
                } else {
                    profileImageView.setImageDrawable(textProfileDrawable);
                }
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
                handleMessageClick(this, position, messageView.getText().toString());
                if(!shouldShowTime) {
                    if(timeView.getVisibility() == View.VISIBLE) {
                        timeView.setVisibility(View.GONE);
                        toggleMessagePressed(position, false);
                    } else {
                        timeView.setVisibility(View.VISIBLE);
                        toggleMessagePressed(position, true);
                    }
                }
            });
        }

        @OnLongClick(R.id.rl_bubble)
        public boolean onMessageLongClicked() {
            handleMessageLongClick(this, this.postition, messageView.getText().toString());
            return true;
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

        private int position;

        SendTextViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void renderItem(String message, String time, MessageResult.MessageStatus messageStatus, int position) {
            this.position = position;
            int bubbleType = bubbleType(position);
            boolean shouldShowTime = shouldShowTime(position);

            GradientDrawable drawable = (GradientDrawable) bubbleView.getBackground();
            drawable.setColor(ContextCompat.getColor(context, R.color.sendMessageBubble));

            timeView.setText(time);
            deliveryStatusText.setVisibility(View.GONE);
            if(shouldShowTime) {
                timeView.setVisibility(View.VISIBLE);
                timeView.setPadding(0, (int)AndroidUtils.px(15.75f),0,(int)AndroidUtils.px(9f));
            } else {
                timeView.setVisibility(View.GONE);
                timeView.setPadding(0,0 ,0,(int)AndroidUtils.px(2));
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
            } else if(messageStatus == MessageResult.MessageStatus.SENT) {
                deliveryStatusView.setImageResource(R.drawable.ic_delivery_sent);
            } else if(messageStatus == MessageResult.MessageStatus.DELIVERED) {
                deliveryStatusView.setImageResource(R.drawable.ic_delivery_delivered);
            } else if(messageStatus == MessageResult.MessageStatus.READ) {
                deliveryStatusView.setImageResource(R.drawable.ic_delivery_read);
            }

            deliveryStatusText.setText(MessageResult.getDeliveryStatusText(messageStatus));
        }

        @OnClick(R.id.rl_bubble)
        public void onMessageClicked() {
            handleMessageClick(this, position, messageView.getText().toString());
            boolean shouldShowTime = shouldShowTime(position);

            if(isMessagePressed(position)) {
                if(!shouldShowTime) {
                    timeView.setVisibility(View.GONE);
                }
                deliveryStatusText.setVisibility(View.GONE);
                toggleMessagePressed(position, false);
                GradientDrawable drawable = (GradientDrawable) bubbleView.getBackground();
                drawable.setColor(ContextCompat.getColor(context, R.color.sendMessageBubble));
            } else {
                timeView.setVisibility(View.VISIBLE);
                deliveryStatusText.setVisibility(View.VISIBLE);
                toggleMessagePressed(position, true);
                GradientDrawable drawable = (GradientDrawable) bubbleView.getBackground();
                drawable.setColor(ContextCompat.getColor(context, R.color.sendMessageBubblePressed));
            }
        }

        @OnLongClick(R.id.rl_bubble)
        public boolean onMessageLongClicked() {
            handleMessageLongClick(this, position, messageView.getText().toString());
            return true;
        }
    }

    class SendLocationViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_place_name)
        TextView placeName;
        @Bind(R.id.tv_address)
        TextView address;
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
        @Bind(R.id.location_description)
        LinearLayout locationDescription;
        @Bind(R.id.location_image)
        ImageView locationImage;

        private int position;
        private LocationMessage locationMessage;

        SendLocationViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void renderItem(LocationMessage locationMessage, String time, MessageResult.MessageStatus messageStatus, int position) {
            this.locationMessage = locationMessage;
            this.position = position;
            int bubbleType = bubbleType(position);
            boolean shouldShowTime = shouldShowTime(position);

            timeView.setText(time);
            deliveryStatusText.setVisibility(View.GONE);
            if(shouldShowTime) {
                timeView.setVisibility(View.VISIBLE);
                timeView.setPadding(0, (int)AndroidUtils.px(15.75f),0,(int)AndroidUtils.px(9f));
            } else {
                timeView.setVisibility(View.GONE);
                timeView.setPadding(0,0 ,0,(int)AndroidUtils.px(2));
            }

            Glide.with(context).load("https://maps.googleapis.com/maps/api/staticmap?markers=size:big|color:red|"+ locationMessage.getLatitude()+","+ locationMessage.getLongitude()+"&zoom=16&size=512x512&key=AIzaSyCPMaS_Gq7h09iFzLKla-UZ9-JCpp8Rgi8")
                    .bitmapTransform(new CenterCrop(context), new RoundedCornerTransformation(context, (int)context.getResources().getDimension(R.dimen.bubble_full_corner_radius), 0, RoundedCornerTransformation.CornerType.ALL))
                    .into(locationImage);
            locationImage.setBackgroundResource(R.drawable.bg_msg_send_full);

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
            placeName.setText(locationMessage.getPlaceName());
            address.setText(locationMessage.getAddress());
            if(messageStatus == MessageResult.MessageStatus.NOT_SENT) {
                deliveryStatusView.setImageResource(R.drawable.ic_delivery_pending);
            } else if(messageStatus == MessageResult.MessageStatus.SENT) {
                deliveryStatusView.setImageResource(R.drawable.ic_delivery_sent);
            } else if(messageStatus == MessageResult.MessageStatus.DELIVERED) {
                deliveryStatusView.setImageResource(R.drawable.ic_delivery_delivered);
            } else if(messageStatus == MessageResult.MessageStatus.READ) {
                deliveryStatusView.setImageResource(R.drawable.ic_delivery_read);
            }

            deliveryStatusText.setText(MessageResult.getDeliveryStatusText(messageStatus));
        }

        @OnClick(R.id.rl_bubble)
        public void onMessageClicked() {
            handleMessageClick(this, position, "");
            // Open Maps Activity
            String label = locationMessage.getPlaceName();
            String uriBegin = "geo:"+ locationMessage.getLatitude()+","+ locationMessage.getLongitude();
            String query = locationMessage.getLatitude()+","+ locationMessage.getLongitude()+"(" + label + ")";
            String encodedQuery = Uri.encode( query  );
            String uriString = uriBegin + "?q=" + encodedQuery;
            Uri uri = Uri.parse( uriString );
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(intent);
        }

        @OnLongClick(R.id.rl_bubble)
        public boolean onMessageLongClicked() {
            handleMessageLongClick(this, position, placeName.getText().toString());
            return true;
        }
    }

    class ReceiveLocationViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_place_name)
        TextView placeName;
        @Bind(R.id.tv_address)
        TextView address;
        @Bind(R.id.rl_bubble)
        RelativeLayout bubbleView;
        @Bind(R.id.ll_message_receive_text)
        LinearLayout layout;
        @Bind(R.id.tv_time)
        TextView timeView;
        @Bind(R.id.iv_profileImage)
        ImageView profileImageView;
        @Bind(R.id.location_description)
        LinearLayout locationDescription;
        @Bind(R.id.location_image)
        ImageView locationImage;

        private int position;
        private LocationMessage locationMessage;

        ReceiveLocationViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void renderItem(LocationMessage locationMessage, String time, int position) {
            this.locationMessage = locationMessage;
            this.position = position;
            int bubbleType = bubbleType(position);
            boolean shouldShowTime = shouldShowTime(position);

            timeView.setText(time);
            if(shouldShowTime) {
                timeView.setVisibility(View.VISIBLE);
                timeView.setPadding(0, (int)AndroidUtils.px(15.75f),0,(int)AndroidUtils.px(9f));
            } else {
                timeView.setVisibility(View.GONE);
                timeView.setPadding(0,0 ,0,(int)AndroidUtils.px(2));
            }

            Glide.with(context).load("https://maps.googleapis.com/maps/api/staticmap?markers=size:big|color:red|"+ locationMessage.getLatitude()+","+ locationMessage.getLongitude()+"&zoom=16&size=512x512&key=AIzaSyCPMaS_Gq7h09iFzLKla-UZ9-JCpp8Rgi8")
                    .bitmapTransform(new CenterCrop(context), new RoundedCornerTransformation(context, (int)context.getResources().getDimension(R.dimen.bubble_full_corner_radius), 0, RoundedCornerTransformation.CornerType.ALL))
                    .into(locationImage);

            switch (bubbleType) {
                case 0:
                    layout.setPadding(0, 0, 0, (int)context.getResources().getDimension(R.dimen.bubble_start_top_space));
                    locationImage.setBackgroundResource(R.drawable.bg_msg_receive_full);
                    locationDescription.setBackgroundResource(R.drawable.bg_lower_template_bottom);
                    break;
                case 1:
                    layout.setPadding(0, 0, 0, (int)context.getResources().getDimension(R.dimen.bubble_mid_top_space));
                    locationImage.setBackgroundResource(R.drawable.bg_msg_receive_full);
                    locationDescription.setBackgroundResource(R.drawable.bg_lower_template_bottom);
                    break;
                case 2:
                    layout.setPadding(0, 0, 0, (int)context.getResources().getDimension(R.dimen.bubble_mid_top_space));
                    locationImage.setBackgroundResource(R.drawable.bg_msg_receive_full);
                    locationDescription.setBackgroundResource(R.drawable.bg_lower_template_bottom);
                    break;
                case 3:
                    layout.setPadding(0, 0, 0, (int)context.getResources().getDimension(R.dimen.bubble_start_top_space));
                    locationImage.setBackgroundResource(R.drawable.bg_msg_receive_full);
                    locationDescription.setBackgroundResource(R.drawable.bg_lower_template_bottom);
                    break;
            }
            placeName.setText(locationMessage.getPlaceName());
            address.setText(locationMessage.getAddress());

            if(hasProfileDP(position)) {
                profileImageView.setVisibility(View.VISIBLE);
                if(dp!=null) {
                    dp.into(profileImageView);
                } else {
                    profileImageView.setImageDrawable(textProfileDrawable);
                }
            } else {
                profileImageView.setVisibility(View.INVISIBLE);
            }
        }

        @OnClick(R.id.rl_bubble)
        public void onMessageClicked() {
            handleMessageClick(this, position, "");
            // Open Maps Activity
            String label = locationMessage.getPlaceName();
            String uriBegin = "geo:"+ locationMessage.getLatitude()+","+ locationMessage.getLongitude();
            String query = locationMessage.getLatitude()+","+ locationMessage.getLongitude()+"(" + label + ")";
            String encodedQuery = Uri.encode( query  );
            String uriString = uriBegin + "?q=" + encodedQuery;
            Uri uri = Uri.parse( uriString );
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(intent);
        }

        @OnLongClick(R.id.rl_bubble)
        public boolean onMessageLongClicked() {
            handleMessageLongClick(this, position, placeName.getText().toString());
            return true;
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

        private int position;

        ReceiveTextViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void renderItem(String message, String time, int position) {
            this.position = position;
            int bubbleType = bubbleType(position);
            boolean displayProfileDP = hasProfileDP(position);
            boolean shouldShowTime = shouldShowTime(position);

            GradientDrawable drawable = (GradientDrawable) bubbleView.getBackground();
            drawable.setColor(ContextCompat.getColor(context, R.color.receiveMessageBubble));

            timeView.setText(time);
            if(shouldShowTime) {
                timeView.setVisibility(View.VISIBLE);
                timeView.setPadding(0, (int)AndroidUtils.px(15.5f),0,(int)AndroidUtils.px(8.75f));
            } else {
                timeView.setVisibility(View.GONE);
                timeView.setPadding(0,0 ,0,(int)AndroidUtils.px(2));
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
                if(dp!=null) {
                    dp.into(profileImageView);
                } else {
                    profileImageView.setImageDrawable(textProfileDrawable);
                }
            } else {
                profileImageView.setVisibility(View.INVISIBLE);
            }

            bubbleView.setOnClickListener(v -> {
                handleMessageClick(this, position, messageView.getText().toString());
                if(isMessagePressed(position)) {
                    if(!shouldShowTime) {
                        timeView.setVisibility(View.GONE);
                    }
                    toggleMessagePressed(position, false);
                    GradientDrawable drawable1 = (GradientDrawable) bubbleView.getBackground();
                    drawable1.setColor(ContextCompat.getColor(context, R.color.receiveMessageBubble));

                } else {
                    timeView.setVisibility(View.VISIBLE);
                    toggleMessagePressed(position, true);
                    GradientDrawable drawable2 = (GradientDrawable) bubbleView.getBackground();
                    drawable2.setColor(ContextCompat.getColor(context, R.color.receiveMessageBubblePressed));
                }
            });
        }

        @OnLongClick(R.id.rl_bubble)
        public boolean onMessageLongClicked() {
            handleMessageLongClick(this, position, messageView.getText().toString());
            return true;
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
                            postbackClickListener.sendPostbackMessage(btn.getTitle(), btn.getPayload());
                        else if(urlClickListener!=null && btn.getType() == _Button.Type.web_url)
                            urlClickListener.urlButtonClicked(btn.getUrl());
                    });
                }
            }

            if(displayDP) {
                profileImage.setVisibility(View.VISIBLE);

                if(dp!=null) {
                    dp.into(profileImage);
                } else {
                    profileImage.setImageDrawable(textProfileDrawable);
                }

            } else {
                profileImage.setVisibility(View.INVISIBLE);
            }
        }
    }

    class TypingViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.iv_profileImage)
        ImageView profileImageView;
        @Bind(R.id.rl_bubble)
        RelativeLayout bubbleView;
        @Bind(R.id.iv_typing)
        ImageView typingImage;
        @Bind(R.id.ll_message_receive_text)
        LinearLayout layout;

        TypingViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void renderItem() {
            GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(typingImage);
            Glide.with(context).load(R.raw.loading_dots).into(imageViewTarget);

            GradientDrawable drawable = (GradientDrawable) bubbleView.getBackground();
            drawable.setColor(ContextCompat.getColor(context, R.color.receiveMessageBubble));
            layout.setPadding(0, 0, 0, (int)context.getResources().getDimension(R.dimen.bubble_start_top_space));
            profileImageView.setVisibility(View.VISIBLE);
            if(dp!=null) {
                dp.into(profileImageView);
            } else {
                profileImageView.setImageDrawable(textProfileDrawable);
            }
        }
    }

    class SendImageViewHolder extends RecyclerView.ViewHolder {
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
        @Bind(R.id.message_image)
        ImageView messageImage;

        private File imageUri;
        private String imageUrl;
        SendImageViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void renderImage(File localFile, String time, MessageResult.MessageStatus messageStatus, int position) {
            this.imageUri = localFile;
            int bubbleType = bubbleType(position);
            RoundedCornerTransformation roundedCornerTransformationT = null;
            RoundedCornerTransformation roundedCornerTransformationB = null;
            switch (bubbleType) {
                case 0:
                    layout.setPadding(0, 0, 0, (int)context.getResources().getDimension(R.dimen.bubble_start_top_space));
                    roundedCornerTransformationT = new RoundedCornerTransformation(context, (int)context.getResources().getDimension(R.dimen.bubble_full_corner_radius), 0, RoundedCornerTransformation.CornerType.TOP_RIGHT);
                    roundedCornerTransformationB = new RoundedCornerTransformation(context, (int)context.getResources().getDimension(R.dimen.bubble_full_corner_radius), 0, RoundedCornerTransformation.CornerType.BOTTOM_RIGHT);
                    break;
                case 1:
                    layout.setPadding(0, 0, 0, (int)context.getResources().getDimension(R.dimen.bubble_mid_top_space));
                    roundedCornerTransformationT = new RoundedCornerTransformation(context, (int)context.getResources().getDimension(R.dimen.bubble_full_corner_radius), 0, RoundedCornerTransformation.CornerType.TOP_RIGHT);
                    roundedCornerTransformationB = new RoundedCornerTransformation(context, (int)context.getResources().getDimension(R.dimen.bubble_mid_corner_radius), 0, RoundedCornerTransformation.CornerType.BOTTOM_RIGHT);
                    break;
                case 2:
                    layout.setPadding(0, 0, 0, (int)context.getResources().getDimension(R.dimen.bubble_mid_top_space));
                    roundedCornerTransformationT = new RoundedCornerTransformation(context, (int)context.getResources().getDimension(R.dimen.bubble_mid_corner_radius), 0, RoundedCornerTransformation.CornerType.TOP_RIGHT);
                    roundedCornerTransformationB = new RoundedCornerTransformation(context, (int)context.getResources().getDimension(R.dimen.bubble_mid_corner_radius), 0, RoundedCornerTransformation.CornerType.BOTTOM_RIGHT);
                    break;
                case 3:
                    layout.setPadding(0, 0, 0, (int)context.getResources().getDimension(R.dimen.bubble_start_top_space));
                    roundedCornerTransformationT = new RoundedCornerTransformation(context, (int)context.getResources().getDimension(R.dimen.bubble_mid_corner_radius), 0, RoundedCornerTransformation.CornerType.TOP_RIGHT);
                    roundedCornerTransformationB = new RoundedCornerTransformation(context, (int)context.getResources().getDimension(R.dimen.bubble_full_corner_radius), 0, RoundedCornerTransformation.CornerType.BOTTOM_RIGHT);
                    break;
            }

            //TODO: hacky solution:
            Glide
                    .with(context)
                    .load(localFile)
                    .asBitmap()
                    .transcode(new BitmapSizeTranscoder(), Size.class)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(new SimpleTarget<Size>() {
                        @Override public void onResourceReady(Size resource, GlideAnimation glideAnimation) {
                            int w,h;
                            if(resource.width >= resource.height) {
                                w= (int)AndroidUtils.px(240);
                                h= (int)((AndroidUtils.px(240)/resource.width)*resource.height);
                            } else {
                                h= (int)AndroidUtils.px(240);
                                w= (int)((AndroidUtils.px(240)/resource.height)*resource.width);
                            }
                            messageImage.getLayoutParams().height = h;
                            messageImage.getLayoutParams().width = w;
                            messageImage.requestLayout();

                            bubbleView.getLayoutParams().height = h;
                            bubbleView.getLayoutParams().width = w;
                            bubbleView.requestLayout();
                            Logger.d(this, "WidthI: "+w+", heightI:"+h);
                        }
                    });

            Glide.with(context).load(localFile)
                    .bitmapTransform(new FitCenter(context), new RoundedCornerTransformation(context, (int)context.getResources().getDimension(R.dimen.bubble_full_corner_radius), 0, RoundedCornerTransformation.CornerType.LEFT), roundedCornerTransformationB, roundedCornerTransformationT)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(messageImage);

            render(time, messageStatus, position);
        }

        void renderGif(String imageUrl, int iWidth, int iHeight, String time, MessageResult.MessageStatus messageStatus, int position) {
            Logger.d(this, "iWidth: "+iWidth+", "+iHeight);
            this.imageUrl = imageUrl;
            int bubbleType = bubbleType(position);
            Logger.d(this, "Radius: "+(int)context.getResources().getDimension(R.dimen.bubble_full_corner_radius));
            int fullRadius = (int)AndroidUtils.px(20);
            int midRadius = (int)AndroidUtils.px(20);
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

            if(iWidth<=0 || iHeight<=0) {
                Glide
                        .with(context)
                        .load(imageUrl)
                        .asBitmap()
                        .transcode(new BitmapSizeTranscoder(), Size.class)
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .into(new SimpleTarget<Size>() {
                            @Override
                            public void onResourceReady(Size resource, GlideAnimation glideAnimation) {
                                Logger.d(this, "SIZE: " + String.format(Locale.ROOT, "%dx%d", resource.width, resource.height));
                                int w, h;
                                if (resource.width >= resource.height) {
                                    w = (int) AndroidUtils.px(240);
                                    h = (int) ((AndroidUtils.px(240) / resource.width) * resource.height);
                                } else {
                                    h = (int) AndroidUtils.px(240);
                                    w = (int) ((AndroidUtils.px(240) / resource.height) * resource.width);
                                }
                                messageImage.getLayoutParams().height = h;
                                messageImage.getLayoutParams().width = w;
                                messageImage.requestLayout();

                                bubbleView.getLayoutParams().height = h;
                                bubbleView.getLayoutParams().width = w;
                                bubbleView.requestLayout();
                                Logger.d(this, "WidthG: " + w + ", heightG:" + h);
                            }
                        });
            } else {
                int wi = (int) AndroidUtils.px(iWidth);
                int he = (int) AndroidUtils.px(iHeight);
                int w, h;
                if (wi >= he) {
                    w = (int) AndroidUtils.px(240);
                    h = (int) ((AndroidUtils.px(240) / wi) * he);
                } else {
                    h = (int) AndroidUtils.px(240);
                    w = (int) ((AndroidUtils.px(240) / he) * wi);
                }

                messageImage.getLayoutParams().height = h;
                messageImage.getLayoutParams().width = w;
                messageImage.requestLayout();

                bubbleView.getLayoutParams().height = h;
                bubbleView.getLayoutParams().width = w;
                bubbleView.requestLayout();
            }

            Glide.with(context).load(imageUrl.replace("https://", "http://"))
                    .bitmapTransform(new CenterCrop(context))
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(messageImage);

            render(time, messageStatus, position);
        }

        void render(String time, MessageResult.MessageStatus messageStatus, int position) {
            boolean shouldShowTime = shouldShowTime(position);

            timeView.setText(time);
            deliveryStatusText.setVisibility(View.GONE);
            if(shouldShowTime) {
                timeView.setVisibility(View.VISIBLE);
                timeView.setPadding(0, (int)AndroidUtils.px(15.75f),0,(int)AndroidUtils.px(9f));
            } else {
                timeView.setVisibility(View.GONE);
                timeView.setPadding(0,0 ,0,(int)AndroidUtils.px(2));
            }

            if(messageStatus == MessageResult.MessageStatus.NOT_SENT) {
                deliveryStatusView.setImageResource(R.drawable.ic_delivery_pending);
            } else if(messageStatus == MessageResult.MessageStatus.SENT) {
                deliveryStatusView.setImageResource(R.drawable.ic_delivery_sent);
            } else if(messageStatus == MessageResult.MessageStatus.DELIVERED) {
                deliveryStatusView.setImageResource(R.drawable.ic_delivery_delivered);
            } else if(messageStatus == MessageResult.MessageStatus.READ) {
                deliveryStatusView.setImageResource(R.drawable.ic_delivery_read);
            }
            deliveryStatusText.setText(MessageResult.getDeliveryStatusText(messageStatus));
        }

        @OnClick(R.id.rl_bubble)
        public void onMessageClicked() {
            if(imageUrl!=null && !imageUrl.isEmpty()) {
                context.startActivity(ImageViewerActivity.callingIntent(context, imageUrl));
            }
        }

        @OnLongClick(R.id.rl_bubble)
        public boolean onMessageLongClicked() {
            return true;
        }
    }

    class ReceiveImageViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.rl_bubble)
        RelativeLayout bubbleView;
        @Bind(R.id.ll_message_receive_text)
        LinearLayout layout;
        @Bind(R.id.tv_time)
        TextView timeView;
        @Bind(R.id.message_image)
        ImageView messageImage;
        @Bind(R.id.iv_profileImage)
        ImageView profileImageView;

        private String imageUrl;
        ReceiveImageViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void renderItem(String imageUrl, String time, int position) {
            this.imageUrl = imageUrl;
            int bubbleType = bubbleType(position);
            Logger.d(this, "URL: "+imageUrl+" TYPE:+"+bubbleType);
            boolean shouldShowTime = shouldShowTime(position);

            timeView.setText(time);
            if(shouldShowTime) {
                timeView.setVisibility(View.VISIBLE);
                timeView.setPadding(0, (int)AndroidUtils.px(15.75f),0,(int)AndroidUtils.px(9f));
            } else {
                timeView.setVisibility(View.GONE);
                timeView.setPadding(0,0 ,0,(int)AndroidUtils.px(2));
            }

            RoundedCornerTransformation roundedCornerTransformationT = null;
            RoundedCornerTransformation roundedCornerTransformationB = null;
            switch (bubbleType) {
                case 0:
                    layout.setPadding(0, 0, 0, (int)context.getResources().getDimension(R.dimen.bubble_start_top_space));
                    roundedCornerTransformationT = new RoundedCornerTransformation(context, (int)context.getResources().getDimension(R.dimen.bubble_full_corner_radius), 0, RoundedCornerTransformation.CornerType.TOP_LEFT);
                    roundedCornerTransformationB = new RoundedCornerTransformation(context, (int)context.getResources().getDimension(R.dimen.bubble_full_corner_radius), 0, RoundedCornerTransformation.CornerType.BOTTOM_LEFT);
                    break;
                case 1:
                    layout.setPadding(0, 0, 0, (int)context.getResources().getDimension(R.dimen.bubble_mid_top_space));
                    roundedCornerTransformationT = new RoundedCornerTransformation(context, (int)context.getResources().getDimension(R.dimen.bubble_full_corner_radius), 0, RoundedCornerTransformation.CornerType.TOP_LEFT);
                    roundedCornerTransformationB = new RoundedCornerTransformation(context, (int)context.getResources().getDimension(R.dimen.bubble_mid_corner_radius), 0, RoundedCornerTransformation.CornerType.BOTTOM_LEFT);
                    break;
                case 2:
                    layout.setPadding(0, 0, 0, (int)context.getResources().getDimension(R.dimen.bubble_mid_top_space));
                    roundedCornerTransformationT = new RoundedCornerTransformation(context, (int)context.getResources().getDimension(R.dimen.bubble_mid_corner_radius), 0, RoundedCornerTransformation.CornerType.TOP_LEFT);
                    roundedCornerTransformationB = new RoundedCornerTransformation(context, (int)context.getResources().getDimension(R.dimen.bubble_mid_corner_radius), 0, RoundedCornerTransformation.CornerType.BOTTOM_LEFT);
                    break;
                case 3:
                    layout.setPadding(0, 0, 0, (int)context.getResources().getDimension(R.dimen.bubble_start_top_space));
                    roundedCornerTransformationT = new RoundedCornerTransformation(context, (int)context.getResources().getDimension(R.dimen.bubble_mid_corner_radius), 0, RoundedCornerTransformation.CornerType.TOP_LEFT);
                    roundedCornerTransformationB = new RoundedCornerTransformation(context, (int)context.getResources().getDimension(R.dimen.bubble_full_corner_radius), 0, RoundedCornerTransformation.CornerType.BOTTOM_LEFT);
                    break;
            }

            if(imageUrl!=null && !imageUrl.isEmpty()) {
                Logger.d(this, "Image url");
                Glide
                        .with(context)
                        .load(imageUrl.replace("https://", "http://"))
                        .asBitmap()
                        .transcode(new BitmapSizeTranscoder(), Size.class)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(new SimpleTarget<Size>() {
                            @Override public void onResourceReady(Size resource, GlideAnimation glideAnimation) {
                                Logger.d(this, "SIZE: "+String.format(Locale.ROOT, "%dx%d", resource.width, resource.height));
                                int w,h;
                                if(resource.width >= resource.height) {
                                    w= (int)AndroidUtils.px(240);
                                    h= (int)((AndroidUtils.px(240)/resource.width)*resource.height);
                                } else {
                                    h= (int)AndroidUtils.px(240);
                                    w= (int)((AndroidUtils.px(240)/resource.height)*resource.width);
                                }
                                messageImage.getLayoutParams().height = h;
                                messageImage.getLayoutParams().width = w;
                                messageImage.requestLayout();

                                bubbleView.getLayoutParams().height = h;
                                bubbleView.getLayoutParams().width = w;
                                bubbleView.requestLayout();
                                Logger.d(this, "Width: "+w+", height:"+h);
                            }
                        });

                Glide.with(context).load(imageUrl.replace("https://", "http://"))
                        .bitmapTransform(new FitCenter(context), new RoundedCornerTransformation(context, (int) context.getResources().getDimension(R.dimen.bubble_full_corner_radius), 0, RoundedCornerTransformation.CornerType.RIGHT), roundedCornerTransformationB, roundedCornerTransformationT)
                        .into(messageImage);
            }

            if(hasProfileDP(position)) {
                profileImageView.setVisibility(View.VISIBLE);
                if(dp!=null) {
                    dp.into(profileImageView);
                } else {
                    profileImageView.setImageDrawable(textProfileDrawable);
                }
            } else {
                profileImageView.setVisibility(View.INVISIBLE);
            }
        }

        void renderGif(String imageUrl, int iWidth, int iHeight, String time, int position) {
            this.imageUrl = imageUrl;
            int bubbleType = bubbleType(position);
            boolean shouldShowTime = shouldShowTime(position);

            timeView.setText(time);
            if(shouldShowTime) {
                timeView.setVisibility(View.VISIBLE);
                timeView.setPadding(0, (int)AndroidUtils.px(15.75f),0,(int)AndroidUtils.px(9f));
            } else {
                timeView.setVisibility(View.GONE);
                timeView.setPadding(0,0 ,0,(int)AndroidUtils.px(2));
            }

            int fullRadius = 26;
            int midRadius = 22;
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

            if(iWidth<=0 || iHeight<=0) {
                Glide
                        .with(context)
                        .load(imageUrl)
                        .asBitmap()
                        .transcode(new BitmapSizeTranscoder(), Size.class)
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .into(new SimpleTarget<Size>() {
                            @Override
                            public void onResourceReady(Size resource, GlideAnimation glideAnimation) {
                                Logger.d(this, "SIZE: " + String.format(Locale.ROOT, "%dx%d", resource.width, resource.height));
                                int w, h;
                                if (resource.width >= resource.height) {
                                    w = (int) AndroidUtils.px(240);
                                    h = (int) ((AndroidUtils.px(240) / resource.width) * resource.height);
                                } else {
                                    h = (int) AndroidUtils.px(240);
                                    w = (int) ((AndroidUtils.px(240) / resource.height) * resource.width);
                                }
                                messageImage.getLayoutParams().height = h;
                                messageImage.getLayoutParams().width = w;
                                messageImage.requestLayout();

                                bubbleView.getLayoutParams().height = h;
                                bubbleView.getLayoutParams().width = w;
                                bubbleView.requestLayout();
                                Logger.d(this, "WidthG: " + w + ", heightG:" + h);
                            }
                        });
            } else {
                int wi = (int) AndroidUtils.px(iWidth);
                int he = (int) AndroidUtils.px(iHeight);
                int w, h;
                if (wi >= he) {
                    w = (int) AndroidUtils.px(240);
                    h = (int) ((AndroidUtils.px(240) / wi) * he);
                } else {
                    h = (int) AndroidUtils.px(240);
                    w = (int) ((AndroidUtils.px(240) / he) * wi);
                }

                messageImage.getLayoutParams().height = h;
                messageImage.getLayoutParams().width = w;
                messageImage.requestLayout();

                bubbleView.getLayoutParams().height = h;
                bubbleView.getLayoutParams().width = w;
                bubbleView.requestLayout();
            }

            Glide.with(context).load(imageUrl.replace("https://", "http://"))
                    .bitmapTransform(new CenterCrop(context))
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(messageImage);

            if(hasProfileDP(position)) {
                profileImageView.setVisibility(View.VISIBLE);
                if(dp!=null) {
                    dp.into(profileImageView);
                } else {
                    profileImageView.setImageDrawable(textProfileDrawable);
                }
            } else {
                profileImageView.setVisibility(View.INVISIBLE);
            }
        }


        @OnClick(R.id.rl_bubble)
        public void onMessageClicked() {
            if(imageUrl!=null && !imageUrl.isEmpty()) {
                context.startActivity(ImageViewerActivity.callingIntent(context, imageUrl));
            }
        }

        @OnLongClick(R.id.rl_bubble)
        public boolean onMessageLongClicked() {
            return true;
        }
    }

    class SendAudioViewHolder extends RecyclerView.ViewHolder {
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
        @Bind(R.id.audio_message)
        AudioMessageView audioMessageView;

        SendAudioViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void renderItem(String fileName, String time, MessageResult.MessageStatus messageStatus, int position) {
            int bubbleType = bubbleType(position);
            boolean shouldShowTime = shouldShowTime(position);

            GradientDrawable drawable = (GradientDrawable) bubbleView.getBackground();
            drawable.setColor(ContextCompat.getColor(context, R.color.sendMessageBubble));

            audioMessageView.setAudioReadyListener(dur -> {
                long ds = dur/1000;
                if(ds <=5) {
                    audioMessageView.getLayoutParams().width = (int)AndroidUtils.px(157);
                    audioMessageView.requestLayout();
                } else if(ds*24 <= 262) {
                    audioMessageView.getLayoutParams().width = (int)AndroidUtils.px(ds*24);
                    audioMessageView.requestLayout();
                } else {
                    audioMessageView.getLayoutParams().width = (int)AndroidUtils.px(262);
                    audioMessageView.requestLayout();
                }
            });
            audioMessageView.setAudioFile(fileName);
            timeView.setText(time);
            deliveryStatusText.setVisibility(View.GONE);
            if(shouldShowTime) {
                timeView.setVisibility(View.VISIBLE);
                timeView.setPadding(0, (int)AndroidUtils.px(15.75f),0,(int)AndroidUtils.px(9f));
            } else {
                timeView.setVisibility(View.GONE);
                timeView.setPadding(0,0 ,0,(int)AndroidUtils.px(2));
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
            if(messageStatus == MessageResult.MessageStatus.NOT_SENT) {
                deliveryStatusView.setImageResource(R.drawable.ic_delivery_pending);
            } else if(messageStatus == MessageResult.MessageStatus.SENT) {
                deliveryStatusView.setImageResource(R.drawable.ic_delivery_sent);
            } else if(messageStatus == MessageResult.MessageStatus.DELIVERED) {
                deliveryStatusView.setImageResource(R.drawable.ic_delivery_delivered);
            } else if(messageStatus == MessageResult.MessageStatus.READ) {
                deliveryStatusView.setImageResource(R.drawable.ic_delivery_read);
            }

            deliveryStatusText.setText(MessageResult.getDeliveryStatusText(messageStatus));
        }

        @OnLongClick(R.id.rl_bubble)
        public boolean onMessageLongClicked() {
            return true;
        }
    }

    class ReceiveAudioViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.iv_profileImage)
        ImageView profileImageView;
        @Bind(R.id.rl_bubble)
        RelativeLayout bubbleView;
        @Bind(R.id.tv_time)
        TextView timeView;
        @Bind(R.id.audio_message)
        AudioMessageView audioMessageView;

        ReceiveAudioViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void renderItem(String url, String time, int position) {
            int bubbleType = bubbleType(position);
            boolean shouldShowTime = shouldShowTime(position);

            audioMessageView.setAudioReadyListener(dur -> {
                long ds = dur/1000;
                if(ds <=5) {
                    audioMessageView.getLayoutParams().width = (int)AndroidUtils.px(157);
                    audioMessageView.requestLayout();
                } else if(ds*24 <= 262) {
                    audioMessageView.getLayoutParams().width = (int)AndroidUtils.px(ds*24);
                    audioMessageView.requestLayout();
                } else {
                    audioMessageView.getLayoutParams().width = (int)AndroidUtils.px(262);
                    audioMessageView.requestLayout();
                }
            });
            audioMessageView.setAudioUrl(url);
            timeView.setText(time);
            if(shouldShowTime) {
                timeView.setVisibility(View.VISIBLE);
                timeView.setPadding(0, (int)AndroidUtils.px(15.75f),0,(int)AndroidUtils.px(9f));
            } else {
                timeView.setVisibility(View.GONE);
                timeView.setPadding(0,0 ,0,(int)AndroidUtils.px(2));
            }

            switch (bubbleType) {
                case 0:
                    bubbleView.setPadding(0, 0, 0, (int)context.getResources().getDimension(R.dimen.bubble_start_top_space));
                    break;
                case 1:
                    bubbleView.setPadding(0, 0, 0, (int)context.getResources().getDimension(R.dimen.bubble_mid_top_space));
                    break;
                case 2:
                    bubbleView.setPadding(0, 0, 0, (int)context.getResources().getDimension(R.dimen.bubble_mid_top_space));
                    break;
                case 3:
                    bubbleView.setPadding(0, 0, 0, (int)context.getResources().getDimension(R.dimen.bubble_start_top_space));
                    break;
            }

            if(hasProfileDP(position)) {
                profileImageView.setVisibility(View.VISIBLE);
                if(dp!=null) {
                    dp.into(profileImageView);
                } else {
                    profileImageView.setImageDrawable(textProfileDrawable);
                }
            } else {
                profileImageView.setVisibility(View.INVISIBLE);
            }
        }

        @OnLongClick(R.id.rl_bubble)
        public boolean onMessageLongClicked() {
            return true;
        }
    }

    interface PostbackClickListener {
        void sendPostbackMessage(String message, String payload);
    }

    interface UrlClickListener {
        void urlButtonClicked(String url);
    }

    interface QuickReplyActionListener {
        void navigateToGetLocation();
    }

    interface ContactActionListener {
        void onContactAddClicked();
        void onContactBlockedClicked();
        void onContactReportSpamClicked();
        void showAddBlock();
    }

    private class BitmapSizeTranscoder implements ResourceTranscoder<Bitmap, Size> {
        @Override public Resource<Size> transcode(Resource<Bitmap> toTranscode) {
            Bitmap bitmap = toTranscode.get();
            Size size = new Size();
            size.width = bitmap.getWidth();
            size.height = bitmap.getHeight();
            return new SimpleResource<>(size);
        }
        @Override public String getId() {
            return getClass().getName();
        }
    }
}