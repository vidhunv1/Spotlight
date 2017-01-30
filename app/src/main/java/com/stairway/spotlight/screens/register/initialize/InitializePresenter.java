package com.stairway.spotlight.screens.register.initialize;

import com.stairway.data.config.Logger;
import com.stairway.data.source.contacts.ContactApi;
import com.stairway.data.source.contacts.ContactContent;
import com.stairway.data.source.contacts.ContactResult;
import com.stairway.data.source.contacts.ContactStore;
import com.stairway.data.source.contacts.gson_models._Contact;
import com.stairway.spotlight.AccessTokenManager;

import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by vidhun on 09/12/16.
 */

public class InitializePresenter implements InitializeContract.Presenter {
    private CompositeSubscription subscriptions;
    private InitializeContract.View initializeView;

    private AccessTokenManager accessTokenManager;
    private ContactApi contactApi;
    private ContactContent contactContent;
    private ContactStore contactStore;

    public InitializePresenter(ContactApi contactApi, ContactContent contactContent, ContactStore contactStore, AccessTokenManager accessTokenManager) {
        this.contactApi = contactApi;
        this.contactContent = contactContent;
        this.contactStore = contactStore;
        this.accessTokenManager = accessTokenManager;

        this.subscriptions = new CompositeSubscription();
    }

    @Override
    public void syncContacts() {
        String accessToken = accessTokenManager.load().getAccessToken();

        Subscription subscription = contactContent.getContacts()
                .subscribe(new Subscriber<List<ContactResult>>() {
                    @Override
                    public void onCompleted() {}
                    @Override
                    public void onError(Throwable e) {}

                    @Override
                    public void onNext(List<ContactResult> contactResults) {
                        contactApi.initPhoneBook(contactResults, accessToken)
                                .map(contactResponse -> {
                                    List<ContactResult>  contacts = new ArrayList<>(contactResponse.getContacts().size());
                                    Logger.d(this,contactResponse.getContacts().size()+"");
                                    for (_Contact contact : contactResponse.getContacts()) {
                                        Logger.d(this, contact.toString());
                                        ContactResult contactResult = new ContactResult(contact.getCountryCode(), contact.getPhone(), contact.getName());
                                        contactResult.setUsername(contact.getUsername());
                                        contactResult.setUserId(contact.getUserId());
                                        contactResult.setRegistered(contact.isRegistered());

                                        // default behaviour, we auto add phone contacts
                                        if(contact.isRegistered())
                                            contactResult.setAdded(true);
                                        contacts.add(contactResult);
                                    }
                                    return contacts; })
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Subscriber<List<ContactResult>>() {
                                    @Override
                                    public void onCompleted() {}
                                    @Override
                                    public void onError(Throwable e) {
                                        e.printStackTrace();
                                        Logger.d(this, "error: "+e.getMessage());
                                    }

                                    @Override
                                    public void onNext(List<ContactResult> contacts) {
                                        contactStore.storeContacts(contacts)
                                                .subscribe(isSuccessful -> initializeView.navigateToHome());
                                    }
                                });
                    }
                });
        subscriptions.add(subscription);
    }

    @Override
    public void attachView(InitializeContract.View view) {
        this.initializeView = view;
    }

    @Override
    public void detachView() {
        this.initializeView = null;
    }
}
