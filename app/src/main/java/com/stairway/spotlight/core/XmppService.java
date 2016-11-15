package com.stairway.spotlight.core;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import com.stairway.data.manager.Logger;
import com.stairway.data.manager.XMPPManager;
import com.stairway.data.source.message.MessageResult;
import com.stairway.data.source.message.MessageStore;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;

import java.io.Serializable;
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
    static final public String XMPP_MESSAGE_RESULT = "com.stairway.spotlight.core.XmppService.XMPP_MSG";

    private static boolean isReceivingMessages = false;

    private String activity_name;
    private boolean isOnlineNotified = false;
    private int retryInterval = 100;
    private MessageStore messageStore;
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
        broadcaster = LocalBroadcastManager.getInstance(this);
        activity_name = intent.getStringExtra(TAG_ACTIVITY_NAME);
        messageStore = new MessageStore();

        try {
            xmppServiceCallback = (XmppServiceCallback) Class.forName(activity_name).newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TryXMPPConnection(), 0,  retryInterval* 1000);
        receiveMessages();

        return super.onStartCommand(intent, flags, startId);
    }

    public void receiveMessages() {
        if(isReceivingMessages)
            return;

        isReceivingMessages = true;
        ChatManager chatManager = ChatManager.getInstanceFor(XMPPManager.getConnection());

        chatManager.addChatListener((chat, createdLocally) -> {
            chat.addMessageListener((chat1, message) -> {
                String participant = chat.getParticipant().split("@")[0];
                MessageResult receivedMessage = new MessageResult(participant, participant, message.getBody());
                receivedMessage.setMessageStatus(MessageResult.MessageStatus.UNSEEN);

                messageStore.storeMessage(receivedMessage).subscribe(new Subscriber<MessageResult>() {
                    @Override
                    public void onCompleted() {}
                    @Override
                    public void onError(Throwable e) {}

                    @Override
                    public void onNext(MessageResult messageResult) {
                        broadcastMessageReceived(receivedMessage);
                    }
                });
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
            XMPPManager.getConnection().disconnect(new Presence(Presence.Type.unavailable));
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    private boolean tryConnect(){
        try {
            Logger.d("TryConnect");
            // TODO: BUGGY
            if(!XMPPManager.getConnection().isConnected()) {
                XMPPManager.getConnection().connect().login();
            }

            if(!isOnlineNotified) {
                xmppServiceCallback.networkOnline();
                isOnlineNotified = true;
            }
            return true;
        } catch (SmackException.ConnectionException e) {
            xmppServiceCallback.networkOffline();
            isOnlineNotified = false;
            e.printStackTrace();
            return false;
        } catch (SmackException.AlreadyConnectedException e) {
            return true;
        } catch (SmackException.AlreadyLoggedInException e) {
            return true;
        } catch (Exception e) {
            Logger.d(e.getMessage());
            return false;
        }
    }

    public void broadcastMessageReceived(MessageResult messageId) {
        Intent intent = new Intent(XMPP_ACTION_RCV_MSG);
        if(messageId != null)
            intent.putExtra(XMPP_MESSAGE_RESULT, messageId);
        broadcaster.sendBroadcast(intent);
    }
}
