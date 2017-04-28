package com.chat.ichat.screens.home;

import com.chat.ichat.MessageController;
import com.chat.ichat.api.ApiError;
import com.chat.ichat.api.ApiManager;
import com.chat.ichat.api.app.AppApi;
import com.chat.ichat.api.app.VersionResponse;
import com.chat.ichat.api.bot.BotApi;
import com.chat.ichat.api.bot.BotResponse;
import com.chat.ichat.api.phone_contacts.PhoneContactResponse;
import com.chat.ichat.api.phone_contacts._PhoneContact;
import com.chat.ichat.api.user.UserApi;
import com.chat.ichat.api.user.UserResponse;
import com.chat.ichat.api.user._User;
import com.chat.ichat.core.Logger;
import com.chat.ichat.db.BotDetailsStore;
import com.chat.ichat.db.ContactStore;
import com.chat.ichat.db.MessageStore;
import com.chat.ichat.models.ContactResult;
import com.chat.ichat.screens.new_chat.AddContactUseCase;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by vidhun on 13/07/16.
 */
public class HomePresenter implements HomeContract.Presenter {
    private HomeContract.View contactsView;
    private MessageController messageController;
    private MessageStore messageStore;
    private CompositeSubscription compositeSubscription;
    private AppApi appApi;
    private ContactStore contactStore;
    private AddContactUseCase addContactUseCase;

    public HomePresenter(MessageController messageController, AppApi appApi, MessageStore messageStore, ContactStore contactStore, UserApi userApi, BotDetailsStore botDetailsStore, BotApi botApi) {
        this.messageController = messageController;
        this.appApi = appApi;
        this.messageStore = messageStore;
        this.contactStore = contactStore;
        this.addContactUseCase = new AddContactUseCase(userApi, contactStore, botApi, botDetailsStore);
        compositeSubscription = new CompositeSubscription();
    }

    @Override
    public void attachView(HomeContract.View view) {
        this.contactsView = view;
    }

    @Override
    public void detachView() {
        contactsView = null;
        compositeSubscription.clear();
    }

    @Override
    public void loadChatList() {
        Logger.d(this, " initChatList");
        Subscription subscription = messageController.getChatList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<ChatItem>>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Logger.d(this, "Error initchatlist");
                    }

                    @Override
                    public void onNext(List<ChatItem> chatItems) {
                        contactsView.displayChatList(chatItems);
                    }
                });

        compositeSubscription.add(subscription);
    }

    @Override
    public void init(int currentVersionCode) {
        Subscription subscription = appApi.appVersion()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<VersionResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(VersionResponse versionResponse) {
                        if(versionResponse.getVersionCode() > currentVersionCode) {
                            contactsView.showUpdate(versionResponse.getVersionCode(), versionResponse.getVersionName(), versionResponse.isMandatory());
                        }
                    }
                });
        compositeSubscription.add(subscription);
    }

    @Override
    public void deleteChat(String chatId) {
        Subscription subscription = messageStore.deleteChat(chatId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        contactsView.removeChatItem(chatId);
                    }
                });
        compositeSubscription.add(subscription);
    }

    // failed, already in contacts, added succesfully
    @Override
    public void addContact(String userId) {
        Logger.d(this);
        Subscription subscription = addContactUseCase.execute(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ContactResult>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        ApiError error = new ApiError(e);
                        contactsView.showError(error.getTitle(), error.getMessage());
                    }

                    @Override
                    public void onNext(ContactResult contactResult) {
                        if(contactResult == null) {
                            contactsView.showInvalidIDError();
                        } else {
                            contactsView.showContactAddedSuccess(contactResult.getContactName(),contactResult.getUsername(), false);
                        }
                    }
                });
        compositeSubscription.add(subscription);
    }

    @Override
    public void performSync() {
        // TODO: Change sync to messageService
        Logger.d(this, "SYncing..");
        this.contactStore.getContacts()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<ContactResult>>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(List<ContactResult> contactResults) {
                        Logger.d(this, "Getting: 1"+contactResults);
                        List<Observable<Boolean>> observables = new ArrayList<>();
                        for (ContactResult contactResult : contactResults) {
                            if(contactResult.getUserType() == _User.UserType.official) {
                                Logger.d("Getting: 2"+contactResult);
                                observables.add(getBotDetails(contactResult.getUsername()));
                            } else {
                                observables.add(getUserDetails(contactResult.getUsername()));
                            }
                        }
                        Observable.zip(observables, (i) -> "Done Sync")
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Subscriber<String>() {
                                @Override
                                public void onCompleted() {

                                }

                                @Override
                                public void onError(Throwable e) {
                                    e.printStackTrace();
                                }

                                @Override
                                public void onNext(String s) {
                                    ApiManager.getPhoneContactsApi()
                                            .getContacts()
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(new Subscriber<PhoneContactResponse>() {
                                                @Override
                                                public void onCompleted() {

                                                }

                                                @Override
                                                public void onError(Throwable e) {

                                                }

                                                @Override
                                                public void onNext(PhoneContactResponse phoneContactResponse) {
                                                    List<ContactResult> cr = new ArrayList<ContactResult>();
                                                    for (_PhoneContact phoneContact : phoneContactResponse.getContacts()) {
                                                        ContactResult c = new ContactResult();
                                                        c.setUserType(_User.UserType.regular);
                                                        c.setAdded(true);
                                                        c.setUserId(phoneContact.getUserId());
                                                        c.setUsername(phoneContact.getUsername());
                                                        c.setPhoneNumber(phoneContact.getPhone());
                                                        c.setContactName(phoneContact.getCountryCode());
                                                        c.setProfileDP(phoneContact.getProfileDP());
                                                        c.setContactName(phoneContact.getName());
                                                        c.setDisplayName(phoneContact.getName());
                                                        cr.add(c);
                                                    }
                                                    contactStore.storeContacts(cr)
                                                            .subscribeOn(Schedulers.io())
                                                            .observeOn(AndroidSchedulers.mainThread())
                                                            .subscribe(new Subscriber<Boolean>() {
                                                                @Override
                                                                public void onCompleted() {

                                                                }

                                                                @Override
                                                                public void onError(Throwable e) {

                                                                }

                                                                @Override
                                                                public void onNext(Boolean aBoolean) {
                                                                    contactsView.onSyncSuccess();
                                                                }
                                                            });
                                                }
                                            });
                                }
                            });
                    }

                });
    }

    private Observable<Boolean> getBotDetails(String username) {
        return Observable.create(subscriber -> {
            Logger.d(this, "Syncing for "+username);
            ApiManager.getBotApi().getBotDetails(username)
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
                            Logger.d(this, "BOT DATA:"+data.getData().toString());
                            if(data.isSuccess()) {
                                BotResponse.Data botResponse = data.getData();
                                Logger.d(this, botResponse.toString());
                                BotDetailsStore.getInstance().putMenu(botResponse.getUsername(), botResponse.getPersistentMenus())
                                        .subscribeOn(Schedulers.newThread())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Subscriber<Boolean>() {
                                            @Override
                                            public void onCompleted() {
                                            }

                                            @Override
                                            public void onError(Throwable e) {
                                            }

                                            @Override
                                            public void onNext(Boolean aBoolean) {
                                                subscriber.onNext(aBoolean);
                                            }
                                        });
                            } else {
                                Logger.d(this, "Error response");
                                Logger.d(this, data.getError().toString());
                            }
                        }
                    });
        });
    }

    private Observable<Boolean> getUserDetails(String username) {
        return Observable.create(subscriber -> {
            ApiManager.getUserApi().findUserByUserName(username)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<UserResponse>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onNext(UserResponse userResponse) {
                            if(userResponse.isSuccess()) {
                                _User user = userResponse.getUser();
                                ContactResult c = new ContactResult();
                                c.setUsername(user.getUsername());
                                c.setUserId(user.getUserId());
                                c.setProfileDP(user.getProfileDP());
                                c.setContactName(user.getName());
                                c.setUserType(user.getUserType());
                                c.setAdded(true);
                                contactStore.update(c)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Subscriber<ContactResult>() {
                                            @Override
                                            public void onCompleted() {

                                            }

                                            @Override
                                            public void onError(Throwable e) {

                                            }

                                            @Override
                                            public void onNext(ContactResult contactResult) {
                                                subscriber.onNext(true);
                                            }
                                        });
                            }
                        }
                    });
        });
    }
}
