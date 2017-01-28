package com.stairway.spotlight.screens.new_chat;

import com.stairway.data.config.Logger;
import com.stairway.data.source.contacts.ContactResult;
import com.stairway.spotlight.core.UseCaseSubscriber;
import com.stairway.spotlight.screens.home.FindUserUseCase;

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
    private FindUserUseCase findUserUseCase;
    private CompositeSubscription compositeSubscription;

    public NewChatPresenter(GetNewChatsUseCase getNewChatsUseCase, FindUserUseCase findUserUseCase) {
        this.findUserUseCase = findUserUseCase;
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