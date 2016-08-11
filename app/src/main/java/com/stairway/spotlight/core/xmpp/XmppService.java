package com.stairway.spotlight.core.xmpp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.stairway.data.manager.Logger;
import com.stairway.data.manager.XMPPManager;

import org.jivesoftware.smack.ConnectionListener;

public class XmppService extends Service {
    private XMPPManager connection;
    private String userId;
    public XmppService(XMPPManager connection, String userId) {
        this.connection = connection;
        this.userId = userId;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: No binding implemted.
        return null;
    }


    @Override
    public void onCreate() {
        connection.getConnection().addConnectionListener(new ConnectionListener() {
            @Override
            public void connected(org.jivesoftware.smack.XMPPConnection connection) {
                Logger.v("Connected XMPP...");
            }

            @Override
            public void authenticated(org.jivesoftware.smack.XMPPConnection connection, boolean resumed) {
                //TODO: send all unsent messages.

//                Logger.v("Authenticated?"+connection.isAuthenticated());
//
//                ChatManager chatManager = ChatManager.getInstanceFor(connection);
//                Chat newChat = chatManager.createChat("test@spotlight.p1.im", (chat, message) -> Logger.d("Received message :"+message));
//
//                try {
//                    newChat.sendMessage("hello");
//                }  catch (SmackException.NotConnectedException e) {
//                    Logger.e("XMPP error: "+e);
//                }
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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
