package com.stairway.spotlight.screens.launcher;

import com.stairway.data.source.user.UserSessionResult;
import com.stairway.spotlight.AccessTokenManager;
import com.stairway.spotlight.core.UseCaseSubscriber;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by vidhun on 21/07/16.
 */
public class LauncherPresenter implements LauncherContract.Presenter {
    private LauncherContract.View launcherView;
    private AccessTokenManager accessTokenManager;

    public LauncherPresenter() {
        this.accessTokenManager = AccessTokenManager.getInstance();
    }

    // Get user session and update component with the session.
    @Override
    public void getUserSession() {
        if(accessTokenManager.hasAccessToken()) {
            launcherView.navigateToHomeActivity();
        } else {
            launcherView.navigateToWelcomeActivity();
        }
    }

    @Override
    public void attachView(LauncherContract.View view) {
        this.launcherView = view;
    }

    @Override
    public void detachView() {
        launcherView = null;
    }
}
