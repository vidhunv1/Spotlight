package com.stairway.spotlight.core;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.stairway.spotlight.MessageController;
import com.stairway.spotlight.XMPPManager;
import com.stairway.spotlight.db.MessageStore;
import com.stairway.spotlight.models.MessageResult;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jivesoftware.smackx.chatstates.packet.ChatStateExtension;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;

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
    private final int RETRY_OFFLINE = 1;
    private final int RETRY_ONLINE = 1;
    private int retryInterval = RETRY_OFFLINE; //seconds
    private MessageStore messageStore;
    private MessageController messageApi;
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
        EventBus.getInstance().getBus().register(this);

        broadcaster = LocalBroadcastManager.getInstance(this);
        activity_name = intent.getStringExtra(TAG_ACTIVITY_NAME);
        messageStore = new MessageStore();
        messageApi = MessageController.getInstance();

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
        ChatManager chatManager = ChatManager.getInstanceFor(XMPPManager.getInstance().getConnection());

        chatManager.addChatListener((chat, createdLocally) -> {
            chat.addMessageListener((chat1, message) -> {
                String participant = XMPPManager.getUserNameFromJid(chat.getParticipant());
                String messageBody = null;
                if(!message.getBodies().isEmpty()) {
                    messageBody = message.getBodies().iterator().next().getMessage();
                }

                if(messageBody!=null && !messageBody.isEmpty()) {
                    MessageResult receivedMessage = new MessageResult(participant, participant, messageBody);
                    receivedMessage.setReceiptId(message.getStanzaId());
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
                    } else if(message.getExtension(ReadReceiptExtension.NAMESPACE)!=null) {
                        ExtensionElement readElement = message.getExtension(ReadReceiptExtension.NAMESPACE);
                        ReadReceiptExtension readReceiptExtension = (ReadReceiptExtension)readElement;

                        if(readReceiptExtension!=null) {
                            broadcastDeliveryReceipt(participant, readReceiptExtension.getLastMessageReceiptId(), MessageResult.MessageStatus.READ);

                            messageStore.updateAllMessageStatus(readReceiptExtension.getLastMessageReceiptId(), MessageResult.MessageStatus.READ)
                                .subscribe(new Subscriber<Boolean>() {
                                    @Override
                                    public void onCompleted() {}

                                    @Override
                                    public void onError(Throwable e) {}

                                    @Override
                                    public void onNext(Boolean aBoolean) {
                                        // updated
                                    }
                                });
                        }
                    }
                }
            });
        });
    }

    public void getDeliveryReceipts(){
        DeliveryReceiptManager.getInstanceFor(XMPPManager.getInstance().getConnection())
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
            XMPPManager.getInstance().getConnection().disconnect(new Presence(Presence.Type.unavailable));
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    private boolean tryConnect(){
        try {
            // TODO: BUGGY
            if(!XMPPManager.getInstance().getConnection().isConnected()) {
                XMPPManager.getInstance().getConnection().connect().login();
            }
            if(!isOnlineNotified) {
                retryInterval = RETRY_ONLINE;
                xmppServiceCallback.networkOnline();
                isOnlineNotified = true;
                sendUnsentMessages();
            }
            return true;
        } catch (SmackException.ConnectionException e) {
            retryInterval = RETRY_OFFLINE;
            xmppServiceCallback.networkOffline();
            isOnlineNotified = false;
            e.printStackTrace();
            return false;
        } catch (SmackException.AlreadyConnectedException e) {
            return true;
        } catch (SmackException.AlreadyLoggedInException e) {
            return true;
        } catch (Exception e) {
            Logger.e(this, e.getMessage());
            return false;
        }
    }

    private void sendUnsentMessages(){
        //send unsent messages
        Logger.d(this, " sending unsentMsgs");
        messageStore.getUnsentMessages().subscribe(new Subscriber<MessageResult>() {
            @Override
            public void onCompleted() {}
            @Override
            public void onError(Throwable e) {}
            @Override
            public void onNext(MessageResult messageResult) {
                messageApi.sendMessage(messageResult).subscribe(new Subscriber<MessageResult>() {
                    @Override
                    public void onCompleted() {}
                    @Override
                    public void onError(Throwable e) {}

                    @Override
                    public void onNext(MessageResult messageResult) {
                        messageStore.updateMessage(messageResult).subscribe(new Subscriber<MessageResult>() {
                            @Override
                            public void onCompleted() {}
                            @Override
                            public void onError(Throwable e) {}

                            @Override
                            public void onNext(MessageResult messageResult) {
                                broadcastDeliveryReceipt(messageResult.getChatId(), messageResult.getReceiptId(), messageResult.getMessageStatus());
                            }
                        });
                    }
                });
            }
        });
    }

    private void broadcastReceivedMessage(MessageResult messageId) {
        Intent intent = new Intent(XMPP_ACTION_RCV_MSG);
        if(messageId != null)
            intent.putExtra(XMPP_RESULT_MESSAGE, messageId);
        broadcaster.sendBroadcast(intent);

        Logger.d(this, "Emitting event");
        EventBus.getInstance().getBus().post(messageId);
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