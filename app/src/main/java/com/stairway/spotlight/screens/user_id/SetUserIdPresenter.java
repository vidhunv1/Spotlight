package com.stairway.spotlight.screens.user_id;

import android.os.Handler;

import com.stairway.spotlight.UserSessionManager;
import com.stairway.spotlight.api.user.UserApi;
import com.stairway.spotlight.api.user.UserRequest;
import com.stairway.spotlight.api.user.UserResponse;
import com.stairway.spotlight.api.user._User;
import com.stairway.spotlight.models.UserSession;
import com.stairway.spotlight.screens.home.HomeActivity;

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
    private UserSessionManager userSessionManager;

    public SetUserIdPresenter(UserApi userApi, UserSessionManager userSessionManager) {
        this.userApi = userApi;
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
                    public void onError(Throwable e) {}

                    @Override
                    public void onNext(UserResponse userResponse) {
                        if(!userResponse.isSuccess()) {
                            if(userResponse.getError().getCode() == 409) {
                                setUserIdView.showUserIdNotAvailableError();
                            }
                        } else {
                            UserSession userSession = new UserSession();
                            userSession.setUserId(userResponse.getUser().getUserId());
                            userSessionManager.save(userSession);

                            setUserIdView.navigateToHome();
                        }
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
    }
}
