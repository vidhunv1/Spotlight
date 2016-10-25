package com.stairway.spotlight.screens.home.contacts;

import com.stairway.data.manager.Logger;
import com.stairway.spotlight.core.UseCaseSubscriber;

import java.util.List;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by vidhun on 01/09/16.
 */
public class ContactListPresenter implements ContactListContract.Presenter {
    private ContactListContract.View contactsView;
    private GetContactsUseCase getContactsUseCase;
    private CompositeSubscription compositeSubscription;

    public ContactListPresenter(GetContactsUseCase getContactsUseCase) {
        this.getContactsUseCase = getContactsUseCase;
        this.compositeSubscription = new CompositeSubscription();
    }

    @Override
    public void attachView(ContactListContract.View view) {
        this.contactsView = view;
    }

    @Override
    public void detachView() {
        contactsView = null;
        compositeSubscription.unsubscribe();
    }

    @Override
    public void initContactList() {
        Logger.d("ContactsPresenter");

        Subscription subscription = getContactsUseCase.execute()
                .observeOn(contactsView.getUiScheduler())
                .subscribe(new UseCaseSubscriber<List<ContactListItemModel>>(contactsView) {
                    @Override
                    public void onResult(List<ContactListItemModel> result) {
                        contactsView.addContacts(result);
                    }
                });

        compositeSubscription.add(subscription);
    }
}
