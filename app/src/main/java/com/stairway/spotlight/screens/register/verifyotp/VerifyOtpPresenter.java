package com.stairway.spotlight.screens.register.verifyotp;

import com.stairway.spotlight.api.ApiException;
import com.stairway.spotlight.XMPPManager;
import com.stairway.spotlight.api.ApiManager;
import com.stairway.spotlight.api.user.UserApi;
import com.stairway.spotlight.api.user.UserRequest;
import com.stairway.spotlight.api.user.UserResponse;
import com.stairway.spotlight.api.user._User;
import com.stairway.spotlight.application.SpotlightApplication;
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
        UserRequest userRequest = new UserRequest(countryCode, mobile);
        _User user = new _User(countryCode, mobile);
        user.setUserTypeRegular();
        user.setVerificationCode(otp);
        UserRequest verifyRequest = new UserRequest();
        verifyRequest.setUser(user);
        Subscription subscription = userApi.verifyUser(verifyRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<UserResponse>() {
                @Override
                public void onCompleted() {}

                @Override
                public void onError(Throwable e) {
                    e.printStackTrace();
                    if(e instanceof ApiException)
                        if(((ApiException) e).getKind()== ApiException.Kind.OTP_INVALID)
                            verifyOtpView.invalidOtpError();
                }

                @Override
                public void onNext(UserResponse verifyResponse) {
                    AccessToken accessToken = new AccessToken(verifyResponse.getAccessToken(), verifyResponse.getUser().getUsername(), verifyResponse.getExpires());
                    accessTokenManager.save(accessToken);
                    ApiManager.getInstance().setAuthorization(accessToken.getAccessToken());
                    XMPPManager.init(accessToken.getUserName(), accessToken.getAccessToken());
                    SpotlightApplication.getContext().initSession();
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