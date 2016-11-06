package com.stairway.spotlight.core;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.stairway.data.manager.Logger;
import com.stairway.data.manager.XMPPManager;
import com.stairway.data.source.message.MessageApi;
import com.stairway.spotlight.application.SpotlightApplication;
import com.stairway.spotlight.core.di.component.ComponentContainer;
import com.stairway.spotlight.core.di.component.UserSessionComponent;
import com.stairway.spotlight.screens.home.HomeActivity;

import java.util.ArrayList;
import java.util.List;

import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by vidhun on 05/07/16.
 */
public abstract class BaseActivity extends AppCompatActivity implements BaseFragment.BackHandlerInterface {
    private List<BaseFragment> baseFragmentList = new ArrayList<>();
    private XMPPManager connection;
    private UserSessionComponent userSessionComponent;

    public static boolean isAppWentToBg = false;
    public static boolean isWindowFocused = false;
    public static boolean isBackPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        injectComponent(((SpotlightApplication) getApplication()).getComponentContainer());
        userSessionComponent = ((SpotlightApplication) getApplication()).getComponentContainer().userSessionComponent();
        if(userSessionComponent!=null)
            connection = userSessionComponent.getXMPPConnection();
    }

    @Override
    protected void onStart() {
        onApplicationToForeground();

        super.onStart();
    }

    @Override
    protected void onStop() {
        onApplicationToBackground();

        super.onStop();
    }

    public void onApplicationToBackground() {
        if (!isWindowFocused) {
            isAppWentToBg = true;
            connection.setPresenceOffline();
        }
    }

    private void onApplicationToForeground() {
        if (isAppWentToBg) {
            isAppWentToBg = false;
            connection.setPresenceOnline();
        }
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

    public Scheduler getUiScheduler() {
        return AndroidSchedulers.mainThread();
    }

    private BaseFragment getCurrentFragment() {
        int size = baseFragmentList.size();
        if (size > 0)
            return baseFragmentList.get(size - 1);
        return null;
    }

    protected abstract void injectComponent(ComponentContainer componentContainer);
}
