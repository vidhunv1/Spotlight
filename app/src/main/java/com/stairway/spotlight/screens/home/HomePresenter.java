package com.stairway.spotlight.screens.home;

import com.stairway.spotlight.MessageController;
import com.stairway.spotlight.core.Logger;
import com.stairway.spotlight.core.UseCaseSubscriber;
import com.stairway.spotlight.db.MessageStore;

import java.util.List;
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

    public HomePresenter(MessageController messageController) {
        this.messageController = messageController;
    }

    @Override
    public void attachView(HomeContract.View view) {
        this.contactsView = view;
    }

    @Override
    public void detachView() {
        contactsView = null;
    }

    @Override
    public void initChatList() {
        Logger.v(this, " initChatList");
        contactsView.displayChatList(messageController.getChatList());
    }
}
