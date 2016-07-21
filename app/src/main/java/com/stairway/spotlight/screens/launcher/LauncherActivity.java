package com.stairway.spotlight.screens.launcher;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.stairway.data.manager.Logger;
import com.stairway.data.source.auth.UserSessionResult;
import com.stairway.spotlight.R;
import com.stairway.spotlight.core.BaseActivity;
import com.stairway.spotlight.core.di.component.ComponentContainer;
import com.stairway.spotlight.screens.home.HomeActivity;
import com.stairway.spotlight.screens.launcher.di.LauncherModule;
import com.stairway.spotlight.screens.register.RegisterActivity;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


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
        launcherPresenter.attachView(this);
        launcherPresenter.getUserSession();
    }

    @Override
    public void navigateToHomeActivity() {
        startActivity(HomeActivity.callingIntent(this));
    }

    @Override
    public void updateSessionDetails(UserSessionResult userSessionResult) {
        // Login details available.
        componentContainer.initUserSession(userSessionResult);
    }

    @Override
    public void navigateToRegisterActivity() {
        // Login details not availble: register user.
        startActivity(RegisterActivity.callingIntent(this));
    }

    @Override
    protected void injectComponent(ComponentContainer componentContainer) {
        this.componentContainer = componentContainer;
        componentContainer.getAppComponent().plus(new LauncherModule()).inject(this);
    }
}
