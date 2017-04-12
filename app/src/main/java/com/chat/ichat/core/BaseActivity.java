package com.chat.ichat.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.chat.ichat.UserSessionManager;
import com.chat.ichat.MessageService;
import com.chat.ichat.config.AnalyticsContants;
import com.chat.ichat.core.lib.AndroidUtils;
import com.chat.ichat.models.ContactResult;
import com.chat.ichat.models.UserSession;
import com.chat.ichat.models.MessageResult;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.chatstates.ChatState;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by vidhun on 05/07/16.
 */
public class BaseActivity extends AppCompatActivity{
    BroadcastReceiver receiver;

    private FirebaseAnalytics firebaseAnalytics;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UserSession userSession = UserSessionManager.getInstance().load();
        if(userSession ==null)
            throw new IllegalStateException("Base activity should be only initialized on user session");

        //Register MessageReceiver
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(MessageService.XMPP_ACTION_RCV_MSG)) {
                    MessageResult s = (MessageResult) intent.getSerializableExtra(MessageService.XMPP_RESULT_MESSAGE);
                    ContactResult from = (ContactResult) intent.getSerializableExtra(MessageService.XMPP_RESULT_CONTACT);
                    onMessageReceived(s, from);
                } else if(intent.getAction().equals(MessageService.XMPP_ACTION_RCV_STATE)) {
                    String from = intent.getStringExtra(MessageService.XMPP_RESULT_FROM);
                    ChatState chatState = (ChatState) intent.getSerializableExtra(MessageService.XMPP_RESULT_STATE);
                    onChatStateReceived(from, chatState);
                } else if(intent.getAction().equals(MessageService.XMPP_ACTION_RCV_RECEIPT)) {
                    String chatId = intent.getStringExtra(MessageService.XMPP_RESULT_CHAT_ID);
                    String deliveryReceiptId = intent.getStringExtra(MessageService.XMPP_RESULT_RECEIPT_ID);
                    MessageResult.MessageStatus messageStatus = MessageResult.MessageStatus.valueOf(intent.getStringExtra(MessageService.XMPP_RESULT_MSG_STATUS));
                    onMessageStatusReceived(chatId, deliveryReceiptId, messageStatus);
                } else if(intent.getAction().equals(MessageService.ACTION_INTERNET_CONNECTION_STATUS)) {
                    boolean isConnectionAvailable = intent.getBooleanExtra(MessageService.ACTION_INTERNET_CONNECTION_STATUS, false);
                    onNetworkStatus(isConnectionAvailable);
                } else if(intent.getAction().equals(MessageService.XMPP_ACTION_RCV_PRESENCE)) {
                    Presence.Type type = (Presence.Type) intent.getSerializableExtra(MessageService.XMPP_RESULT_PRESENCE_TYPE);
                    String username = intent.getStringExtra(MessageService.XMPP_RESULT_PRESENCE_FROM);
                    onPresenceChanged(username, type);
                }
            }
        };

        this.firebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    @Override
    protected void onStart() {
        IntentFilter filter = new IntentFilter(MessageService.XMPP_ACTION_RCV_STATE);
        filter.addAction(MessageService.XMPP_ACTION_RCV_MSG);
        filter.addAction(MessageService.XMPP_ACTION_RCV_RECEIPT);
        filter.addAction(MessageService.ACTION_INTERNET_CONNECTION_STATUS);
        filter.addAction(MessageService.XMPP_ACTION_RCV_PRESENCE);
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver), filter);
        super.onStart();
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();
    }

    @Override
    protected void onPause() {
        AndroidUtils.hideSoftInput(this);
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public void onMessageReceived(MessageResult messageId, ContactResult from) {
        Logger.d(this, "[Base]Message Received "+messageId);
        NotificationController.getInstance().showNotificationAndAlert(true);

        /*              Analytics           */
        Bundle bundle = new Bundle();
        bundle.putString(AnalyticsContants.Param.OTHER_USER_NAME, from.getUsername());
        firebaseAnalytics.logEvent(AnalyticsContants.Event.RECEIVE_MESSAGE, bundle);
    }

    public void onPresenceChanged(String username, Presence.Type type) {
        Logger.d(this, "Received Presence: "+username+", Type: "+type);

        /*              Analytics           */
        Bundle bundle = new Bundle();
        bundle.putString(AnalyticsContants.Param.OTHER_USER_NAME, username);
        firebaseAnalytics.logEvent(AnalyticsContants.Event.RECEIVE_PRESENCE, bundle);
    }

    public void onNetworkStatus(boolean isAvailable) {
        if(isAvailable)
            Logger.d(this, "Internet available");
        else
            Logger.d(this, "Internet not available");
    }

    public void onChatStateReceived(String from, ChatState chatState) {
        Logger.d(this, "chatState: "+chatState.name()+", from "+from);

        /*              Analytics           */
        Bundle bundle = new Bundle();
        bundle.putString(AnalyticsContants.Param.OTHER_USER_NAME, from);
        firebaseAnalytics.logEvent(AnalyticsContants.Event.RECEIVE_CHAT_STATE, bundle);
    }

    public void onMessageStatusReceived(String chatId, String deliveryReceiptId, MessageResult.MessageStatus messageStatus) {
        /*              Analytics           */
        Bundle bundle = new Bundle();
        bundle.putString(AnalyticsContants.Param.OTHER_USER_NAME, chatId);
        firebaseAnalytics.logEvent(AnalyticsContants.Event.RECEIVE_MESSAGE_STATUS, bundle);
    }
}