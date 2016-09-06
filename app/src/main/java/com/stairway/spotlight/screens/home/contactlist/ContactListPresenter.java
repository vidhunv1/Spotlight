package com.stairway.spotlight.screens.home.contactlist;

import com.stairway.data.source.contacts.ContactsContent;
import com.stairway.data.source.contacts.ContactsResult;
import com.stairway.spotlight.core.UseCaseSubscriber;

import java.util.List;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by vidhun on 01/09/16.
 */
public class ContactListPresenter implements ContactListContract.Presenter {
    private ContactListContract.View view;
    private GetContactsUseCase getContactsUseCase;
    private CompositeSubscription compositeSubscription;

    public ContactListPresenter(GetContactsUseCase getContactsUseCase) {
        this.getContactsUseCase = getContactsUseCase;
        this.compositeSubscription = new CompositeSubscription();
    }

    @Override
    public void attachView(ContactListContract.View view) {
        this.view = view;
    }

    @Override
    public void detachView() {
        view = null;
    }

    @Override
    public void initContactList() {
        Subscription subscription = getContactsUseCase.execute()
                .observeOn(view.getUiScheduler())
                .toList()
                .subscribe(new UseCaseSubscriber<List<ContactsResult>>(view) {
                    @Override
                    public void onResult(List<ContactsResult> result) {

                    }
                });

        compositeSubscription.add(subscription);
    }
}
