package com.stairway.spotlight.screens.new_chat;

import com.stairway.spotlight.core.UseCaseSubscriber;

import java.util.List;

import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by vidhun on 01/09/16.
 */
public class NewChatPresenter implements NewChatContract.Presenter {
    private NewChatContract.View contactsView;
    private GetNewChatsUseCase getNewChatsUseCase;
    private CompositeSubscription compositeSubscription;

    public NewChatPresenter(GetNewChatsUseCase getNewChatsUseCase) {
        this.getNewChatsUseCase = getNewChatsUseCase;
        this.compositeSubscription = new CompositeSubscription();
    }

    @Override
    public void attachView(NewChatContract.View view) {
        this.contactsView = view;
    }

    @Override
    public void detachView() {
        contactsView = null;
        compositeSubscription.unsubscribe();
    }

    @Override
    public void initContactList() {
        Subscription subscription = getNewChatsUseCase.execute()
                .subscribeOn(Schedulers.io())
                .observeOn(contactsView.getUiScheduler())
                .subscribe(new UseCaseSubscriber<List<NewChatItemModel>>(contactsView) {
                    @Override
                    public void onResult(List<NewChatItemModel> result) {
                        contactsView.displayContacts(result);
                    }
                });

        compositeSubscription.add(subscription);
    }
}
