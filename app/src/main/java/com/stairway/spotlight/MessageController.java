package com.stairway.spotlight;

import com.stairway.spotlight.core.Logger;
import com.stairway.spotlight.core.ReadReceiptExtension;
import com.stairway.spotlight.db.ContactStore;
import com.stairway.spotlight.db.MessageStore;
import com.stairway.spotlight.models.ContactResult;
import com.stairway.spotlight.models.MessageResult;
import com.stairway.spotlight.screens.home.ChatItem;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.sm.StreamManagementException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jivesoftware.smackx.chatstates.packet.ChatStateExtension;
import org.jivesoftware.smackx.iqlast.LastActivityManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by vidhun on 29/01/17.
 */

public class MessageController {
    private static MessageController instance;

    public static void init(XMPPTCPConnection conn, MessageStore messageStore, ContactStore contactStore) {
        instance = new MessageController(conn, messageStore, contactStore);
    }

    public static MessageController getInstance() {
        if (instance == null) {
            Logger.d(MessageController.class, "[MessageController Not Initialized]");
            throw new IllegalStateException("[MessageController Not Initialized]");
        }

        return instance;
    }

    private XMPPTCPConnection conn;
    private MessageStore messageStore;
    private ContactStore contactStore;

    private MessageController(XMPPTCPConnection conn, MessageStore messageStore, ContactStore contactStore) {
        this.conn = conn;
        this.messageStore = messageStore;
        this.contactStore = contactStore;
    }

    public Observable<List<ChatItem>> getChatList() {
        return Observable.create(subscriber -> {
            messageStore.getChatList()
                    .subscribeOn(Schedulers.io())
                    .map(messageResults -> {
                        List<ChatItem> chatItems = new ArrayList<>(messageResults.size());
                        for (MessageResult messageResult : messageResults) {
                            contactStore.getContactByUserName(messageResult.getChatId()).subscribe(new Subscriber<ContactResult>() {
                                @Override
                                public void onCompleted() {}
                                @Override
                                public void onError(Throwable e) {}

                                @Override
                                public void onNext(ContactResult contactResult) {
                                    String name;
                                    if (contactResult != null)
                                        name = contactResult.getContactName();
                                    else {
                                        //TODO: get user details from server
                                        name = messageResult.getChatId();
                                    }

                                    chatItems.add(new ChatItem(
                                            messageResult.getChatId(),
                                            name,
                                            messageResult.getMessage(),
                                            messageResult.getTime(),
                                            messageResult.getUnSeenCount()));
                                }
                            });
                        }
                        return chatItems;
                    })
                    .subscribe(chats -> {
                        subscriber.onNext(chats);
                        subscriber.onCompleted();
                    });
        });
    }

    public Observable<MessageResult> sendMessage(MessageResult message){
        Logger.d(this, "Sending message"+message.getMessage()+" to "+message.getChatId());

        return Observable.create(subscriber -> {
            if(!this.conn.isAuthenticated()) {
                Logger.v(this, "XMPP Not connected");
                message.setMessageStatus(MessageResult.MessageStatus.NOT_SENT);
                subscriber.onNext(message);
            } else {
                ChatManager chatManager = ChatManager.getInstanceFor(this.conn);
                Chat newChat = chatManager.createChat(XMPPManager.getJidFromUserName(message.getChatId()));
                String deliveryReceiptId;
                try {
                    Message sendMessage = new Message(XMPPManager.getJidFromUserName(message.getChatId()));
                    deliveryReceiptId = DeliveryReceiptRequest.addTo(sendMessage);
                    message.setReceiptId(deliveryReceiptId);
                    sendMessage.addBody("en", message.getMessage());
                    newChat.sendMessage(sendMessage);

                    //Acknowledgement
                    if(this.conn.isSmEnabled()) {
                        this.conn.addStanzaIdAcknowledgedListener(sendMessage.getStanzaId(), packet -> {
                            message.setMessageStatus(MessageResult.MessageStatus.SENT);
                            subscriber.onNext(message);
                        });
                    }
                } catch (SmackException.NotConnectedException e) {
                    message.setMessageStatus(MessageResult.MessageStatus.NOT_SENT);
                    subscriber.onNext(message);
                    Logger.e(this, "XMPP error: "+e);
                }
                catch (StreamManagementException.StreamManagementNotEnabledException e) {
                    message.setMessageStatus(MessageResult.MessageStatus.NOT_SENT);
                    subscriber.onNext(message);
                    Logger.e(this, "Stream management not enabled");
                }
            }
        });
    }

    public Observable<Long> getLastActivity(String userId) {
        Logger.d(this, "Getting last activity");
        String jid = XMPPManager.getJidFromUserName(userId);
        return Observable.create(subscriber -> {
            LastActivityManager activity = LastActivityManager.getInstanceFor(this.conn);
            try {
                subscriber.onNext(activity.getLastActivity(jid).lastActivity);
                subscriber.onCompleted();
            } catch (SmackException.NoResponseException e) {
                e.printStackTrace();
                subscriber.onError(e);
            } catch (XMPPException.XMPPErrorException e) {
                e.printStackTrace();
                subscriber.onError(e);
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
                subscriber.onError(e);
            }
        });
    }

    //Typing/stopped typing indicators
    public Observable<Boolean> sendChatState(String chatId, ChatState chatState){
        return Observable.create(subscriber -> {
            ChatManager chatManager = ChatManager.getInstanceFor(this.conn);
            Chat newChat = chatManager.createChat(XMPPManager.getJidFromUserName(chatId));
            try {
                Message msg = new Message();
                msg.setBody(null);
                msg.addExtension(new ChatStateExtension(chatState));
                newChat.sendMessage(msg);
                subscriber.onNext(true);

            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
                subscriber.onError(e);
            }
        });
    }

    public Observable<Boolean> sendReadReceipt(MessageResult messageResult){
        return Observable.create(subscriber -> {
            Message message = new Message(XMPPManager.getJidFromUserName(messageResult.getChatId()));
            ReadReceiptExtension read = new ReadReceiptExtension(messageResult.getReceiptId());
            message.addExtension(read);
            try {
                if(messageResult.getReceiptId()==null && !messageResult.getReceiptId().isEmpty()) {
                    Logger.e(this, "receipt id not found");
                    subscriber.onError(new IllegalArgumentException("receipt id not found"));
                } else {
                    XMPPManager.getInstance().getConnection().sendStanza(message);
                    subscriber.onNext(true);
                    subscriber.onCompleted();
                }
            } catch (SmackException.NotConnectedException e) {
                subscriber.onError(e);
                e.printStackTrace();
            }
        });
    }
}