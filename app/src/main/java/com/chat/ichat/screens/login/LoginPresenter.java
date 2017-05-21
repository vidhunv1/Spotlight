package com.chat.ichat.screens.login;

import android.content.SharedPreferences;

import com.chat.ichat.UserSessionManager;
import com.chat.ichat.XMPPManager;
import com.chat.ichat.api.ApiError;
import com.chat.ichat.api.ApiManager;
import com.chat.ichat.api.contact.ContactResponse;
import com.chat.ichat.api.user.UserApi;
import com.chat.ichat.api.user.UserRequest;
import com.chat.ichat.api.user.UserResponse;
import com.chat.ichat.api.user._User;
import com.chat.ichat.application.SpotlightApplication;
import com.chat.ichat.core.Logger;
import com.chat.ichat.db.ContactStore;
import com.chat.ichat.db.core.DatabaseManager;
import com.chat.ichat.models.ContactResult;
import com.chat.ichat.models.UserSession;

import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static com.chat.ichat.core.FCMRegistrationIntentService.FCM_TOKEN;

/**
 * Created by vidhun on 12/03/17.
 */

public class LoginPresenter implements LoginContract.Presenter {
    private LoginContract.View loginView;
    private CompositeSubscription subscriptions;

    private UserApi userApi;
    private ContactStore contactStore;
    private UserSessionManager userSessionManager;
    private SharedPreferences defaultSP;

    public LoginPresenter(UserApi userApi, UserSessionManager userSessionManager, SharedPreferences defaultSP, ContactStore contactStore) {
        this.userApi = userApi;
        this.contactStore = contactStore;
        this.userSessionManager = userSessionManager;
        this.defaultSP = defaultSP;
        subscriptions = new CompositeSubscription();
    }

    @Override
    public void loginUser(String account, String password) {
        UserRequest request = new UserRequest();
        _User user = new _User();
        if(android.util.Patterns.EMAIL_ADDRESS.matcher(account).matches()) { //is email
            user.setEmail(account);
            user.setPassword(password);
        } else { // is userId
            user.setUserId(account);
            user.setPassword(password);
        }
        user.setNotificationToken(defaultSP.getString(FCM_TOKEN, ""));
        request.setUser(user);

        Subscription subscription = userApi.loginUser(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<UserResponse>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        ApiError error = new ApiError(e);
                        loginView.showError(error.getTitle(), error.getMessage());
                    }

                    @Override
                    public void onNext(UserResponse userResponse) {
                        if (!userResponse.isSuccess()) {
                            Logger.d(this, "Error Code"+userResponse.getError().getCode());
                            loginView.showError(userResponse.getError().getTitle(), userResponse.getError().getMessage());
                        } else {
                            UserSession us = userSessionManager.load();
                            if(us!=null && !us.getUserId().equals(userResponse.getUser().getUserId())) {
                                DatabaseManager.getSQLiteHelper().clearData(DatabaseManager.getInstance().openConnection());
                            }
                            UserSession userSession = new UserSession(userResponse.getAccessToken(), userResponse.getUser().getUsername(), userResponse.getExpires(), userResponse.getUser().getName(), userResponse.getUser().getEmail(), password);
                            userSession.setUserId(userResponse.getUser().getUserId());
                            userSession.setProfilePicPath(userResponse.getUser().getProfileDP());
                            userSessionManager.save(userSession);
                            ApiManager.reset();
                            XMPPManager.reset();

                            SpotlightApplication.getContext().initSession();

                            if (userResponse.getUser().getUserId() == null) {
                                loginView.navigateToSetUserId();
                            } else {
                                loginView.setInitializing();
                            }
                        }
                    }
                });
        subscriptions.add(subscription);
    }

    @Override
    public void fetchContacts() {
        Subscription subscription = ApiManager.getContactApi().getContacts()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ContactResponse>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        loginView.navigateToHome();
                    }

                    @Override
                    public void onNext(ContactResponse contactResponse) {
                        Logger.d(this, "CONTATCTS");
                        List<ContactResult> contactResults = new ArrayList<>();
                        for (_User user : contactResponse.getContacts()) {
                            Logger.d(this, user.toString());
                            ContactResult c = new ContactResult();
                            c.setUsername(user.getUsername());
                            c.setUserId(user.getUserId());
                            c.setProfileDP(user.getProfileDP());
                            c.setContactName(user.getName());
                            c.setUserType(user.getUserType());
                            c.setAdded(true);

                            contactResults.add(c);
                        }
                        contactStore.storeContacts(contactResults)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Subscriber<Boolean>() {
                                    @Override
                                    public void onCompleted() {}

                                    @Override
                                    public void onError(Throwable e) {
                                        loginView.navigateToHome();
                                    }

                                    @Override
                                    public void onNext(Boolean aBoolean) {
                                        loginView.navigateToHome();
                                    }
                                });
                    }
                });
        subscriptions.add(subscription);
    }

    @Override
    public void attachView(LoginContract.View view) {
        this.loginView = view;
    }

    @Override
    public void detachView() {
        subscriptions.clear();
        loginView = null;
    }
}
