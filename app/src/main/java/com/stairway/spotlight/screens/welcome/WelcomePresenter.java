package com.stairway.spotlight.screens.welcome;

/**
 * Created by vidhun on 22/07/16.
 */
public class WelcomePresenter implements WelcomeContract.Presenter{
    private WelcomeContract.View view;

    public WelcomePresenter() {
    }

    @Override
    public void attachView(WelcomeContract.View view) {
        this.view = view;
    }

    @Override
    public void detachView() {
        view = null;
    }
}
