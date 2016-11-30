package com.stairway.data.source.message;

import com.stairway.data.config.Logger;
import com.stairway.data.config.XMPPManager;
import com.stairway.data.xmpp.ReadReceiptExtension;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.sm.StreamManagementException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jivesoftware.smackx.chatstates.packet.ChatStateExtension;
import org.jivesoftware.smackx.iqlast.LastActivityManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;

import java.util.Collection;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by vidhun on 06/08/16.
 */
public class MessageApi {
    private XMPPTCPConnection connection;
    public MessageApi(XMPPTCPConnection connection) {
        this.connection = connection;
    }

    public Observable<MessageResult> sendMessage(MessageResult message){
        Logger.d("Sending message"+message.getMessage()+" to "+message.getChatId());

        Observable<MessageResult> sendMessage = Observable.create(subscriber -> {
            String recipient = XMPPManager.getJidFromUserName(message.getChatId());

            if(!connection.isAuthenticated()) {
                Logger.v("XMPP Not connected");

                connection.addConnectionListener(new ConnectionListener() {
                    @Override
                    public void connected(XMPPConnection connection) {
                        Logger.v("Connected XMPP...");
                    }

                    @Override
                    public void authenticated(XMPPConnection connection, boolean resumed) {
                        Logger.v("Authenticated?"+connection.isAuthenticated());
                        sendMessageXMPP(message, subscriber);
                    }

                    @Override
                    public void connectionClosed() {}
                    @Override
                    public void connectionClosedOnError(Exception e) {}
                    @Override
                    public void reconnectionSuccessful() {}
                    @Override
                    public void reconnectingIn(int seconds) {
                        Logger.v("Reconnecting in"+seconds);
                    }
                    @Override
                    public void reconnectionFailed(Exception e) {}
                });
            } else {
                Logger.v("XMPP connected");
                sendMessageXMPP(message, subscriber);
            }
        });

        return sendMessage;
    }

    // userId: 91-9999999999
    public Observable<Presence.Type> getPresence(String userId) {
        String jid = XMPPManager.getJidFromUserName(userId);
        Observable<Presence.Type> getPresence;
        getPresence = Observable.create(subscriber -> {
            //initial presence
            Roster roster = Roster.getInstanceFor(connection);
            roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);

            boolean pre = roster.addRosterListener(new RosterListener() {
                @Override
                public void entriesAdded(Collection<String> addresses) {}
                @Override
                public void entriesUpdated(Collection<String> addresses) {}
                @Override
                public void entriesDeleted(Collection<String> addresses) {}

                public void presenceChanged(Presence presence) {
                    Logger.d("Presence received"+presence.getFrom()+", "+presence.getType());
                    if(presence.getFrom().split("/")[0].equals(jid))
                        subscriber.onNext(presence.getType());
                }
            });
        });

        return getPresence;
    }

    // userId: 91-9999999999
    public Observable<Long> getLastActivity(String userId) {
        Logger.d("Getting last activity");
        String jid = XMPPManager.getJidFromUserName(userId);
        Observable<Long> getLastActivity = Observable.create(subscriber -> {
            LastActivityManager activity = LastActivityManager.getInstanceFor(connection);
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
        return getLastActivity;
    }

    //Typing/stopped typing indicators
    public Observable<Boolean> sendChatState(String chatId, ChatState chatState){
        return Observable.create(subscriber -> {
            ChatManager chatManager = ChatManager.getInstanceFor(connection);
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
                    Logger.e("receipt id not found");
                    subscriber.onError(new IllegalArgumentException("receipt id not found"));
                } else {
                    XMPPManager.getConnection().sendStanza(message);
                    subscriber.onNext(true);
                    subscriber.onCompleted();
                }
            } catch (SmackException.NotConnectedException e) {
                subscriber.onError(e);
                e.printStackTrace();
            }
        });
    }

    private void sendMessageXMPP(MessageResult message, final Subscriber<? super MessageResult> subscriber){

        ChatManager chatManager = ChatManager.getInstanceFor(connection);
        Chat newChat = chatManager.createChat(XMPPManager.getJidFromUserName(message.getChatId()));
        String deliveryReceiptId;
        try {
            Message sendMessage = new Message(XMPPManager.getJidFromUserName(message.getChatId()));
            deliveryReceiptId = DeliveryReceiptRequest.addTo(sendMessage);
            message.setReceiptId(deliveryReceiptId);
            sendMessage.addBody("en", message.getMessage());
            newChat.sendMessage(sendMessage);

            //Acknowledgement
            if(connection.isSmEnabled()) {
                connection.addStanzaIdAcknowledgedListener(sendMessage.getStanzaId(), packet -> {
                    message.setMessageStatus(MessageResult.MessageStatus.SENT);
                    subscriber.onNext(message);
                });
            }
        } catch (SmackException.NotConnectedException e) {
            message.setMessageStatus(MessageResult.MessageStatus.NOT_SENT);
            subscriber.onNext(message);
            Logger.e("XMPP error: "+e);
        }
        catch (StreamManagementException.StreamManagementNotEnabledException e) {
            message.setMessageStatus(MessageResult.MessageStatus.SENT);
            subscriber.onNext(message);
            Logger.e("Stream management not enabled");
        }
    }
}
