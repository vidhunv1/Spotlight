package com.stairway.spotlight.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import com.stairway.spotlight.AccessTokenManager;
import com.stairway.spotlight.R;
import com.stairway.spotlight.models.MessageResult;

import org.jivesoftware.smackx.chatstates.ChatState;

import java.util.ArrayList;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by vidhun on 05/07/16.
 */
public abstract class BaseActivity extends AppCompatActivity implements  XmppService.XmppServiceCallback{

    public static boolean isAppWentToBg = false;
    public static boolean isWindowFocused = false;
    public static boolean isBackPressed = false;

    BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.statusBar));
        }

        //Register MessageReceiver
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(XmppService.XMPP_ACTION_RCV_MSG)) {
                    MessageResult s = (MessageResult) intent.getSerializableExtra(XmppService.XMPP_RESULT_MESSAGE);
                    Logger.d(this, "Message received: "+s);
                    onMessageReceived(s);
                } else if(intent.getAction().equals(XmppService.XMPP_ACTION_RCV_STATE)) {
                    String from = intent.getStringExtra(XmppService.XMPP_RESULT_FROM);
                    ChatState chatState = (ChatState) intent.getSerializableExtra(XmppService.XMPP_RESULT_STATE);
                    onChatStateReceived(from, chatState);
                } else if(intent.getAction().equals(XmppService.XMPP_ACTION_RCV_RECEIPT)) {
                    String chatId = intent.getStringExtra(XmppService.XMPP_RESULT_CHAT_ID);
                    String deliveryReceiptId = intent.getStringExtra(XmppService.XMPP_RESULT_RECEIPT_ID);
                    MessageResult.MessageStatus messageStatus = MessageResult.MessageStatus.valueOf(intent.getStringExtra(XmppService.XMPP_RESULT_MSG_STATUS));
                    onMessageStatusReceived(chatId, deliveryReceiptId, messageStatus);
                }
            }
        };

        if(AccessTokenManager.getInstance().hasAccessToken()) {
            Intent intent = new Intent(this, XmppService.class);
            intent.putExtra(XmppService.TAG_ACTIVITY_NAME, this.getClass().getName());
            startService(intent);
        }
    }

    @Override
    protected void onStart() {
        onApplicationToForeground();
        IntentFilter filter = new IntentFilter(XmppService.XMPP_ACTION_RCV_STATE);
        filter.addAction(XmppService.XMPP_ACTION_RCV_MSG);
        filter.addAction(XmppService.XMPP_ACTION_RCV_RECEIPT);
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver), filter);
        super.onStart();
    }

    @Override
    protected void onStop() {
        onApplicationToBackground();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        isWindowFocused = hasFocus;
        if (isBackPressed && !hasFocus) {
            isBackPressed = false;
            isWindowFocused = true;
        }
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void networkOnline() {
//        Logger.d(this, "HAS INTERNET CONNECTION");
    }

    @Override
    public void networkOffline() {
//        Logger.d(this, "HAS NO INTERNET CONNECTION");
    }

    public void onApplicationToBackground() {
        if (!isWindowFocused) {
            isAppWentToBg = true;
            stopService(new Intent(this, XmppService.class));
        }
    }

    private void onApplicationToForeground() {
        if (isAppWentToBg) {
            isAppWentToBg = false;
            Intent intent = new Intent(this, XmppService.class);
            intent.putExtra(XmppService.TAG_ACTIVITY_NAME, this.getClass().getName());
            startService(intent);
        }
    }

    public void onMessageReceived(MessageResult messageId){
        Logger.d(this, "MessageId "+messageId);
    }

    public void onChatStateReceived(String from, ChatState chatState) { Logger.d(this, "chatState: "+chatState.name()+", from "+from);}

    public void onMessageStatusReceived(String chatId, String deliveryReceiptId, MessageResult.MessageStatus messageStatus) {}
}
