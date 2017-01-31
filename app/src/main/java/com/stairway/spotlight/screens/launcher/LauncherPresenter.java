package com.stairway.spotlight.screens.launcher;

import com.stairway.spotlight.AccessTokenManager;
/**
 * Created by vidhun on 21/07/16.
 */
public class LauncherPresenter implements LauncherContract.Presenter {
    private LauncherContract.View launcherView;
    private AccessTokenManager accessTokenManager;

    public LauncherPresenter(AccessTokenManager accessTokenManager) {
        this.accessTokenManager = accessTokenManager;
    }

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
