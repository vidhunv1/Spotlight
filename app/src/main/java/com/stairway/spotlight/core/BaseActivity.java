package com.stairway.spotlight.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

import com.stairway.data.manager.Logger;
import com.stairway.data.manager.XMPPManager;
import com.stairway.data.source.message.MessageResult;
import com.stairway.spotlight.application.SpotlightApplication;
import com.stairway.spotlight.core.di.component.ComponentContainer;
import com.stairway.spotlight.core.di.component.UserSessionComponent;
import com.stairway.spotlight.screens.home.HomeActivity;

import org.jivesoftware.smackx.chatstates.ChatState;

import java.util.ArrayList;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by vidhun on 05/07/16.
 */
public abstract class BaseActivity extends AppCompatActivity implements BaseFragment.BackHandlerInterface, XmppService.XmppServiceCallback{
    private List<BaseFragment> baseFragmentList = new ArrayList<>();
    private XMPPManager connection;
    private UserSessionComponent userSessionComponent;

    public static boolean isAppWentToBg = false;
    public static boolean isWindowFocused = false;
    public static boolean isBackPressed = false;

    BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        injectComponent(((SpotlightApplication) getApplication()).getComponentContainer());

        //Register MessageReceiver
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(XmppService.XMPP_ACTION_RCV_MSG)) {
                    MessageResult s = (MessageResult) intent.getSerializableExtra(XmppService.XMPP_RESULT_MESSAGE);
                    Logger.d("Message received: "+s);
                    onMessageReceived(s);
                } else if(intent.getAction().equals(XmppService.XMPP_ACTION_RCV_STATE)) {
                    String from = intent.getStringExtra(XmppService.XMPP_RESULT_FROM);
                    ChatState chatState = (ChatState) intent.getSerializableExtra(XmppService.XMPP_RESULT_STATE);
                    onChatStateReceived(from, chatState);
                }
            }
        };

        userSessionComponent = ((SpotlightApplication) getApplication()).getComponentContainer().userSessionComponent();
        if(userSessionComponent!=null) {
            connection = userSessionComponent.getXMPPConnection();
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
        if (this instanceof HomeActivity) {
        } else {
            isBackPressed = true;
        }
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
    public void removeSelectedFragment(BaseFragment backHandledFragment) {
        baseFragmentList.remove(backHandledFragment);
    }

    @Override
    public void setSelectedFragment(BaseFragment backHandledFragment) {
        baseFragmentList.add(backHandledFragment);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void networkOnline() {
        Logger.d("HAS INTERNET CONNECTION");
    }

    @Override
    public void networkOffline() {
        Logger.d("HAS NO INTERNET CONNECTION");
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

    public Scheduler getUiScheduler() {
        return AndroidSchedulers.mainThread();
    }

    private BaseFragment getCurrentFragment() {
        int size = baseFragmentList.size();
        if (size > 0)
            return baseFragmentList.get(size - 1);
        return null;
    }

    public void onMessageReceived(MessageResult messageId){
        Logger.d("MessageId "+messageId);
    }

    public void onChatStateReceived(String from, ChatState chatState) { Logger.d("chatState: "+chatState.name()+", from "+from);}

    protected abstract void injectComponent(ComponentContainer componentContainer);
}
