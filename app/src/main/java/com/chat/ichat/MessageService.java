package com.chat.ichat;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.LongSparseArray;

import com.chat.ichat.api.ApiManager;
import com.chat.ichat.api.bot.BotApi;
import com.chat.ichat.api.bot.BotResponse;
import com.chat.ichat.api.message.MessageDataResponse;
import com.chat.ichat.api.user.UserApi;
import com.chat.ichat.api.user.UserResponse;
import com.chat.ichat.api.user._User;
import com.chat.ichat.application.SpotlightApplication;
import com.chat.ichat.core.GsonProvider;
import com.chat.ichat.core.Logger;
import com.chat.ichat.core.NotificationController;
import com.chat.ichat.core.ReadReceiptExtension;
import com.chat.ichat.db.BotDetailsStore;
import com.chat.ichat.db.ContactStore;
import com.chat.ichat.db.MessageStore;
import com.chat.ichat.db.core.DatabaseManager;
import com.chat.ichat.db.core.SQLiteContract;
import com.chat.ichat.models.AudioMessage;
import com.chat.ichat.models.ContactResult;
import com.chat.ichat.models.ImageMessage;
import com.chat.ichat.models.Message;
import com.chat.ichat.models.MessageResult;
import com.chat.ichat.screens.new_chat.NewChatActivity;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.ReconnectionManager;
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
import org.jivesoftware.smackx.ping.PingFailedListener;
import org.jivesoftware.smackx.ping.PingManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.ReceiptReceivedListener;
import org.joda.time.DateTime;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.Observable;
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

    private Timer networkTimer = null;

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
    private PingFailedListener pingFailedListener;
    private PingManager pingManager;

    private boolean isConnectionAvailable = true;

    private LocalBroadcastManager broadcaster;
    private Roster roster;

    private DatabaseManager databaseManager;

    private LongSparseArray<MessageData> messageDatas;

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
        databaseManager = DatabaseManager.getInstance();
        messageDatas = new LongSparseArray<>();

        //init
        broadcastConnectionStatus(false);

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
            public void entriesUpdated(Collection<String> addresses) {}

            @Override
            public void entriesDeleted(Collection<String> addresses) {}

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

                            messageStore.getMessage(readReceiptExtension.getLastMessageReceiptId())
                                    .subscribe(new Subscriber<MessageResult>() {
                                        @Override
                                        public void onCompleted() {}

                                        @Override
                                        public void onError(Throwable e) {
                                            broadcastDeliveryReceipt("", participant, readReceiptExtension.getLastMessageReceiptId(), MessageResult.MessageStatus.READ);
                                        }

                                        @Override
                                        public void onNext(MessageResult messageResult) {
                                            broadcastDeliveryReceipt(messageResult.getMessageId(), participant, readReceiptExtension.getLastMessageReceiptId(), MessageResult.MessageStatus.READ);
                                            messageStore.updateAllMessageStatus(readReceiptExtension.getLastMessageReceiptId(), MessageResult.MessageStatus.READ)
                                                    .subscribe(new Subscriber<Boolean>() {
                                                        @Override
                                                        public void onCompleted() {}

                                                        @Override
                                                        public void onError(Throwable e) {}

                                                        @Override
                                                        public void onNext(Boolean aBoolean) {}
                                                    });
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
                broadcastConnectionStatus(true);
                sendUnsentMessages();
            }

            @Override
            public void authenticated(XMPPConnection connection, boolean resumed) {
                Logger.d(this, "[-]Authenticated");
            }

            @Override
            public void connectionClosed() {
                Logger.d(this, "[-]ConnectionClosed");
                broadcastConnectionStatus(false);
            }

            @Override
            public void connectionClosedOnError(Exception e) {
                Logger.d(this, "[-]connectionClosedOnError");
                broadcastConnectionStatus(false);
            }
            @Override
            public void reconnectionSuccessful() {
                Logger.d(this, "[-]reconnectionSuccessful");
                broadcastConnectionStatus(true);

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

        connection.addConnectionListener(connectionListener);

        this.pingFailedListener = new PingFailedListener() {
            @Override
            public void pingFailed() {
                connection.disconnect();
                Logger.d(this, "[-]Ping failed");
                broadcastConnectionStatus(false);
            }
        };

        ChatManager.getInstanceFor(connection).addChatListener(this.chatListener);
        DeliveryReceiptManager.getInstanceFor(connection)
                .addReceiptReceivedListener(receiptReceivedListener);
        roster = Roster.getInstanceFor(connection);
        roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);
        roster.addRosterListener(presenceStateListener);

        networkTimer = new Timer();
        networkTimer.scheduleAtFixedRate(new TryXMPPConnection(), 0,  3* 1000);
        pingManager = PingManager.getInstanceFor(connection);
        pingManager.setPingInterval(5);
        pingManager.registerPingFailedListener(pingFailedListener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.d(this, "onStartCommand");
        sendUnsentMessages();
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
        pingManager.unregisterPingFailedListener(pingFailedListener);

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

    private boolean tryConnect() {
        if(ForegroundDetector.getInstance().isForeground()) {
            if(!isNetworkOnline() || !XMPPManager.getInstance().getConnection().isConnected()) {
                if(isNetworkOnline())
                    connection = XMPPManager.getInstance().getConnection();
                broadcastConnectionStatus(false);
            } else {
                broadcastConnectionStatus(true);
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
        getMessagesFromStore();
        sendCacheMessages();
    }

    private void getMessagesFromStore() {
        Logger.d(this, "sending unsentMsgs");

        SQLiteDatabase db = databaseManager.openConnection();

        String selection = SQLiteContract.MessagesContract.COLUMN_MESSAGE_STATUS + "=?";

        String[] selectionArgs = {MessageResult.MessageStatus.NOT_SENT.name()};
        String[] columns = {
                SQLiteContract.MessagesContract.COLUMN_CHAT_ID,
                SQLiteContract.MessagesContract.COLUMN_FROM_ID,
                SQLiteContract.MessagesContract.COLUMN_MESSAGE,
                SQLiteContract.MessagesContract.COLUMN_MESSAGE_STATUS,
                SQLiteContract.MessagesContract.COLUMN_ROW_ID,
                SQLiteContract.MessagesContract.COLUMN_CREATED_AT};

        try {
            Cursor cursor = db.query(SQLiteContract.MessagesContract.TABLE_NAME, columns, selection, selectionArgs, null, null, "rowid ASC");
            cursor.moveToFirst();
            Logger.d(this, "MESSAGE:");
            while(!cursor.isAfterLast()) {
                String chatId = cursor.getString(cursor.getColumnIndex(SQLiteContract.MessagesContract.COLUMN_CHAT_ID));
                String fromId = cursor.getString(cursor.getColumnIndex(SQLiteContract.MessagesContract.COLUMN_FROM_ID));
                String message = cursor.getString(cursor.getColumnIndex(SQLiteContract.MessagesContract.COLUMN_MESSAGE));
                String delivery = cursor.getString(cursor.getColumnIndex(SQLiteContract.MessagesContract.COLUMN_MESSAGE_STATUS));
                String messageId = cursor.getString(cursor.getColumnIndex(SQLiteContract.MessagesContract.COLUMN_ROW_ID));
                String time = cursor.getString(cursor.getColumnIndex(SQLiteContract.MessagesContract.COLUMN_CREATED_AT));
                MessageResult.MessageStatus deliveryStatus = MessageResult.MessageStatus.valueOf(delivery);
                if(deliveryStatus != MessageResult.MessageStatus.NOT_SENT)
                    continue;

                long mess = cursor.getLong(cursor.getColumnIndex(SQLiteContract.MessagesContract.COLUMN_ROW_ID));
                MessageResult msg = new MessageResult(chatId, fromId, message);
                msg.setMessageStatus(deliveryStatus);
                msg.setTime(DateTime.parse(time));
                msg.setMessageId(messageId);

                MessageData messageData = new MessageData(msg, mess);
                MessageData t = messageDatas.get(mess);

                if(t==null ||  !t.isOpen()) {
                    messageDatas.put(mess, messageData);
                }
                cursor.moveToNext();
            }
            cursor.close();
            databaseManager.closeConnection();
        } catch (Exception e) {
            Logger.e(this, "MessageStore sqlite error: getUnsentMessages()-"+e.getMessage());
            databaseManager.closeConnection();
        }
    }

    public void sendCacheMessages() {
        for(int i = 0; i < messageDatas.size(); i++) {
            long key = messageDatas.keyAt(i);
            // get the object by the key.
            MessageData messageData = messageDatas.get(key);

            MessageResult messageResult = messageData.openAndGet();
            if(messageResult==null)
                continue;

            Message parseMessage = GsonProvider.getGson().fromJson(messageResult.getMessage(), Message.class);

            if(parseMessage.getMessageType() == Message.MessageType.image) {
                if(parseMessage.getImageMessage().getImageUrl() == null || parseMessage.getImageMessage().getImageUrl().isEmpty()) {
                    uploadImage(parseMessage.getImageMessage().getFileUri(), messageResult.getChatId())
                            .subscribeOn(Schedulers.newThread())
                            .subscribe(new Subscriber<MessageDataResponse>() {
                                @Override
                                public void onCompleted() {}

                                @Override
                                public void onError(Throwable e) {
                                    messageDatas.put(messageData.getMessageId(), messageData.close());
                                }

                                @Override
                                public void onNext(MessageDataResponse dataResponse) {
                                    Message m = new Message();
                                    ImageMessage imageMessage = new ImageMessage();
                                    imageMessage.setImageUrl(dataResponse.getDataUrl());
                                    imageMessage.setFileUri(parseMessage.getImageMessage().getFileUri());
                                    m.setImageMessage(imageMessage);
                                    messageResult.setMessage(GsonProvider.getGson().toJson(m));
                                    messageStore.updateMessage(messageResult)
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribeOn(Schedulers.io())
                                            .subscribe(new Subscriber<MessageResult>() {
                                                @Override
                                                public void onCompleted() {}

                                                @Override
                                                public void onError(Throwable e) {
                                                    e.printStackTrace();
                                                    messageDatas.put(messageData.getMessageId(), messageData.close());
                                                }

                                                @Override
                                                public void onNext(MessageResult messageResult) {
                                                    sendMessage(messageResult, messageData.getMessageId());
                                                }
                                            });
                                }
                            });
                } else {
                    sendMessage(messageResult, messageData.getMessageId());
                }
            } else if(parseMessage.getMessageType() == Message.MessageType.audio) {
                if(parseMessage.getAudioMessage().getAudioUrl() == null || parseMessage.getAudioMessage().getAudioUrl().isEmpty()) {
                    uploadAudio(parseMessage.getAudioMessage().getFileUri())
                            .subscribeOn(Schedulers.newThread())
                            .subscribe(new Subscriber<MessageDataResponse>() {
                                @Override
                                public void onCompleted() {}

                                @Override
                                public void onError(Throwable e) {
                                    messageDatas.put(messageData.getMessageId(), messageData.close());
                                }

                                @Override
                                public void onNext(MessageDataResponse dataResponse) {
                                    Message m = new Message();
                                    AudioMessage audioMessage1 = new AudioMessage();
                                    audioMessage1.setAudioUrl(dataResponse.getDataUrl());
                                    audioMessage1.setFileUri(parseMessage.getAudioMessage().getFileUri());
                                    m.setAudioMessage(audioMessage1);
                                    messageResult.setMessage(GsonProvider.getGson().toJson(m));
                                    messageStore.updateMessage(messageResult)
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribeOn(Schedulers.io())
                                            .subscribe(new Subscriber<MessageResult>() {
                                                @Override
                                                public void onCompleted() {}

                                                @Override
                                                public void onError(Throwable e) {
                                                    e.printStackTrace();
                                                    messageDatas.put(messageData.getMessageId(), messageData.close());
                                                }

                                                @Override
                                                public void onNext(MessageResult messageResult) {
                                                    sendMessage(messageResult, messageData.getMessageId());
                                                }
                                            });
                                }
                            });
                } else {
                    sendMessage(messageResult, messageData.getMessageId());
                }
            } else {
                sendMessage(messageResult, messageData.getMessageId());
            }
        }
    }

    private void sendMessage(MessageResult messageResult, long messageId) {
        Logger.d(this, "sending message: "+messageResult.toString());
        messageApi.sendMessage(messageResult).subscribe(new Subscriber<MessageResult>() {
            @Override
            public void onCompleted() {}
            @Override
            public void onError(Throwable e) {
                messageDatas.put(messageId, messageDatas.get(messageId).close());
            }

            @Override
            public void onNext(MessageResult messageResult) {
                if(messageResult.getMessageStatus() != MessageResult.MessageStatus.NOT_SENT) {
                    messageStore.updateMessage(messageResult).subscribe(new Subscriber<MessageResult>() {
                        @Override
                        public void onCompleted() {}

                        @Override
                        public void onError(Throwable e) {
                            messageDatas.put(messageId, messageDatas.get(messageId).close());
                        }

                        @Override
                        public void onNext(MessageResult messageResult) {
                            messageDatas.remove(messageId);
                            broadcastDeliveryReceipt(messageResult.getMessageId(), messageResult.getChatId(), messageResult.getReceiptId(), messageResult.getMessageStatus());
                        }
                    });
                } else {
                    messageDatas.put(messageId, messageDatas.get(messageId).close());
                }
            }
        });
    }

    private Observable<MessageDataResponse> uploadImage(String fileUri, String userId) {
        File image = saveBitmapToFile(new File(fileUri));
        Logger.d(this, "File size(MB): "+image.length()/(1024*1024));
        String filename = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        int i = image.getName().lastIndexOf('.');
        if (i > 0) {
            filename = filename + "_" + userId+ image.getName().substring(i);
        } else {
            filename = filename + "." + image.getName();
        }
        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), image);
        MultipartBody.Part imageFileBody = MultipartBody.Part.createFormData("image", filename, requestBody);
        return ApiManager.getMessageApi().uploadImageData(imageFileBody);
    }

    private Observable<MessageDataResponse> uploadAudio(String fileUri) {
        File image = new File(fileUri);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String filename = timeStamp;
        int i = image.getName().lastIndexOf('.');
        if (i > 0) {
            filename = filename + image.getName().substring(i);
        } else {
            filename = filename + "." + image.getName();
        }
        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), image);
        MultipartBody.Part audioFileBody = MultipartBody.Part.createFormData("audio", filename, requestBody);
        return ApiManager.getMessageApi().uploadAudioData(audioFileBody);
    }

    public File saveBitmapToFile(File file) {
        try {
            // BitmapFactory options to downsize the image
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            o.inSampleSize = 6;
            // factor of downsizing the image

            FileInputStream inputStream = new FileInputStream(file);
            //Bitmap selectedBitmap = null;
            BitmapFactory.decodeStream(inputStream, null, o);
            inputStream.close();

            // The new size we want to scale to
            final int REQUIRED_SIZE=75;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while(o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            inputStream = new FileInputStream(file);

            Bitmap selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2);
            inputStream.close();

            // here i override the original image file
            file.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(file);

            selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 100 , outputStream);

            return file;
        } catch (Exception e) {
            return null;
        }
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
        if(isAvailable)
            sendCacheMessages();
        if(!isConnectionAvailable && isAvailable || isConnectionAvailable && !isAvailable) {
            sendUnsentMessages();
            isConnectionAvailable = isAvailable;
            Logger.d(this, "[-]BroadCastConnection status: "+isConnectionAvailable);
            Intent intent = new Intent(ACTION_INTERNET_CONNECTION_STATUS);
            intent.putExtra(CONNECTION_STATE, isAvailable);
            broadcaster.sendBroadcast(intent);
        }
    }

    private void broadcastPresenceState(String jid, Presence.Type type) {
        Intent intent = new Intent(XMPP_ACTION_RCV_PRESENCE);
        intent.putExtra(XMPP_RESULT_PRESENCE_TYPE, type);
        intent.putExtra(XMPP_RESULT_PRESENCE_FROM, XMPPManager.getUserNameFromJid(jid));
        broadcaster.sendBroadcast(intent);
    }

    public static boolean isNetworkOnline() {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) SpotlightApplication.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            if (netInfo != null && (netInfo.isConnectedOrConnecting() || netInfo.isAvailable())) {
                return true;
            }

            netInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                return true;
            } else {
                netInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                    return true;
                }
            }
        } catch (Exception e) {
            return true;
        }
        return false;
    }

    public class MessageData {
        private MessageResult messageResult;
        private AtomicInteger counter ;
        private long messageId;

        public MessageData(MessageResult messageResult, long messageId) {
            this.messageResult = messageResult;
            this.counter = new AtomicInteger(0);
            this.messageId = messageId;
        }

        public synchronized MessageResult openAndGet() {
            if(counter.intValue() == 0) {
                counter.incrementAndGet();
                return messageResult;
            }
            return null;
        }

        public boolean isOpen() {
            return counter.intValue() == 1;
        }

        public synchronized MessageData close() {
            if(counter.intValue() == 1) {
                counter.decrementAndGet();
            }
            return MessageData.this;
        }

        public long getMessageId() {
            return messageId;
        }
    }
}