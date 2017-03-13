package com.stairway.spotlight.screens.login;

import com.stairway.spotlight.UserSessionManager;
import com.stairway.spotlight.api.ApiError;
import com.stairway.spotlight.api.user.UserApi;
import com.stairway.spotlight.api.user.UserRequest;
import com.stairway.spotlight.api.user.UserResponse;
import com.stairway.spotlight.api.user._User;
import com.stairway.spotlight.application.SpotlightApplication;
import com.stairway.spotlight.core.Logger;
import com.stairway.spotlight.db.core.DatabaseManager;
import com.stairway.spotlight.models.UserSession;

import java.io.IOException;

import retrofit2.adapter.rxjava.HttpException;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by vidhun on 12/03/17.
 */

public class LoginPresenter implements LoginContract.Presenter {
    private LoginContract.View loginView;
    private CompositeSubscription subscriptions;

    private UserApi userApi;
    private UserSessionManager userSessionManager;

    public LoginPresenter(UserApi userApi, UserSessionManager userSessionManager) {
        this.userApi = userApi;
        this.userSessionManager = userSessionManager;
        subscriptions = new CompositeSubscription();
    }

    @Override
    public void loginUser(String account, String password) {
        UserRequest request = new UserRequest();
        if(android.util.Patterns.EMAIL_ADDRESS.matcher(account).matches()) { //is email
            _User user = new _User();
            user.setEmail(account);
            user.setPassword(password);
            request.setUser(user);
        } else { // is userId
            _User user = new _User();
            user.setUserId(account);
            user.setPassword(password);
            request.setUser(user);
        }

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
                            loginView.showError(userResponse.getError().getTitle(), userResponse.getError().getMessage());
                        } else {
                            UserSession us = userSessionManager.load();
                            if(us!=null && !us.getUserId().equals(userResponse.getUser().getUserId())) {
                                DatabaseManager.getSQLiteHelper().clearData(DatabaseManager.getInstance().openConnection());
                            }
                            UserSession userSession = new UserSession(userResponse.getAccessToken(), userResponse.getUser().getUsername(), userResponse.getExpires(), userResponse.getUser().getName(), userResponse.getUser().getEmail(), password);
                            userSession.setUserId(userResponse.getUser().getUserId());
                            userSessionManager.save(userSession);

                            SpotlightApplication.getContext().initSession();

                            if (userResponse.getUser().getUserId() == null) {
                                loginView.navigateToSetUserId();
                            } else {
                                loginView.navigateToHome();
                            }
                        }
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
