//package com.chat.ichat.screens.new_chat;
//
//import android.view.View;
//
//import com.chat.ichat.MessageController;
//import com.chat.ichat.XMPPManager;
//import com.chat.ichat.api.ApiError;
//import com.chat.ichat.api.bot.BotApi;
//import com.chat.ichat.api.bot.BotResponse;
//import com.chat.ichat.api.user.UserApi;
//import com.chat.ichat.api.user._User;
//import com.chat.ichat.core.Logger;
//import com.chat.ichat.db.BotDetailsStore;
//import com.chat.ichat.db.ContactStore;
//import com.chat.ichat.db.ContactsContent;
//import com.chat.ichat.models.ContactResult;
//
//import org.jivesoftware.smack.SmackException;
//import org.jivesoftware.smack.XMPPException;
//import org.jivesoftware.smack.roster.Roster;
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
// * Created by vidhun on 01/09/16.
// */
//public class NewChatPresenter implements NewChatContract.Presenter {
//    private NewChatContract.View contactsView;
//    private CompositeSubscription compositeSubscription;
//    private ContactStore contactStore;
//    private ContactsContent contactsContent;
//    private AddContactUseCase addContactUseCase;
//
//    NewChatPresenter(ContactStore contactStore, ContactsContent contactsContent, UserApi userApi, BotDetailsStore botDetailsStore, BotApi botApi) {
//        this.contactStore = contactStore;
//        this.contactsContent = contactsContent;
//        this.compositeSubscription = new CompositeSubscription();
//        this.addContactUseCase = new AddContactUseCase(userApi, contactStore, botApi, botDetailsStore);
//    }
//
//    @Override
//    public void attachView(NewChatContract.View view) {
//        this.contactsView = view;
//    }
//
//    @Override
//    public void detachView() {
//        contactsView = null;
//        compositeSubscription.unsubscribe();
//    }
//
//    @Override
//    public void initContactList(boolean shouldShowPhoneBook) {
//        Subscription subscription = contactStore.getContacts()
//                .subscribe(new Subscriber<List<ContactResult>>() {
//                    @Override
//                    public void onCompleted() {}
//                    @Override
//                    public void onError(Throwable e) {}
//
//                    @Override
//                    public void onNext(List<ContactResult> contactsResults) {
//                        List<NewChatItemModel> newChatItemModels = new ArrayList<NewChatItemModel>(contactsResults.size());
//                        for(ContactResult contactsResult: contactsResults) {
//                            if(contactsResult.isAdded()) {
//                                NewChatItemModel newChatItemModel = new NewChatItemModel(
//                                        contactsResult.getContactName(),
//                                        contactsResult.getUsername(),
//                                        contactsResult.getUserId());
//                                newChatItemModel.setProfileDP(contactsResult.getProfileDP());
//                                newChatItemModel.setRegistered(true);
//                                newChatItemModels.add(newChatItemModel);
//
//                                MessageController messageController = MessageController.getInstance();
//                                messageController.getLastActivity(contactsResult.getUsername())
//                                        .observeOn(AndroidSchedulers.mainThread())
//                                        .subscribeOn(Schedulers.io())
//                                        .subscribe(new Subscriber<String>() {
//                                            @Override
//                                            public void onCompleted() {}
//                                            @Override
//                                            public void onError(Throwable e) {}
//                                            @Override
//                                            public void onNext(String time) {}
//                                        });
//                            }
//                        }
//
//                        if(shouldShowPhoneBook) {
//                            contactsContent.getContacts()
//                                    .subscribeOn(Schedulers.newThread())
//                                    .observeOn(AndroidSchedulers.mainThread())
//                                    .subscribe(new Subscriber<List<ContactResult>>() {
//                                        @Override
//                                        public void onCompleted() {
//                                        }
//
//                                        @Override
//                                        public void onError(Throwable e) {
//                                            contactsView.displayContacts(newChatItemModels);
//                                            e.printStackTrace();
//                                        }
//
//                                        @Override
//                                        public void onNext(List<ContactResult> cr) {
//                                            for (ContactResult contactsResult : cr) {
//                                                NewChatItemModel newChatItemModel = new NewChatItemModel(
//                                                        contactsResult.getContactName(),
//                                                        contactsResult.getUsername(),
//                                                        contactsResult.getUserId());
//                                                newChatItemModel.setRegistered(false);
//                                                newChatItemModels.add(newChatItemModel);
//                                            }
//                                            Logger.d(this, "Phone Contacts: " + cr.size());
//                                            contactsView.displayContacts(newChatItemModels);
//                                        }
//                                    });
//                        } else {
//                            contactsView.displayContacts(newChatItemModels);
//                        }
//                    }
//                });
//        compositeSubscription.add(subscription);
//    }
//
//    // failed, already in contacts, added succesfully
//    @Override
//    public void addContact(String userId) {
//        Logger.d(this);
//        Subscription subscription = addContactUseCase.execute(userId, true)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Subscriber<ContactResult>() {
//                    @Override
//                    public void onCompleted() {}
//                    @Override
//                    public void onError(Throwable e) {
//                        e.printStackTrace();
//                        ApiError error = new ApiError(e);
//                        contactsView.showError(error.getTitle(), error.getMessage());
//                    }
//
//                    @Override
//                    public void onNext(ContactResult contactResult) {
//                        if(contactResult == null) {
//                            contactsView.showInvalidIDError();
//                        } else {
//                            contactsView.showContactAddedSuccess(contactResult.getContactName(),contactResult.getUsername(), false);
//                        }
//                    }
//                });
//        compositeSubscription.add(subscription);
//    }
//}