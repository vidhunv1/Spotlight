package com.stairway.spotlight.screens.register.verifyotp;

import com.stairway.data.error.DataException;
import com.stairway.data.source.user.UserApi;
import com.stairway.data.source.user.gson_models.UserResponse;
import com.stairway.spotlight.models.AccessToken;
import com.stairway.spotlight.AccessTokenManager;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by vidhun on 25/07/16.
 */
public class VerifyOtpPresenter implements VerifyOtpContract.Presenter {

    private VerifyOtpContract.View verifyOtpView;
    private CompositeSubscription subscriptions;

    private AccessTokenManager accessTokenManager;
    private UserApi userApi;

    public VerifyOtpPresenter(UserApi userApi, AccessTokenManager accessTokenManager) {
        this.accessTokenManager = accessTokenManager;
        this.userApi = userApi;
        this.subscriptions = new CompositeSubscription();
    }

    @Override
    public void registerUser(String countryCode, String mobile, String otp) {

        Subscription subscription = userApi.verifyUser(countryCode, mobile, otp)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<UserResponse>() {
                @Override
                public void onCompleted() {}

                @Override
                public void onError(Throwable e) {
                    e.printStackTrace();
                    if(e instanceof DataException)
                        if(((DataException) e).getKind()== DataException.Kind.OTP_INVALID)
                            verifyOtpView.invalidOtpError();
                }

                @Override
                public void onNext(UserResponse verifyResponse) {
                    AccessToken accessToken = new AccessToken(verifyResponse.getAccessToken(), verifyResponse.getUser().getUsername(), verifyResponse.getExpires());
                    accessTokenManager.save(accessToken);
                    verifyOtpView.navigateToInitializeFragment(accessToken);
                }
            });

        subscriptions.add(subscription);
    }

    @Override
    public void attachView(VerifyOtpContract.View view) {
        this.verifyOtpView = view;
    }

    @Override
    public void detachView() {
        subscriptions.clear();
        verifyOtpView = null;
    }
}
