package com.stairway.spotlight.screens.launcher;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import com.stairway.data.config.Logger;
import com.stairway.data.source.user.UserSessionResult;
import com.stairway.spotlight.AccessToken;
import com.stairway.spotlight.R;
import com.stairway.spotlight.application.SpotlightApplication;
import com.stairway.spotlight.core.di.component.ComponentContainer;
import com.stairway.spotlight.screens.home.HomeActivity;
import com.stairway.spotlight.screens.launcher.di.LauncherModule;
import com.stairway.spotlight.screens.welcome.WelcomeActivity;

import javax.inject.Inject;

import butterknife.ButterKnife;
import de.measite.minidns.record.A;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;


public class LauncherActivity extends AppCompatActivity implements LauncherContract.View{

    @Inject
    LauncherPresenter launcherPresenter;

    ComponentContainer componentContainer;

    public static Intent callingIntent(Context context) {
        Intent intent = new Intent(context, LauncherActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        componentContainer = ((SpotlightApplication) getApplication()).getComponentContainer();
        componentContainer.getAppComponent().plus(new LauncherModule(getApplicationContext())).inject(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.statusBar));
        }

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
    public void updateSessionDetails(AccessToken accessToken) {
        // Login details available.
        componentContainer.initUserSession(accessToken);
    }

    @Override
    public void navigateToWelcomeActivity() {
        Logger.d(this, "Navigate to welcome.");
        startActivity(WelcomeActivity.callingIntent(this));
        finish();
    }

}
