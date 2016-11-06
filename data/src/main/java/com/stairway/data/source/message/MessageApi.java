package com.stairway.data.source.message;

import android.util.Log;

import com.stairway.data.manager.Logger;
import com.stairway.data.manager.XMPPManager;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smackx.iqlast.LastActivityManager;

import java.util.Collection;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by vidhun on 06/08/16.
 */
public class MessageApi {
    private XMPPManager connection;

    public MessageApi(XMPPManager connection) {
        this.connection = connection;
    }

    public Observable<MessageResult> sendMessage(MessageResult message) {
        Logger.d("Sending message"+message.getMessage()+" to "+message.getChatId());

        Observable<MessageResult> sendMessage = Observable.create(subscriber -> {
            String recipient = XMPPManager.getJidFromUserName(message.getChatId());

            if(!connection.getConnection().isAuthenticated()) {
                Logger.v("XMPP Not connected");
                connection.initConnection();

                connection.getConnection().addConnectionListener(new ConnectionListener() {
                    @Override
                    public void connected(XMPPConnection connection) {
                        Logger.v("Connected XMPP...");
                    }

                    @Override
                    public void authenticated(XMPPConnection connection, boolean resumed) {
                        Logger.v("Authenticated?"+connection.isAuthenticated());
                        sendMessageXMPP(connection, message, subscriber);

                    }

                    @Override
                    public void connectionClosed() {

                    }

                    @Override
                    public void connectionClosedOnError(Exception e) {

                    }

                    @Override
                    public void reconnectionSuccessful() {

                    }

                    @Override
                    public void reconnectingIn(int seconds) {
                        Logger.v("Reconnecting in"+seconds);
                    }

                    @Override
                    public void reconnectionFailed(Exception e) {

                    }
                });
            } else {
                Logger.v("XMPP connected");
                sendMessageXMPP(connection.getConnection(), message, subscriber);
            }
        });

        return sendMessage;
    }

    public Observable<MessageResult> receiveMessages() {
        Observable<MessageResult> receiveMessages = Observable.create(subscriber -> {
            ChatManager chatManager = ChatManager.getInstanceFor(connection.getConnection());

            chatManager.addChatListener(new ChatManagerListener() {
                @Override
                public void chatCreated(Chat chat, boolean createdLocally) {
                    chat.addMessageListener(new ChatMessageListener() {
                        @Override
                        public void processMessage(Chat chat, Message message) {
                            String participant = chat.getParticipant().split("@")[0];
                            MessageResult receivedMessage = new MessageResult(participant, participant, message.getBody());
                            receivedMessage.setDeliveryStatus(MessageResult.DeliveryStatus.NOT_AVAILABLE);
                            subscriber.onNext(receivedMessage);
                        }
                    });
                }
            });

        });

        return receiveMessages;
    }

    private void sendMessageXMPP(XMPPConnection connection, MessageResult message, final Subscriber<? super MessageResult> subscriber) {
        Logger.v("Sending message");

        if(connection.isAuthenticated())
            Logger.v("[XMPP] Connection authenticated");
        else
            Logger.v("[XMPP] Connection authenticated");
        ChatManager chatManager = ChatManager.getInstanceFor(connection);
        Chat newChat = chatManager.createChat(message.getChatId()+"@"+connection.getServiceName());

        try {
            newChat.sendMessage(message.getMessage());

            // TODO: check for acknowledgement
            if(connection.isAuthenticated())
                message.setDeliveryStatus(MessageResult.DeliveryStatus.SENT);
            else
                message.setDeliveryStatus(MessageResult.DeliveryStatus.NOT_SENT);
            subscriber.onNext(message);
        }  catch (SmackException.NotConnectedException e) {
            message.setDeliveryStatus(MessageResult.DeliveryStatus.NOT_SENT);
            subscriber.onNext(message);
            Logger.e("XMPP error: "+e);
        }
    }

    // userId: 91-9999999999
    public Observable<Presence.Mode> getPresence(String userId) {
        String jid = XMPPManager.getJidFromUserName(userId);
        Observable<Presence.Mode> getPresence;
        getPresence = Observable.create(subscriber -> {
            //initial presence
            subscriber.onNext(Presence.Mode.away);
            Roster roster = Roster.getInstanceFor(connection.getConnection());
            roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);

            boolean pre = roster.addRosterListener(new RosterListener() {

                @Override
                public void entriesAdded(Collection<String> addresses) {

                }

                @Override
                public void entriesUpdated(Collection<String> addresses) {

                }

                @Override
                public void entriesDeleted(Collection<String> addresses) {

                }

                public void presenceChanged(Presence presence) {
                    if(presence.getFrom().split("/")[0].equals(jid))
                        subscriber.onNext(presence.getMode());
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
            LastActivityManager activity = LastActivityManager.getInstanceFor(connection.getConnection());
            try {
                subscriber.onNext(activity.getLastActivity(jid).lastActivity);
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
}
