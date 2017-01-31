package com.stairway.spotlight.screens.home;

import com.stairway.spotlight.core.Logger;
import com.stairway.spotlight.core.UseCaseSubscriber;

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
    private CompositeSubscription compositeSubscription;
    private GetChatsUseCase getChatsUseCase;

    public HomePresenter(GetChatsUseCase getChatsUseCase) {
        this.compositeSubscription = new CompositeSubscription();
        this.getChatsUseCase = getChatsUseCase;
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
        Subscription subscription = getChatsUseCase.execute()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new UseCaseSubscriber<List<ChatListItemModel>>(contactsView) {
                    @Override
                    public void onResult(List<ChatListItemModel> result) {
                        contactsView.displayChatList(result);
                    }
                });

        compositeSubscription.add(subscription);
    }
}
