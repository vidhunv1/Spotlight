//package com.stairway.spotlight.screens.register.initialize;
//
//import com.stairway.spotlight.api.contacts.ContactRequest;
//import com.stairway.spotlight.api.contacts.ContactsApi;
//import com.stairway.spotlight.api.contacts._Contact;
//import com.stairway.spotlight.core.Logger;
//import com.stairway.spotlight.db.ContactStore;
//import com.stairway.spotlight.db.ContactsContent;
//import com.stairway.spotlight.models.ContactResult;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import rx.Subscriber;
//import rx.Subscription;
//import rx.android.schedulers.AndroidSchedulers;
//import rx.schedulers.Schedulers;
//import rx.subscriptions.CompositeSubscription;
//
///**
// * Created by vidhun on 09/12/16.
// */
//
//public class InitializePresenter implements InitializeContract.Presenter {
//    private CompositeSubscription subscriptions;
//    private InitializeContract.View initializeView;
//
//    private ContactsApi contactApi;
//    private ContactsContent contactContent;
//    private ContactStore appContactStore;
//
//    public InitializePresenter(ContactsApi contactApi, ContactsContent contactContent, ContactStore appContactStore) {
//        this.contactApi = contactApi;
//        this.contactContent = contactContent;
//        this.appContactStore = appContactStore;
//
//        this.subscriptions = new CompositeSubscription();
//    }
//
//    @Override
//    public void syncContacts() {
//        Subscription subscription = contactContent.getContacts()
//                .subscribe(new Subscriber<List<ContactResult>>() {
//                    @Override
//                    public void onCompleted() {}
//                    @Override
//                    public void onError(Throwable e) {}
//
//                    @Override
//                    public void onNext(List<ContactResult> contactResults) {
//                        contactApi.createContacts(new ContactRequest(contactResults))
//                                .map(contactResponse -> {
//                                    List<ContactResult>  contacts = new ArrayList<>(contactResponse.getContacts().size());
//                                    Logger.d(this,contactResponse.getContacts().size()+"");
//                                    for (_Contact contact : contactResponse.getContacts()) {
//                                        Logger.d(this, contact.toString());
//                                        ContactResult contactResult = new ContactResult(contact.getCountryCode(), contact.getPhone(), contact.getName());
//                                        contactResult.setUsername(contact.getUsername());
//                                        contactResult.setUserId(contact.getUserId());
//
//                                        // default behaviour, we auto add phone contacts
//                                        if(contact.isRegistered())
//                                            contacts.add(contactResult);
//                                    }
//                                    return contacts; })
//                                .subscribeOn(Schedulers.io())
//                                .observeOn(AndroidSchedulers.mainThread())
//                                .subscribe(new Subscriber<List<ContactResult>>() {
//                                    @Override
//                                    public void onCompleted() {}
//                                    @Override
//                                    public void onError(Throwable e) {
//                                        e.printStackTrace();
//                                        Logger.d(this, "error: "+e.getMessage());
//                                    }
//
//                                    @Override
//                                    public void onNext(List<ContactResult> contacts) {
//                                        appContactStore.storeContacts(contacts)
//                                                .subscribe(isSuccessful -> initializeView.navigateToHome());
//                                    }
//                                });
//                    }
//                });
//        subscriptions.add(subscription);
//    }
//
//    @Override
//    public void attachView(InitializeContract.View view) {
//        this.initializeView = view;
//    }
//
//    @Override
//    public void detachView() {
//        this.initializeView = null;
//    }
//}
