package com.stairway.data.source.message;

import com.stairway.data.manager.Logger;
import com.stairway.data.manager.XMPPManager;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;

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
            String recipient = message.getChatId() + "@" + connection.getServiceName();

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

    public void sendMessageXMPP(XMPPConnection connection, MessageResult message, final Subscriber<? super MessageResult> subscriber) {
        Logger.v("Sending message");

        if(connection.isAuthenticated())
            Logger.v("[XMPP] Connection authenticated");
        else
            Logger.v("[XMPP] Connection authenticated");
        ChatManager chatManager = ChatManager.getInstanceFor(connection);
        Chat newChat = chatManager.createChat("test@spotlight.p1.im", (chat, receivedMessage) -> Logger.v("Received message :"+receivedMessage));

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
}
