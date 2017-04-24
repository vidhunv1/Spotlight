package com.chat.ichat.screens.new_chat;


import com.chat.ichat.MessageController;
import com.chat.ichat.XMPPManager;
import com.chat.ichat.api.ApiError;
import com.chat.ichat.api.bot.BotApi;
import com.chat.ichat.api.bot.BotResponse;
import com.chat.ichat.api.user.UserApi;
import com.chat.ichat.api.user._User;
import com.chat.ichat.core.Logger;
import com.chat.ichat.db.BotDetailsStore;
import com.chat.ichat.db.ContactStore;
import com.chat.ichat.models.ContactResult;
import com.chat.ichat.models.Message;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.roster.Roster;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by vidhun on 20/04/17.
 */

public class AddContactUseCase {
    private UserApi userApi;
    private ContactStore contactStore;
    private BotApi botApi;
    private BotDetailsStore botDetailsStore;

    public AddContactUseCase(UserApi userApi, ContactStore contactStore, BotApi botApi, BotDetailsStore botDetailsStore) {
        this.userApi = userApi;
        this.contactStore = contactStore;
        this.botApi = botApi;
        this.botDetailsStore = botDetailsStore;
    }

    public Observable<ContactResult> execute(String userId) {
        return Observable.create(subscriber -> {
            Subscription subscription = contactStore.getContactByUserId(userId)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(contact -> {
                        if (contact != null && contact.isAdded()) {
                            subscriber.onNext(contact);
                        } else {
                            userApi.addContact(userId)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribeOn(Schedulers.io())
                                    .map(userResponse -> {
                                        if(!userResponse.isSuccess()) {
                                            if(userResponse.getError().getCode() == 404) {
                                                subscriber.onNext(null);
                                            }
                                            return null;
                                        } else {
                                            ContactResult contactResult = new ContactResult();
                                            contactResult.setUserId(userResponse.getUser().getUserId());
                                            contactResult.setUsername(userResponse.getUser().getUsername());
                                            contactResult.setDisplayName(userResponse.getUser().getName());
                                            contactResult.setUserType(userResponse.getUser().getUserType());
                                            contactResult.setProfileDP(userResponse.getUser().getProfileDP());
                                            contactResult.setAdded(true);
                                            contactResult.setBlocked(false);

                                            return contactResult;
                                        }
                                    })
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribeOn(Schedulers.io())
                                    .subscribe(new Subscriber<ContactResult>() {
                                        @Override
                                        public void onCompleted() {}

                                        @Override
                                        public void onError(Throwable e) {
                                            subscriber.onError(e);
                                        }

                                        @Override
                                        public void onNext(ContactResult contactResult) {
                                            if(contactResult == null)
                                                return;
                                            Logger.d(this, contactResult.toString());

                                            Roster roster = Roster.getInstanceFor(XMPPManager.getInstance().getConnection());
                                            if (!roster.isLoaded())
                                                try {
                                                    roster.reloadAndWait();
                                                } catch (SmackException.NotLoggedInException | SmackException.NotConnectedException | InterruptedException e) {
                                                    e.printStackTrace();
                                                }
                                            try {
                                                roster.createEntry(XMPPManager.getJidFromUserName(contactResult.getUsername()), contactResult.getContactName(), null);
                                            } catch (SmackException.NotLoggedInException | SmackException.NoResponseException | XMPPException.XMPPErrorException | SmackException.NotConnectedException e) {
                                                e.printStackTrace();
                                            }
                                            Logger.d(this, "b4 contactstore");
                                            contactStore.storeContact(contactResult).subscribe(new Subscriber<Boolean>() {
                                                @Override
                                                public void onCompleted() {}

                                                @Override
                                                public void onError(Throwable e) {}

                                                @Override
                                                public void onNext(Boolean b) {
                                                    Logger.d(this, "aftr contactstore");
                                                    if(contactResult.getUserType()== _User.UserType.regular) {
                                                        Logger.d(this, "aftr contactstore success");
                                                        MessageController.getInstance().getLastActivity(contactResult.getUsername());
                                                        subscriber.onNext(contactResult);
                                                    } else if(contactResult.getUserType() == _User.UserType.official){
                                                        botApi.getBotDetails(contactResult.getUsername())
                                                                .subscribeOn(Schedulers.io())
                                                                .observeOn(AndroidSchedulers.mainThread())
                                                                .subscribe(new Subscriber<BotResponse>() {
                                                                    @Override
                                                                    public void onCompleted() {
                                                                        Logger.d(this, "onComplete");
                                                                    }

                                                                    @Override
                                                                    public void onError(Throwable e) {
                                                                        Logger.d(this, "Error: "+e.getMessage());
                                                                        e.printStackTrace();
                                                                    }

                                                                    @Override
                                                                    public void onNext(BotResponse data) {
                                                                        if(data.isSuccess()) {
                                                                            BotResponse.Data botResponse = data.getData();
                                                                            Logger.d(this, botResponse.toString());
                                                                            botDetailsStore.putMenu(botResponse.getUsername(), botResponse.getPersistentMenus())
                                                                                    .subscribeOn(Schedulers.newThread())
                                                                                    .observeOn(AndroidSchedulers.mainThread())
                                                                                    .subscribe(new Subscriber<Boolean>() {
                                                                                        @Override
                                                                                        public void onCompleted() {
                                                                                        }

                                                                                        @Override
                                                                                        public void onError(Throwable e) {
                                                                                            subscriber.onNext(contactResult);
                                                                                        }

                                                                                        @Override
                                                                                        public void onNext(Boolean aBoolean) {
                                                                                            subscriber.onNext(contactResult);
                                                                                        }
                                                                                    });
                                                                        } else {
                                                                            Logger.d(this, "Error response");
                                                                            Logger.d(this, data.getError().toString());
                                                                        }
                                                                    }
                                                                });
                                                    }
                                                }
                                            });
                                        }
                                    });
                        }
                    });
        });
    }
}
