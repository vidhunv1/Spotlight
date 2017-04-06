package com.stairway.spotlight.screens.user_id;

import android.os.Handler;

import com.stairway.spotlight.UserSessionManager;
import com.stairway.spotlight.XMPPManager;
import com.stairway.spotlight.api.ApiError;
import com.stairway.spotlight.api.StatusResponse;
import com.stairway.spotlight.api.app.AppApi;
import com.stairway.spotlight.api.contacts.ContactRequest;
import com.stairway.spotlight.api.contacts.ContactsApi;
import com.stairway.spotlight.api.contacts._Contact;
import com.stairway.spotlight.api.user.UserApi;
import com.stairway.spotlight.api.user.UserRequest;
import com.stairway.spotlight.api.user.UserResponse;
import com.stairway.spotlight.api.user._User;
import com.stairway.spotlight.application.SpotlightApplication;
import com.stairway.spotlight.core.Logger;
import com.stairway.spotlight.db.ContactStore;
import com.stairway.spotlight.db.ContactsContent;
import com.stairway.spotlight.models.ContactResult;
import com.stairway.spotlight.models.UserSession;
import com.stairway.spotlight.screens.home.HomeActivity;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by vidhun on 09/03/17.
 */

public class SetUserIdPresenter implements SetUserIdContract.Presenter {
    private SetUserIdContract.View setUserIdView;
    private CompositeSubscription subscriptions;

    private UserApi userApi;
    private AppApi appApi;
    private UserSessionManager userSessionManager;
    private ContactsApi contactApi;
    private ContactsContent contactContent;
    private ContactStore appContactStore;

    public SetUserIdPresenter(UserApi userApi, AppApi appApi, UserSessionManager userSessionManager, ContactsApi contactApi, ContactsContent contactContent, ContactStore appContactStore) {
        this.userApi = userApi;
        this.appApi = appApi;
        this.contactApi = contactApi;
        this.contactContent = contactContent;
        this.appContactStore = appContactStore;
        this.userSessionManager = userSessionManager;
        subscriptions = new CompositeSubscription();
    }

    @Override
    public void setUserId(String userId) {
        UserRequest request = new UserRequest();
        _User user = new _User();
        user.setUserId(userId);
        request.setUser(user);

        Subscription subscription = userApi.updateUser(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<UserResponse>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        ApiError error = new ApiError(e);
                        setUserIdView.showError(error.getTitle(), error.getMessage());
                    }

                    @Override
                    public void onNext(UserResponse userResponse) {
                        if(!userResponse.isSuccess()) {
                            if(userResponse.getError().getCode() == 409) {
                                setUserIdView.showUserIdNotAvailableError();
                            } else {
                                setUserIdView.showError(userResponse.getError().getTitle(), userResponse.getError().getTitle());
                            }
                        } else {
                            UserSession userSession = new UserSession();
                            userSession.setUserId(userResponse.getUser().getUserId());
                            userSession.setUserName(userResponse.getUser().getUsername());
                            userSessionManager.save(userSession);
                            SpotlightApplication.getContext().initSession();

                            appApi.appInit()
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Subscriber<StatusResponse>() {
                                        @Override
                                        public void onCompleted() {
                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            ApiError error = new ApiError(e);
                                            setUserIdView.showError(error.getTitle(), error.getMessage());
                                        }

                                        @Override
                                        public void onNext(StatusResponse statusResponse) {
                                            setUserIdView.onSetUserIdSuccess();
                                        }
                                    });
                        }
                    }
                });
        subscriptions.add(subscription);
    }

    @Override
    public void initialize() {
        Subscription subscription = contactContent.getContacts()
                .subscribe(new Subscriber<List<ContactResult>>() {
                    @Override
                    public void onCompleted() {}
                    @Override
                    public void onError(Throwable e) {}

                    @Override
                    public void onNext(List<ContactResult> contactResults) {
                        contactApi.createContacts(new ContactRequest(contactResults))
                                .map(contactResponse -> {
                                    List<ContactResult>  contacts = new ArrayList<>(contactResponse.getContacts().size());
                                    Logger.d(this,contactResponse.getContacts().size()+"");
                                    for (_Contact contact : contactResponse.getContacts()) {
                                        Logger.d(this, contact.toString());
                                        ContactResult contactResult = new ContactResult(contact.getCountryCode(), contact.getPhone(), contact.getName());
                                        contactResult.setUsername(contact.getUsername());
                                        contactResult.setUserId(contact.getUserId());

                                        // default behaviour, we auto add phone contacts
                                        if(contact.isRegistered())
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
                                        setUserIdView.navigateToHome();
                                    }

                                    @Override
                                    public void onNext(List<ContactResult> contacts) {
                                        appContactStore.storeContacts(contacts)
                                                .subscribe(new Subscriber<Boolean>() {
                                                    @Override
                                                    public void onCompleted() {

                                                    }

                                                    @Override
                                                    public void onError(Throwable e) {
                                                        setUserIdView.navigateToHome();
                                                    }

                                                    @Override
                                                    public void onNext(Boolean aBoolean) {
                                                        setUserIdView.navigateToHome();
                                                    }
                                                });
                                    }
                                });
                    }
                });
        subscriptions.add(subscription);
    }

    @Override
    public void attachView(SetUserIdContract.View view) {
        this.setUserIdView = view;
    }

    @Override
    public void detachView() {
        this.setUserIdView = null;
        subscriptions.clear();
    }
}
