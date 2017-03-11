package com.stairway.spotlight.screens.new_chat;

import com.stairway.spotlight.XMPPManager;
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
                            NewChatItemModel newChatItemModel = new NewChatItemModel(
                                    contactsResult.getDisplayName(),
                                    contactsResult.getUsername(),
                                    contactsResult.getUserId());

                            newChatItemModels.add(newChatItemModel);
                        }

                        contactsView.displayContacts(newChatItemModels);
                    }
                });

        compositeSubscription.add(subscription);
    }

    // failed, already in contacts, added succesfully
    @Override
    public void addContact(String userId, String accessToken) {
        Logger.d(this);
        Subscription subscription = contactStore.getContactByUserId(userId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(contact -> {
                    if(contact!=null) {
                        contactsView.showContactAddedSuccess(contact.getDisplayName(), contact.getUsername(), true);
                    } else {
                        userApi.findUser(userId)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeOn(Schedulers.io())
                                .map(userResponse -> {
                                    ContactResult contactResult = new ContactResult();
                                    contactResult.setUserId(userResponse.getUser().getUserId());
                                    contactResult.setUsername(userResponse.getUser().getUsername());
                                    contactResult.setDisplayName(userResponse.getUser().getName());
                                    contactResult.setUserType(userResponse.getUser().getUserType());
                                    contactResult.setAdded(true);

                                    return contactResult;
                                })
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeOn(Schedulers.io())
                                .subscribe(new Subscriber<ContactResult>() {
                                    @Override
                                    public void onCompleted() {}

                                    @Override
                                    public void onError(Throwable e) {
                                        e.printStackTrace();
                                        Logger.d("userApi.findUser error");

                                        //TODO: remove not found user from error.
                                        contactsView.showInvalidIDError();
                                    }

                                    @Override
                                    public void onNext(ContactResult contactResult) {
                                        Logger.d(this, contactResult.toString());

                                        Roster roster = Roster.getInstanceFor(XMPPManager.getInstance().getConnection());
                                        if (!roster.isLoaded())
                                            try {
                                                roster.reloadAndWait();
                                            } catch (SmackException.NotLoggedInException e) {
                                                e.printStackTrace();
                                            } catch (SmackException.NotConnectedException e) {
                                                e.printStackTrace();
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        try {
                                            roster.createEntry(XMPPManager.getJidFromUserName(contactResult.getUsername()), contactResult.getContactName(), null);
                                        } catch (SmackException.NotLoggedInException e) {
                                            e.printStackTrace();
                                        } catch (SmackException.NoResponseException e) {
                                            e.printStackTrace();
                                        } catch (XMPPException.XMPPErrorException e) {
                                            e.printStackTrace();
                                        } catch (SmackException.NotConnectedException e) {
                                            e.printStackTrace();
                                        }

                                        contactStore.storeContact(contactResult).subscribe(new Subscriber<Boolean>() {
                                            @Override
                                            public void onCompleted() {}

                                            @Override
                                            public void onError(Throwable e) {}

                                            @Override
                                            public void onNext(Boolean b) {
                                                if(contactResult.getUserType()== _User.UserType.regular) {
                                                    contactsView.showContactAddedSuccess(contactResult.getDisplayName(), contactResult.getUsername(), false);
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
                                                                                        contactsView.showContactAddedSuccess(contactResult.getDisplayName(), contactResult.getUsername(), false);
                                                                                    }

                                                                                    @Override
                                                                                    public void onNext(Boolean aBoolean) {
                                                                                        contactsView.showContactAddedSuccess(contactResult.getDisplayName(), contactResult.getUsername(), false);
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