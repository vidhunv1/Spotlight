package com.chat.ichat.screens.launcher;

import com.chat.ichat.UserSessionManager;
/**
 * Created by vidhun on 21/07/16.
 */
public class LauncherPresenter implements LauncherContract.Presenter {
    private LauncherContract.View launcherView;
    private UserSessionManager userSessionManager;

    public LauncherPresenter(UserSessionManager userSessionManager) {
        this.userSessionManager = userSessionManager;
    }

    @Override
    public void getUserSession() {
        if(userSessionManager.hasAccessToken()) {
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
