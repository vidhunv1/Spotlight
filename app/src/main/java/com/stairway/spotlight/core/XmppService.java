package com.stairway.spotlight.core;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import com.stairway.data.manager.Logger;
import com.stairway.data.manager.XMPPManager;
import com.stairway.data.source.message.MessageResult;
import com.stairway.data.source.message.MessageStore;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jivesoftware.smackx.chatstates.packet.ChatStateExtension;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.ReceiptReceivedListener;

import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

import rx.Subscriber;

/**
 * Created by vidhun on 11/11/16.
 */

public class XmppService extends Service {

    // Constant
    public static String TAG_ACTIVITY_NAME = "activity_name";

    static final public String XMPP_ACTION_RCV_MSG = "com.stairway.spotlight.core.XmppService.MESSAGE_RECEIVED";
    static final public String XMPP_RESULT_MESSAGE = "com.stairway.spotlight.core.XmppService.XMPP_MSG";

    static final public String XMPP_ACTION_RCV_STATE = "com.stairway.spotlight.core.XmppService.CHAT_STATE_RECEIVED";
    static final public String XMPP_RESULT_STATE = "com.stairway.spotlight.core.XmppService.XMPP_STATE";
    static final public String XMPP_RESULT_FROM = "com.stairway.spotlight.core.XmppService.XMPP_FROM";

    static final public String XMPP_ACTION_RCV_RECEIPT = "com.stairway.spotlight.core.XmppService.DELIVERY_RECEIPT_RECEIVED";
    static final public String XMPP_RESULT_MSG_STATUS = "com.stairway.spotlight.core.XmppService.XMPP_MSG_STATUS";
    static final public String XMPP_RESULT_CHAT_ID = "com.stairway.spotlight.core.XmppService.XMPP_CHAT_ID";
    static final public String XMPP_RESULT_RECEIPT_ID = "com.stairway.spotlight.core.XmppService.XMPP_RECEIPT_ID";

    private static boolean isReceivingMessages = false;

    private String activity_name;
    private boolean isOnlineNotified = false;
    private int retryInterval = 100;
    private MessageStore messageStore;
    private LocalBroadcastManager broadcaster;

    private Timer mTimer = null;

    XmppServiceCallback xmppServiceCallback;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public interface XmppServiceCallback {
        void networkOnline();
        void networkOffline();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        broadcaster = LocalBroadcastManager.getInstance(this);
        activity_name = intent.getStringExtra(TAG_ACTIVITY_NAME);
        messageStore = new MessageStore();

        try {
            xmppServiceCallback = (XmppServiceCallback) Class.forName(activity_name).newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TryXMPPConnection(), 0,  retryInterval* 1000);
        receiveMessages();
        getDeliveryReceipts();

        return super.onStartCommand(intent, flags, startId);
    }

    public void receiveMessages() {
        if(isReceivingMessages)
            return;

        isReceivingMessages = true;
        ChatManager chatManager = ChatManager.getInstanceFor(XMPPManager.getConnection());

        chatManager.addChatListener((chat, createdLocally) -> {
            chat.addMessageListener((chat1, message) -> {
                String participant = XMPPManager.getUserNameFromJid(chat.getParticipant());
                String messageBody = null;
                if(!message.getBodies().isEmpty())
                    messageBody = message.getBodies().iterator().next().getMessage();

                if(messageBody!=null && !messageBody.isEmpty()) {
                    MessageResult receivedMessage = new MessageResult(participant, participant, messageBody);
                    receivedMessage.setMessageStatus(MessageResult.MessageStatus.UNSEEN);
                    messageStore.storeMessage(receivedMessage).subscribe(new Subscriber<MessageResult>() {
                        @Override
                        public void onCompleted() {}
                        @Override
                        public void onError(Throwable e) {}

                        @Override
                        public void onNext(MessageResult messageResult) {
                            broadcastReceivedMessage(receivedMessage);
                        }
                    });
                } else{
                    ExtensionElement element = message.getExtension(ChatStateExtension.NAMESPACE);
                    if (element != null) {
                        switch (element.getElementName()) {
                            case "composing":
                                broadcastTypingState(participant, ChatState.composing);
                                break;
                            case "paused":
                                broadcastTypingState(participant, ChatState.paused);
                                break;
                            case "active":
                                broadcastTypingState(participant, ChatState.active);
                                break;
                            case "inactive":
                                broadcastTypingState(participant, ChatState.inactive);
                                break;
                            case "gone":
                                broadcastTypingState(participant, ChatState.gone);
                                break;
                        }
                    }
                }
            });
        });
    }

    public void getDeliveryReceipts(){
        DeliveryReceiptManager.getInstanceFor(XMPPManager.getConnection()).setAutoReceiptMode(DeliveryReceiptManager.AutoReceiptMode.always);
        DeliveryReceiptManager.getInstanceFor(XMPPManager.getConnection())
                .addReceiptReceivedListener((fromJid, toJid, deliveryReceiptId, stanza) -> {
                    String chatId = XMPPManager.getUserNameFromJid(fromJid);
                    messageStore.updateMessageStatus(chatId, deliveryReceiptId, MessageResult.MessageStatus.DELIVERED).subscribe(new Subscriber<Boolean>() {
                        @Override
                        public void onCompleted() {
                            broadcastDeliveryReceipt(XMPPManager.getUserNameFromJid(chatId), deliveryReceiptId, MessageResult.MessageStatus.DELIVERED);
                        }
                        @Override
                        public void onError(Throwable e) {}
                        @Override
                        public void onNext(Boolean aBoolean) {}
                    });
                });
    }

    class TryXMPPConnection extends TimerTask{
        @Override
        public void run() {
            tryConnect();
        }
    }

    @Override
    public void onDestroy() {
        mTimer.cancel();
        try {
            XMPPManager.getConnection().disconnect(new Presence(Presence.Type.unavailable));
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    private boolean tryConnect(){
        try {
            Logger.d("TryConnect");
            // TODO: BUGGY
            if(!XMPPManager.getConnection().isConnected()) {
                XMPPManager.getConnection().connect().login();
            }

            if(!isOnlineNotified) {
                xmppServiceCallback.networkOnline();
                isOnlineNotified = true;
            }
            return true;
        } catch (SmackException.ConnectionException e) {
            xmppServiceCallback.networkOffline();
            isOnlineNotified = false;
            e.printStackTrace();
            return false;
        } catch (SmackException.AlreadyConnectedException e) {
            return true;
        } catch (SmackException.AlreadyLoggedInException e) {
            return true;
        } catch (Exception e) {
            Logger.d(e.getMessage());
            return false;
        }
    }

    private void broadcastReceivedMessage(MessageResult messageId) {
        Intent intent = new Intent(XMPP_ACTION_RCV_MSG);
        if(messageId != null)
            intent.putExtra(XMPP_RESULT_MESSAGE, messageId);
        broadcaster.sendBroadcast(intent);
    }

    private void broadcastTypingState(String participant, ChatState state) {
        Intent intent = new Intent(XMPP_ACTION_RCV_STATE);
        intent.putExtra(XMPP_RESULT_STATE, state);
        intent.putExtra(XMPP_RESULT_FROM, participant);
        broadcaster.sendBroadcast(intent);
    }

    private void broadcastDeliveryReceipt(String chatId, String deliveryReceiptId, MessageResult.MessageStatus messageStatus){
        Intent intent = new Intent(XMPP_ACTION_RCV_RECEIPT);
        intent.putExtra(XMPP_RESULT_MSG_STATUS, messageStatus.name());
        intent.putExtra(XMPP_RESULT_CHAT_ID, chatId);
        intent.putExtra(XMPP_RESULT_RECEIPT_ID, deliveryReceiptId);
        broadcaster.sendBroadcast(intent);
    }
}
