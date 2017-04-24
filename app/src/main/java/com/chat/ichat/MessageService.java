package com.chat.ichat;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.chat.ichat.api.ApiManager;
import com.chat.ichat.api.bot.BotApi;
import com.chat.ichat.api.bot.BotResponse;
import com.chat.ichat.api.user.UserApi;
import com.chat.ichat.api.user.UserResponse;
import com.chat.ichat.api.user._User;
import com.chat.ichat.core.Logger;
import com.chat.ichat.core.NotificationController;
import com.chat.ichat.core.ReadReceiptExtension;
import com.chat.ichat.db.BotDetailsStore;
import com.chat.ichat.db.ContactStore;
import com.chat.ichat.db.MessageStore;
import com.chat.ichat.models.ContactResult;
import com.chat.ichat.models.MessageResult;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jivesoftware.smackx.chatstates.packet.ChatStateExtension;
import org.jivesoftware.smackx.ping.PingManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.ReceiptReceivedListener;

import java.io.IOException;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by vidhun on 11/11/16.
 */

public class MessageService extends Service {

    // Constant
    public static String TAG_ACTIVITY_NAME = "activity_name";

    static final public String XMPP_ACTION_RCV_MSG = "com.stairway.spotlight.MessageService.MESSAGE_RECEIVED";
    static final public String XMPP_RESULT_CONTACT = "com.stairway.spotlight.MessageService.XMPP_CONTACT";
    static final public String XMPP_RESULT_MESSAGE = "com.stairway.spotlight.MessageService.XMPP_MSG";

    static final public String XMPP_ACTION_RCV_STATE = "com.stairway.spotlight.MessageService.CHAT_STATE_RECEIVED";
    static final public String XMPP_RESULT_STATE = "com.stairway.spotlight.MessageService.XMPP_STATE";
    static final public String XMPP_RESULT_FROM = "com.stairway.spotlight.MessageService.XMPP_FROM";

    static final public String XMPP_ACTION_RCV_PRESENCE = "com.stairway.spotlight.MessageService.PRESENCE_RECEIVED";
    static final public String XMPP_RESULT_PRESENCE_TYPE = "com.stairway.spotlight.MessageService.XMPP_PRESENCE_TYPE";
    static final public String XMPP_RESULT_PRESENCE_FROM = "com.stairway.spotlight.MessageService.XMPP_PRESENCE_FROM";

    static final public String XMPP_ACTION_RCV_RECEIPT = "com.stairway.spotlight.MessageService.DELIVERY_RECEIPT_RECEIVED";
    static final public String XMPP_RESULT_MSG_STATUS = "com.stairway.spotlight.MessageService.XMPP_MSG_STATUS";
    static final public String XMPP_RESULT_CHAT_ID = "com.stairway.spotlight.MessageService.XMPP_CHAT_ID";
    static final public String XMPP_RESULT_RECEIPT_ID = "com.stairway.spotlight.MessageService.XMPP_RECEIPT_ID";
    static final public String XMPP_RESULT_MESSAGE_ID = "com.stairway.spotlight.MessageService.XMPP_MESSAGE_ID";

    static final public String ACTION_INTERNET_CONNECTION_STATUS = "com.stairway.spotlight.MessageService.INTERNET_CONNECTION_STATUS";
    static final public String CONNECTION_STATE = "com.stairway.spotlight.MessageService.INTERNET_CONNECTION_STATE";

    private boolean isOnlineNotified = false;
    private final int RETRY_OFFLINE = 1;
    private final int RETRY_ONLINE = 3;
    private int retryInterval = 2; //seconds
    private Timer networkTimer = null;
    private Timer connectTimer = null;

    private MessageStore messageStore;
    private ContactStore contactStore;
    private MessageController messageApi;
    private UserApi userApi;
    private BotDetailsStore botDetailsStore;
    private BotApi botApi;
    private XMPPTCPConnection connection;

    private ChatManagerListener chatListener;
    private ReceiptReceivedListener receiptReceivedListener;
    private RosterListener presenceStateListener;
    private ConnectionListener connectionListener;
    private PingManager pingManager;

    private LocalBroadcastManager broadcaster;
    Roster roster;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.d(this, "onCreate");
        messageStore = MessageStore.getInstance();
        contactStore = ContactStore.getInstance();
        messageApi = MessageController.getInstance();
        userApi = ApiManager.getUserApi();
        this.botDetailsStore = BotDetailsStore.getInstance();
        this.botApi = ApiManager.getBotApi();
        connection = XMPPManager.getInstance().getConnection();
        broadcaster = LocalBroadcastManager.getInstance(this);

        this.receiptReceivedListener = (fromJid, toJid, deliveryReceiptId, stanza) -> {
            String chatId = XMPPManager.getUserNameFromJid(fromJid);
            messageStore.updateMessageStatus(chatId, deliveryReceiptId, MessageResult.MessageStatus.DELIVERED).subscribe(new Subscriber<Boolean>() {
                @Override
                public void onCompleted() {
                    broadcastDeliveryReceipt("", XMPPManager.getUserNameFromJid(chatId), deliveryReceiptId, MessageResult.MessageStatus.DELIVERED);
                }
                @Override
                public void onError(Throwable e) {}
                @Override
                public void onNext(Boolean aBoolean) {}
            });
        };

        this.presenceStateListener = new RosterListener() {
            @Override
            public void entriesAdded(Collection<String> addresses) {
                Logger.d(this, "entries added");
            }

            @Override
            public void entriesUpdated(Collection<String> addresses) {

            }

            @Override
            public void entriesDeleted(Collection<String> addresses) {

            }

            @Override
            public void presenceChanged(Presence presence) {
                broadcastPresenceState(presence.getFrom().split("/")[0], presence.getType());

                Logger.d(this, "Received Presence"+presence.getType().name());
            }
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

                    contactStore.getContactByUserName(receivedMessage.getChatId()).subscribe(new Subscriber<ContactResult>() {
                        @Override
                        public void onCompleted() {}

                        @Override
                        public void onError(Throwable e) {}

                        @Override
                        public void onNext(ContactResult contactResult) {
                            if(contactResult == null) {
                                userApi.findUserByUserName(receivedMessage.getChatId()).subscribe(new Subscriber<UserResponse>() {
                                    @Override
                                    public void onCompleted() {}

                                    @Override
                                    public void onError(Throwable e) {}

                                    @Override
                                    public void onNext(UserResponse userResponse) {
                                        ContactResult contactResult1 = new ContactResult();
                                        contactResult1.setUserId(userResponse.getUser().getUserId());
                                        contactResult1.setContactName(userResponse.getUser().getName());
                                        contactResult1.setUsername(userResponse.getUser().getUsername());
                                        contactResult1.setAdded(false);
                                        contactResult1.setBlocked(false);
                                        contactResult1.setUserType(userResponse.getUser().getUserType());
                                        contactResult1.setProfileDP(userResponse.getUser().getProfileDP());

                                        Roster roster = Roster.getInstanceFor(XMPPManager.getInstance().getConnection());
                                        if (!roster.isLoaded())
                                            try {
                                                roster.reloadAndWait();
                                            } catch (SmackException.NotLoggedInException | SmackException.NotConnectedException | InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        try {
                                            roster.createEntry(XMPPManager.getJidFromUserName(contactResult1.getUsername()), contactResult1.getContactName(), null);
                                        } catch (SmackException.NotLoggedInException | SmackException.NoResponseException | XMPPException.XMPPErrorException | SmackException.NotConnectedException e) {
                                            e.printStackTrace();
                                        }

                                        contactStore.storeContact(contactResult1)
                                                .subscribe(new Subscriber<Boolean>() {
                                                    @Override
                                                    public void onCompleted() {}

                                                    @Override
                                                    public void onError(Throwable e) {}

                                                    @Override
                                                    public void onNext(Boolean aBoolean) {
                                                        // TODO: Move to use case file!!
                                                        if(contactResult1.getUserType()== _User.UserType.regular) {
                                                            storeAndbroadcastReceivedMessage(receivedMessage, contactResult1);
                                                        } else if(contactResult1.getUserType() == _User.UserType.official){
                                                            botApi.getBotDetails(contactResult1.getUsername())
                                                                    .subscribeOn(Schedulers.io())
                                                                    .observeOn(AndroidSchedulers.mainThread())
                                                                    .subscribe(new Subscriber<BotResponse>() {
                                                                        @Override
                                                                        public void onCompleted() {
                                                                            Logger.d(this, "onComplete");
                                                                        }

                                                                        @Override
                                                                        public void onError(Throwable e) {
                                                                            Logger.d(this, "Error: "+e.getMessage());
                                                                            e.printStackTrace();
                                                                        }

                                                                        @Override
                                                                        public void onNext(BotResponse data) {
                                                                            if(data.isSuccess()) {
                                                                                BotResponse.Data botResponse = data.getData();
                                                                                Logger.d(this, botResponse.toString());
                                                                                botDetailsStore.putMenu(botResponse.getUsername(), botResponse.getPersistentMenus())
                                                                                        .subscribeOn(Schedulers.newThread())
                                                                                        .observeOn(AndroidSchedulers.mainThread())
                                                                                        .subscribe(new Subscriber<Boolean>() {
                                                                                            @Override
                                                                                            public void onCompleted() {
                                                                                            }

                                                                                            @Override
                                                                                            public void onError(Throwable e) {
                                                                                                storeAndbroadcastReceivedMessage(receivedMessage, contactResult1);
                                                                                            }

                                                                                            @Override
                                                                                            public void onNext(Boolean aBoolean) {
                                                                                                storeAndbroadcastReceivedMessage(receivedMessage, contactResult1);
                                                                                            }
                                                                                        });
                                                                            } else {
                                                                                Logger.d(this, "Error response");
                                                                                Logger.d(this, data.getError().toString());
                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                });
                                    }
                                });
                            } else {
                                if(contactResult.isBlocked()) {
                                    // do nothing
                                } else {
                                    storeAndbroadcastReceivedMessage(receivedMessage, contactResult);
                                }
                            }
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
                            broadcastDeliveryReceipt("", participant, readReceiptExtension.getLastMessageReceiptId(), MessageResult.MessageStatus.READ);

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

        this.connectionListener = new ConnectionListener() {
            @Override
            public void connected(XMPPConnection connection) {
                Logger.d(this, "[-]Connected XMPP");
                Logger.d(this, "XMPP connected.");
                retryInterval = RETRY_ONLINE;
                broadcastConnectionStatus(true);
                isOnlineNotified = true;
                sendUnsentMessages();
            }

            @Override
            public void authenticated(XMPPConnection connection, boolean resumed) {
                Logger.d(this, "[-]Authenticated");
            }

            @Override
            public void connectionClosed() {
                Logger.d(this, "[-]ConnectionClosed");
            }

            @Override
            public void connectionClosedOnError(Exception e) {
                Logger.d(this, "[-]connectionClosedOnError");
            }
            @Override
            public void reconnectionSuccessful() {
                Logger.d(this, "[-]reconnectionSuccessful");

            }
            @Override
            public void reconnectingIn(int seconds) {
                Logger.d(this, "[-]reconnectingIn");
            }
            @Override
            public void reconnectionFailed(Exception e) {
                Logger.d(this, "[-]reconnectionFailed");
            }
        };

        pingManager = PingManager.getInstanceFor(connection);

        ChatManager.getInstanceFor(connection).addChatListener(this.chatListener);
        connection.addConnectionListener(connectionListener);
        DeliveryReceiptManager.getInstanceFor(connection)
                .addReceiptReceivedListener(receiptReceivedListener);
        roster = Roster.getInstanceFor(connection);
        roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);
        roster.addRosterListener(presenceStateListener);

        networkTimer = new Timer();
        connectTimer = new Timer();
        networkTimer.scheduleAtFixedRate(new TryXMPPConnection(), 0,  retryInterval* 1000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Logger.d(this, "onDestry");
        networkTimer.cancel();

        ChatManager.getInstanceFor(connection).removeChatListener(this.chatListener);
        DeliveryReceiptManager.getInstanceFor(connection).removeReceiptReceivedListener(receiptReceivedListener);
        Roster.getInstanceFor(connection).removeRosterListener(presenceStateListener);
        connection.removeConnectionListener(connectionListener);

        try {
            connection.disconnect(new Presence(Presence.Type.unavailable));
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }

        super.onDestroy();
    }

//    class CheckConnection extends TimerTask {
//        @Override
//        public void run() {
//            checkConnection();
//        }
//    }
//
//    private boolean checkConnection() {
//        try {
//            if(pingManager.pingMyServer()) {
//                connectTimer.cancel();
//                Logger.d(this, "Ping true. ");
//                if(ForegroundDetector.getInstance().isForeground()) {
//                    try {
//                        if(!connection.isConnected()) {
//                            connection.connect();
//                            sendUnsentMessages();
//                        }
//                    } catch (SmackException e) {
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    } catch (XMPPException e) {
//                        e.printStackTrace();
//                    }
//                } else {
//                    Logger.d(this, "Disconnect");
//                    connection.disconnect(new Presence(Presence.Type.unavailable));
//                }
//            } else {
//                Logger.d(this, "Ping false. ");
//                connection.disconnect();
//            }
//        } catch (SmackException.NotConnectedException e) {
//            connectTimer = new Timer();
//            connectTimer.scheduleAtFixedRate(new TryConnect(), 0, 500);
//            e.printStackTrace();
//        }
//        return true;
//    }
//
//    class TryConnect extends TimerTask {
//        @Override
//        public void run() {
//            tryConnect();
//        }
//    }
//
//    private boolean tryConnect() {
//        Logger.d(this, "Try Connect");
//        try {
//            connection.connect();
//        } catch (SmackException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (XMPPException e) {
//            e.printStackTrace();
//        }
//        return true;
//    }

    class TryXMPPConnection extends TimerTask{
        @Override
        public void run() {
            tryConnect();
        }
    }

    private boolean tryConnect() {
        // TODO: BUGGY
        if(ForegroundDetector.getInstance().isForeground()) {
            if(!XMPPManager.getInstance().getConnection().isConnected() && XMPPManager.getInstance().getConnection().isConnected()) {
                connection = XMPPManager.getInstance().getConnection();
            } else {
                if(!isOnlineNotified) {
                    retryInterval = RETRY_ONLINE;
                    broadcastConnectionStatus(true);
                    isOnlineNotified = true;
                    sendUnsentMessages();
                }
            }
        } else {
            try {
                connection.disconnect(new Presence(Presence.Type.unavailable));
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            }
        }
        return true;
    }


    private void sendUnsentMessages() {
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
                                        Logger.d(this, "Unsent message sent: "+messageResult.toString());
                                        broadcastDeliveryReceipt(messageResult.getMessageId(), messageResult.getChatId(), messageResult.getReceiptId(), messageResult.getMessageStatus());
                                    }
                                });
                            }
                        });
                    }
                });
    }

    private void storeAndbroadcastReceivedMessage(MessageResult messageId, ContactResult from) {
        messageStore.storeMessage(messageId).subscribe(new Subscriber<MessageResult>() {
            @Override
            public void onCompleted() {}
            @Override
            public void onError(Throwable e) {}

            @Override
            public void onNext(MessageResult messageResult) {
                if(messageResult!=null) {
                    Intent intent = new Intent(XMPP_ACTION_RCV_MSG);
                    intent.putExtra(XMPP_RESULT_MESSAGE, messageId);
                    intent.putExtra(XMPP_RESULT_CONTACT, from);
                    broadcaster.sendBroadcast(intent);

                    if(ForegroundDetector.getInstance().isBackground()) {
                        NotificationController.getInstance().showNotificationAndAlert(true);
                    }
                }
            }
        });
    }

    private void broadcastTypingState(String participant, ChatState state) {
        Intent intent = new Intent(XMPP_ACTION_RCV_STATE);
        intent.putExtra(XMPP_RESULT_STATE, state);
        intent.putExtra(XMPP_RESULT_FROM, participant);
        broadcaster.sendBroadcast(intent);
    }

    private void broadcastDeliveryReceipt(String messageId, String chatId, String deliveryReceiptId, MessageResult.MessageStatus messageStatus) {
        Intent intent = new Intent(XMPP_ACTION_RCV_RECEIPT);
        intent.putExtra(XMPP_RESULT_MSG_STATUS, messageStatus.name());
        intent.putExtra(XMPP_RESULT_CHAT_ID, chatId);
        intent.putExtra(XMPP_RESULT_RECEIPT_ID, deliveryReceiptId);
        intent.putExtra(XMPP_RESULT_MESSAGE_ID, messageId);
        broadcaster.sendBroadcast(intent);
    }

    private void broadcastConnectionStatus(boolean isAvailable) {
        Intent intent = new Intent(ACTION_INTERNET_CONNECTION_STATUS);
        intent.putExtra(CONNECTION_STATE, isAvailable);
        broadcaster.sendBroadcast(intent);
    }

    private void broadcastPresenceState(String jid, Presence.Type type) {
        Intent intent = new Intent(XMPP_ACTION_RCV_PRESENCE);
        intent.putExtra(XMPP_RESULT_PRESENCE_TYPE, type);
        intent.putExtra(XMPP_RESULT_PRESENCE_FROM, XMPPManager.getUserNameFromJid(jid));
        broadcaster.sendBroadcast(intent);
    }
}