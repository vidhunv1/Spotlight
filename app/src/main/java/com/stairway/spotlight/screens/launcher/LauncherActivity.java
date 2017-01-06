package com.stairway.spotlight.screens.launcher;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.stairway.data.config.Logger;
import com.stairway.data.source.user.UserSessionResult;
import com.stairway.spotlight.R;
import com.stairway.spotlight.application.SpotlightApplication;
import com.stairway.spotlight.core.di.component.ComponentContainer;
import com.stairway.spotlight.screens.home.HomeActivity;
import com.stairway.spotlight.screens.launcher.di.LauncherModule;
import com.stairway.spotlight.screens.welcome.WelcomeActivity;

import javax.inject.Inject;

import butterknife.ButterKnife;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;


public class LauncherActivity extends AppCompatActivity implements LauncherContract.View{

    @Inject
    LauncherPresenter launcherPresenter;

    ComponentContainer componentContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        componentContainer = ((SpotlightApplication) getApplication()).getComponentContainer();
        componentContainer.getAppComponent().plus(new LauncherModule(getApplicationContext())).inject(this);

        ButterKnife.bind(this);

    }

    @Override
    public Scheduler getUiScheduler() {
        return AndroidSchedulers.mainThread();
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

}
