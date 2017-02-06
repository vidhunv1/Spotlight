package com.stairway.spotlight;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.stairway.spotlight.core.Logger;
import com.stairway.spotlight.core.ReadReceiptExtension;
import com.stairway.spotlight.db.MessageStore;
import com.stairway.spotlight.models.MessageResult;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jivesoftware.smackx.chatstates.packet.ChatStateExtension;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.ReceiptReceivedListener;

import java.util.Timer;
import java.util.TimerTask;

import rx.Subscriber;

/**
 * Created by vidhun on 11/11/16.
 */

public class MessageService extends Service {

    // Constant
    public static String TAG_ACTIVITY_NAME = "activity_name";

    static final public String XMPP_ACTION_RCV_MSG = "com.stairway.spotlight.MessageService.MESSAGE_RECEIVED";
    static final public String XMPP_RESULT_MESSAGE = "com.stairway.spotlight.MessageService.XMPP_MSG";

    static final public String XMPP_ACTION_RCV_STATE = "com.stairway.spotlight.MessageService.CHAT_STATE_RECEIVED";
    static final public String XMPP_RESULT_STATE = "com.stairway.spotlight.MessageService.XMPP_STATE";
    static final public String XMPP_RESULT_FROM = "com.stairway.spotlight.MessageService.XMPP_FROM";

    static final public String XMPP_ACTION_RCV_RECEIPT = "com.stairway.spotlight.MessageService.DELIVERY_RECEIPT_RECEIVED";
    static final public String XMPP_RESULT_MSG_STATUS = "com.stairway.spotlight.MessageService.XMPP_MSG_STATUS";
    static final public String XMPP_RESULT_CHAT_ID = "com.stairway.spotlight.MessageService.XMPP_CHAT_ID";
    static final public String XMPP_RESULT_RECEIPT_ID = "com.stairway.spotlight.MessageService.XMPP_RECEIPT_ID";

    static final public String ACTION_INTERNET_CONNECTION_STATUS = "com.stairway.spotlight.MessageService.INTERNET_CONNECTION_STATUS";
    static final public String CONNECTION_STATE = "com.stairway.spotlight.MessageService.INTERNET_CONNECTION_STATE";

    private boolean isOnlineNotified = false;
    private final int RETRY_OFFLINE = 1;
    private final int RETRY_ONLINE = 1;
    private int retryInterval = RETRY_OFFLINE; //seconds
    private Timer mTimer = null;

    private MessageStore messageStore;
    private MessageController messageApi;
    private XMPPTCPConnection connection;
    private ChatManagerListener chatListener;
    private ReceiptReceivedListener receiptReceivedListener;

    private LocalBroadcastManager broadcaster;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.d(this, "onCreate");
        messageStore = MessageStore.getInstance();
        messageApi = MessageController.getInstance();
        connection = XMPPManager.getInstance().getConnection();
        broadcaster = LocalBroadcastManager.getInstance(this);

        this.receiptReceivedListener = (fromJid, toJid, deliveryReceiptId, stanza) -> {
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
        };

        this.chatListener = (chat, createdLocally) -> {
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
        };

        ChatManager.getInstanceFor(connection).addChatListener(this.chatListener);
        DeliveryReceiptManager.getInstanceFor(connection)
                .addReceiptReceivedListener(receiptReceivedListener);

        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TryXMPPConnection(), 0,  retryInterval* 1000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Logger.d(this, "onDestry");
        mTimer.cancel();

        ChatManager.getInstanceFor(connection).removeChatListener(this.chatListener);
        DeliveryReceiptManager.getInstanceFor(connection).removeReceiptReceivedListener(receiptReceivedListener);

        try {
            connection.disconnect(new Presence(Presence.Type.unavailable));
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    class TryXMPPConnection extends TimerTask{
        @Override
        public void run() {
            tryConnect();
        }
    }

    private boolean tryConnect(){
        try {
            // TODO: BUGGY
            if(!connection.isConnected()) {
                connection.connect().login();
            }
            if(!isOnlineNotified) {
                retryInterval = RETRY_ONLINE;
                broadcastConnectionStatus(true);
                isOnlineNotified = true;
                sendUnsentMessages();
            }
            return true;
        } catch (SmackException.ConnectionException e) {
            retryInterval = RETRY_OFFLINE;
            broadcastConnectionStatus(false);
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
        messageStore.getUnsentMessages()
                .subscribe(new Subscriber<MessageResult>() {
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

    private void broadcastConnectionStatus(boolean isAvailable) {
        Intent intent = new Intent(ACTION_INTERNET_CONNECTION_STATUS);
        intent.putExtra(CONNECTION_STATE, isAvailable);
        broadcaster.sendBroadcast(intent);
    }
}