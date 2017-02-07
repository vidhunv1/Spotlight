package com.stairway.spotlight.screens.home;

import com.stairway.spotlight.MessageController;
import com.stairway.spotlight.core.Logger;
import com.stairway.spotlight.core.UseCaseSubscriber;
import com.stairway.spotlight.db.MessageStore;

import java.util.List;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by vidhun on 13/07/16.
 */
public class HomePresenter implements HomeContract.Presenter {
    private HomeContract.View contactsView;
    private MessageController messageController;
    private CompositeSubscription compositeSubscription;

    public HomePresenter(MessageController messageController) {
        this.messageController = messageController;
        compositeSubscription = new CompositeSubscription();
    }

    @Override
    public void attachView(HomeContract.View view) {
        this.contactsView = view;
    }

    @Override
    public void detachView() {
        contactsView = null;
        compositeSubscription.clear();
    }

    @Override
    public void loadChatList() {
        Logger.d(this, " initChatList");
        Subscription subscription = messageController.getChatList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<ChatItem>>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Logger.d(this, "Error initchatlist");
                    }

                    @Override
                    public void onNext(List<ChatItem> chatItems) {
                        contactsView.displayChatList(chatItems);
                    }
                });

        compositeSubscription.add(subscription);
    }
}
