package com.stairway.spotlight.screens.register.signup;

import com.stairway.data.config.Logger;
import com.stairway.data.source.user.UserApi;
import com.stairway.data.source.user.gson_models.StatusResponse;
import com.stairway.data.source.user.gson_models.User;
import com.stairway.spotlight.core.UseCaseSubscriber;

import javax.inject.Inject;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by vidhun on 16/10/16.
 */

public class SignUpPresenter implements SignUpContract.Presenter {
    private SignUpContract.View signUpView;
    private CompositeSubscription compositeSubscription;

    private UserApi userApi;

    public SignUpPresenter(UserApi userApi) {
        this.userApi = userApi;
        compositeSubscription = new CompositeSubscription();
    }

    @Override
    public void createUser(String countryCode, String phoneNumber) {

        Subscription subscription = userApi.createUser(countryCode, phoneNumber).subscribe(new Subscriber<StatusResponse>() {
            @Override
            public void onCompleted() {}

            @Override
            public void onError(Throwable e) {
                Logger.d(this, " goterror"+e.getMessage());
            }

            @Override
            public void onNext(StatusResponse createResponse) {
                Logger.d(this, " gotresutl"+createResponse);
                signUpView.navigateToVerifyOtp(countryCode, phoneNumber);
            }
        });

        compositeSubscription.add(subscription);
    }

    @Override
    public void attachView(SignUpContract.View view) {
        this.signUpView = view;
    }

    @Override
    public void detachView() {
        compositeSubscription.clear();
        signUpView = null;
    }
}
