package com.stairway.spotlight.screens.home.chats;

import com.stairway.data.config.Logger;
import com.stairway.spotlight.core.UseCaseSubscriber;
import java.util.List;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by vidhun on 13/07/16.
 */
public class ChatListPresenter implements ChatListContract.Presenter {
    private ChatListContract.View contactsView;
    private CompositeSubscription compositeSubscription;
    private GetChatsUseCase getChatsUseCase;

    public ChatListPresenter(GetChatsUseCase getChatsUseCase) {
        this.compositeSubscription = new CompositeSubscription();
        this.getChatsUseCase = getChatsUseCase;
    }

    @Override
    public void attachView(ChatListContract.View view) {
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
                .observeOn(contactsView.getUiScheduler())
                .subscribe(new UseCaseSubscriber<List<ChatListItemModel>>(contactsView) {
                    @Override
                    public void onResult(List<ChatListItemModel> result) {
                        contactsView.displayChatList(result);
                    }
                });

        compositeSubscription.add(subscription);
    }
}
