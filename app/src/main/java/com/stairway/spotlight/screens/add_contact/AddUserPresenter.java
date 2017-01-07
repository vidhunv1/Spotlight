package com.stairway.spotlight.screens.add_contact;

import com.stairway.data.source.contacts.ContactResult;
import com.stairway.spotlight.core.UseCaseSubscriber;

import rx.subscriptions.CompositeSubscription;

/**
 * Created by vidhun on 07/01/17.
 */

public class AddUserPresenter implements AddUserContract.Presenter{
    private CompositeSubscription compositeSubscription;
    private AddUserContract.View addContactView;
    private AddUserUseCase addUserUseCase;

    public AddUserPresenter(AddUserUseCase addUserUseCase) {
        this.addUserUseCase = addUserUseCase;
        this.compositeSubscription = new CompositeSubscription();
    }

    @Override
    public void addContact(ContactResult contactResult) {
        addUserUseCase.execute(contactResult).subscribe(new UseCaseSubscriber<ContactResult>(addContactView) {
            @Override
            public void onResult(ContactResult result) {
                addContactView.navigateToMessage(result.getUsername());
            }
        });
    }

    @Override
    public void attachView(AddUserContract.View view) {
        this.addContactView = view;
    }

    @Override
    public void detachView() {
        compositeSubscription.clear();
        this.addContactView = null;
    }
}
