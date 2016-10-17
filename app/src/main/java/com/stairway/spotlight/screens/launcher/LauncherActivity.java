package com.stairway.spotlight.screens.launcher;

import android.os.Bundle;

import com.stairway.data.manager.Logger;
import com.stairway.data.source.user.UserSessionResult;
import com.stairway.spotlight.R;
import com.stairway.spotlight.core.BaseActivity;
import com.stairway.spotlight.core.di.component.ComponentContainer;
import com.stairway.spotlight.screens.home.HomeActivity;
import com.stairway.spotlight.screens.launcher.di.LauncherModule;
import com.stairway.spotlight.screens.welcome.WelcomeActivity;

import javax.inject.Inject;

import butterknife.ButterKnife;


public class LauncherActivity extends BaseActivity implements LauncherContract.View{

    @Inject
    LauncherPresenter launcherPresenter;

    ComponentContainer componentContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        Logger.v("[Launcher Activity]");

        ButterKnife.bind(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        launcherPresenter.attachView(this);
        launcherPresenter.getUserSession();
    }

    @Override
    protected void onPause() {
        super.onPause();
        launcherPresenter.detachView();
    }

    @Override
    public void navigateToHomeActivity() {
        startActivity(HomeActivity.callingIntent(this));
        finish();
    }

    @Override
    public void updateSessionDetails(UserSessionResult userSessionResult) {
        // Login details available.
        componentContainer.initUserSession(userSessionResult);
    }

    @Override
    public void navigateToWelcomeActivity() {
        // Login details not availble: register user.
        startActivity(WelcomeActivity.callingIntent(this));
        finish();
    }

    @Override
    protected void injectComponent(ComponentContainer componentContainer) {
        this.componentContainer = componentContainer;
        componentContainer.getAppComponent().plus(new LauncherModule(getApplicationContext())).inject(this);
    }
}
