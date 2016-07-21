package com.stairway.spotlight.screens.launcher;

import com.stairway.data.source.auth.UserSessionResult;
import com.stairway.spotlight.core.UseCaseSubscriber;
import com.stairway.spotlight.core.di.component.ComponentContainer;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by vidhun on 21/07/16.
 */
public class LauncherPresenter implements LauncherContract.Presenter {
    private LauncherContract.View launcherView;

    private CompositeSubscription compositeSubscription;

    private UserSessionUseCase userSessionUseCase;

    public LauncherPresenter(UserSessionUseCase userSessionUseCase) {
        this.userSessionUseCase = userSessionUseCase;
        this.compositeSubscription = new CompositeSubscription();
    }

    // Get user session and update component with the session.
    @Override
    public void getUserSession() {
        Subscription subscription = userSessionUseCase.execute()
                .observeOn(launcherView.getUiScheduler())
                .subscribe(new UseCaseSubscriber<UserSessionResult>(launcherView) {
                    @Override
                    public void onResult(UserSessionResult result) {
                        launcherView.updateSessionDetails(result);
                        launcherView.navigateToHomeActivity();
                    }

                    @Override
                    public void onSessionNotFound() {
                        launcherView.navigateToRegisterActivity();
                    }
                });

        compositeSubscription.add(subscription);
    }

    @Override
    public void attachView(LauncherContract.View view) {
        this.launcherView = view;
    }

    @Override
    public void detachView() {
        compositeSubscription.clear();
        launcherView = null;
    }
}
