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
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jivesoftware.smackx.chatstates.packet.ChatStateExtension;
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

                connection.getConnection().addConnectionListener(new ConnectionListener() {
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
            Roster roster = Roster.getInstanceFor(connection.getConnection());
            roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);

            boolean pre = roster.addRosterListener(new RosterListener() {

                @Override
                public void entriesAdded(Collection<String> addresses) {}

                @Override
                public void entriesUpdated(Collection<String> addresses) {}

                @Override
                public void entriesDeleted(Collection<String> addresses) {

                }

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
            LastActivityManager activity = LastActivityManager.getInstanceFor(connection.getConnection());
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
            ChatManager chatManager = ChatManager.getInstanceFor(connection.getConnection());
            Chat newChat = chatManager.createChat(chatId + "@" + connection.getConnection().getServiceName());
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

    private void sendMessageXMPP(MessageResult message, final Subscriber<? super MessageResult> subscriber) {
        Logger.v("Sending message");

        if(connection.getConnection().isAuthenticated())
            Logger.v("[XMPP] Connection authenticated");
        else
            Logger.v("[XMPP] Connection authenticated");
        ChatManager chatManager = ChatManager.getInstanceFor(connection.getConnection());
        Chat newChat = chatManager.createChat(message.getChatId()+"@"+connection.getConnection().getServiceName());

        try {
            newChat.sendMessage(message.getMessage());

            // TODO: check for acknowledgement
            if(connection.getConnection().isAuthenticated())
                message.setMessageStatus(MessageResult.MessageStatus.SENT);
            else
                message.setMessageStatus(MessageResult.MessageStatus.NOT_SENT);
            subscriber.onNext(message);
        }  catch (SmackException.NotConnectedException e) {
            message.setMessageStatus(MessageResult.MessageStatus.NOT_SENT);
            subscriber.onNext(message);
            Logger.e("XMPP error: "+e);
        }
    }
}
