package com.stairway.spotlight.screens.home;

import com.stairway.data.config.Logger;
import com.stairway.data.source.contacts.ContactResult;
import com.stairway.spotlight.core.UseCaseSubscriber;

import java.util.List;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by vidhun on 13/07/16.
 */
public class HomePresenter implements HomeContract.Presenter {
    private HomeContract.View contactsView;
    private CompositeSubscription compositeSubscription;
    private GetChatsUseCase getChatsUseCase;
    private FindUserUseCase findUserUseCase;

    public HomePresenter(GetChatsUseCase getChatsUseCase, FindUserUseCase findUserUseCase) {
        this.compositeSubscription = new CompositeSubscription();
        this.getChatsUseCase = getChatsUseCase;
        this.findUserUseCase = findUserUseCase;
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
                .observeOn(contactsView.getUiScheduler())
                .subscribe(new UseCaseSubscriber<List<ChatListItemModel>>(contactsView) {
                    @Override
                    public void onResult(List<ChatListItemModel> result) {
                        contactsView.displayChatList(result);
                    }
                });

        compositeSubscription.add(subscription);
    }

    @Override
    public void addContact(String userId, String accessToken) {
        Subscription subscription = findUserUseCase.executeLocal(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(contactsView.getUiScheduler())
                .subscribe(new UseCaseSubscriber<ContactResult>(contactsView) {
                    @Override
                    public void onResult(ContactResult result) {
                        if(result!=null)
                            contactsView.showContactAddedSuccess(result.getDisplayName(), result.getUsername(), true);
                        else {
                            findUserUseCase.execute(userId, accessToken)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(contactsView.getUiScheduler())
                                    .subscribe(new UseCaseSubscriber<ContactResult>(contactsView) {
                                        @Override
                                        public void onResult(ContactResult result) {
                                            contactsView.showContactAddedSuccess(result.getDisplayName(), result.getUsername(), false);
                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            //TODO: display error in view
                                            contactsView.showInvalidIDError();
                                            Logger.d(this, "No contact found with id: "+userId);
                                            Logger.d(this,e.getMessage());
                                        }
                                    });
                        }
                    }
                });
        compositeSubscription.add(subscription);
    }
}
