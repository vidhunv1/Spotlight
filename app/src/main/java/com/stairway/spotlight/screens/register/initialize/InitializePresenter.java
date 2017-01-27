package com.stairway.spotlight.screens.register.initialize;

import com.stairway.spotlight.core.UseCaseSubscriber;

import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by vidhun on 09/12/16.
 */

public class InitializePresenter implements InitializeContract.Presenter {
    private SyncContactsUseCase syncContactsUseCase;
    private CompositeSubscription compositeSubscription;
    private InitializeContract.View initializeView;

    public InitializePresenter(SyncContactsUseCase syncContactsUseCase) {
        this.syncContactsUseCase = syncContactsUseCase;
        this.compositeSubscription = new CompositeSubscription();
    }

    @Override
    public void syncContacts(String authToken) {
        Subscription subscription = syncContactsUseCase.execute(authToken)
                .subscribeOn(Schedulers.io())
                .observeOn(initializeView.getUiScheduler())
                .subscribe(new UseCaseSubscriber<Boolean>(initializeView) {
                    @Override
                    public void onResult(Boolean result) {
                        initializeView.navigateToHome();
                    }
                });
        compositeSubscription.add(subscription);
    }

    @Override
    public void attachView(InitializeContract.View view) {
        this.initializeView = view;
    }

    @Override
    public void detachView() {
        compositeSubscription.clear();
        this.initializeView = null;
    }
}
