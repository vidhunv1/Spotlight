package com.stairway.spotlight.screens.contacts;

import com.stairway.spotlight.core.UseCaseSubscriber;

import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by vidhun on 13/12/16.
 */

public class ContactsPresenter implements ContactsContract.Presenter {
    private CompositeSubscription compositeSubscription;
    private AddContactUseCase addContactUseCase;
    private GetContactsUseCase getContactsUseCase;
    private ContactsContract.View contactsView;

    public ContactsPresenter(AddContactUseCase addContactUseCase, GetContactsUseCase getContactsUseCase) {
        this.addContactUseCase = addContactUseCase;
        this.getContactsUseCase = getContactsUseCase;
        this.compositeSubscription = new CompositeSubscription();
    }

    @Override
    public void addContact(String userName) {
        Subscription subscription = addContactUseCase.execute(userName)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new UseCaseSubscriber<Boolean>(contactsView) {
                    @Override
                    public void onResult(Boolean result) {
                        contactsView.contactAdded(userName);
                    }
                });

        compositeSubscription.add(subscription);
    }

    @Override
    public void loadContacts() {
        Subscription subscription = getContactsUseCase.execute()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new UseCaseSubscriber<List<ContactItemModel>>(contactsView) {
                    @Override
                    public void onResult(List<ContactItemModel> result) {
                        contactsView.showContacts(result);
                    }
                });

        compositeSubscription.add(subscription);
    }

    @Override
    public void attachView(ContactsContract.View view) {
        this.contactsView = view;
    }

    @Override
    public void detachView() {
        contactsView = null;
        compositeSubscription.unsubscribe();
    }
}
