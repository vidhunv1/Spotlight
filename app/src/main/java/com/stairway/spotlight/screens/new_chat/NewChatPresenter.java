package com.stairway.spotlight.screens.new_chat;

import com.stairway.spotlight.XMPPManager;
import com.stairway.spotlight.api.ApiError;
import com.stairway.spotlight.api.ApiManager;
import com.stairway.spotlight.api.bot.BotApi;
import com.stairway.spotlight.api.bot.BotResponse;
import com.stairway.spotlight.api.user.UserApi;
import com.stairway.spotlight.api.user.UserResponse;
import com.stairway.spotlight.api.user._User;
import com.stairway.spotlight.core.Logger;
import com.stairway.spotlight.db.BotDetailsStore;
import com.stairway.spotlight.db.ContactStore;
import com.stairway.spotlight.models.ContactResult;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;

import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by vidhun on 01/09/16.
 */
public class NewChatPresenter implements NewChatContract.Presenter {
    private NewChatContract.View contactsView;
    private CompositeSubscription compositeSubscription;
    private ContactStore contactStore;
    private BotDetailsStore botDetailsStore;
    private UserApi userApi;
    private BotApi botApi;

    public NewChatPresenter(ContactStore contactStore, UserApi userApi, BotDetailsStore botDetailsStore, BotApi botApi) {
        this.contactStore = contactStore;
        this.userApi = userApi;
        this.botDetailsStore = botDetailsStore;
        this.botApi = botApi;
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
        Subscription subscription = contactStore.getContacts()
                .subscribe(new Subscriber<List<ContactResult>>() {
                    @Override
                    public void onCompleted() {}
                    @Override
                    public void onError(Throwable e) {}

                    @Override
                    public void onNext(List<ContactResult> contactsResults) {
                        List<NewChatItemModel> newChatItemModels = new ArrayList<NewChatItemModel>(contactsResults.size());
                        for(ContactResult contactsResult: contactsResults) {
                            if(contactsResult.isAdded()) {
                                NewChatItemModel newChatItemModel = new NewChatItemModel(
                                        contactsResult.getContactName(),
                                        contactsResult.getUsername(),
                                        contactsResult.getUserId());
                                newChatItemModel.setProfileDP(contactsResult.getProfileDP());
                                newChatItemModels.add(newChatItemModel);
                            }
                        }

                        contactsView.displayContacts(newChatItemModels);
                    }
                });

        compositeSubscription.add(subscription);
    }

    // failed, already in contacts, added succesfully
    @Override
    public void addContact(String userId) {
        Logger.d(this);
        Subscription subscription = contactStore.getContactByUserId(userId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(contact -> {
                    if(contact!=null) {
                        contactsView.showContactAddedSuccess(contact.getContactName(), contact.getUsername(), true);
                    } else {
                        userApi.findUserByUserId(userId)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeOn(Schedulers.io())
                                .map(userResponse -> {
                                    if(!userResponse.isSuccess()) {
                                        if(userResponse.getError().getCode() == 404) {
                                            contactsView.showInvalidIDError();
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
                                        e.printStackTrace();
                                        ApiError error = new ApiError(e);
                                        contactsView.showError(error.getTitle(), error.getMessage());
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
                                                    contactsView.showContactAddedSuccess(contactResult.getContactName(), contactResult.getUsername(), false);
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
                                                                                        contactsView.showContactAddedSuccess(contactResult.getContactName(), contactResult.getUsername(), false);
                                                                                    }

                                                                                    @Override
                                                                                    public void onNext(Boolean aBoolean) {
                                                                                        contactsView.showContactAddedSuccess(contactResult.getContactName(), contactResult.getUsername(), false);
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
        compositeSubscription.add(subscription);
    }
}