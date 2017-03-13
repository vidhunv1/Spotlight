package com.stairway.spotlight.screens.sign_up;

import com.stairway.spotlight.MessageController;
import com.stairway.spotlight.UserSessionManager;
import com.stairway.spotlight.XMPPManager;
import com.stairway.spotlight.api.ApiManager;
import com.stairway.spotlight.api.user.UserApi;
import com.stairway.spotlight.api.user.UserRequest;
import com.stairway.spotlight.api.user.UserResponse;
import com.stairway.spotlight.api.user._User;
import com.stairway.spotlight.application.SpotlightApplication;
import com.stairway.spotlight.db.ContactStore;
import com.stairway.spotlight.db.MessageStore;
import com.stairway.spotlight.models.UserSession;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by vidhun on 08/03/17.
 */

public class SignUpPresenter implements SignUpContract.Presenter {
    private SignUpContract.View signUpView;
    private CompositeSubscription subscriptions;

    private UserApi userApi;
    private UserSessionManager userSessionManager;

    public SignUpPresenter(UserApi userApi, UserSessionManager userSessionManager) {
        this.userApi = userApi;
        this.userSessionManager = userSessionManager;
        this.subscriptions = new CompositeSubscription();
    }

    @Override
    public void registerUser(String fullName, String email, String password, String countryCode, String mobile) {
        UserRequest request = new UserRequest();
        _User user = new _User();
        user.setName(fullName);
        user.setEmail(email);
        user.setPassword(password);
        user.setCountryCode(countryCode);
        user.setPhone(mobile);
        user.setUserType(_User.UserType.regular);
        request.setUser(user);

        Subscription subscription = userApi.createUser(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<UserResponse>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {}

                    @Override
                    public void onNext(UserResponse userResponse) {
                        UserSession userSession = new UserSession(userResponse.getAccessToken(), userResponse.getUser().getUsername(), userResponse.getExpires(), fullName, email, password);
                        userSessionManager.save(userSession);

                        SpotlightApplication.getContext().initSession();

                        signUpView.navigateToSetUserID();
                    }
                });

        subscriptions.add(subscription);
    }

    @Override
    public void attachView(SignUpContract.View view) {
        this.signUpView = view;
    }

    @Override
    public void detachView() {
        subscriptions.clear();
        signUpView = null;
    }
}
