package com.chat.ichat.screens.sign_up;

import android.content.SharedPreferences;

import com.chat.ichat.UserSessionManager;
import com.chat.ichat.api.ApiError;
import com.chat.ichat.api.ApiManager;
import com.chat.ichat.api.StatusResponse;
import com.chat.ichat.api.user.UserApi;
import com.chat.ichat.api.user.UserRequest;
import com.chat.ichat.api.user.UserResponse;
import com.chat.ichat.api.user.VerifyRequest;
import com.chat.ichat.api.user._User;
import com.chat.ichat.application.SpotlightApplication;
import com.chat.ichat.db.core.DatabaseManager;
import com.chat.ichat.models.UserSession;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static com.chat.ichat.core.FCMRegistrationIntentService.FCM_TOKEN;
/**
 * Created by vidhun on 08/03/17.
 */

public class SignUpPresenter implements SignUpContract.Presenter {
    private SignUpContract.View signUpView;
    private CompositeSubscription subscriptions;

    private UserApi userApi;
    private UserSessionManager userSessionManager;
    private SharedPreferences defaultSP;

    public SignUpPresenter(UserApi userApi, UserSessionManager userSessionManager, SharedPreferences defaultSP) {
        this.userApi = userApi;
        this.userSessionManager = userSessionManager;
        this.defaultSP = defaultSP;
        this.subscriptions = new CompositeSubscription();
    }

    @Override
    public void registerUser(String fullName, String email, String password, String countryCode, String mobile, String imei) {
        UserRequest request = new UserRequest();
        _User user = new _User();
        user.setName(fullName);
        user.setEmail(email);
        user.setPassword(password);
        user.setCountryCode(countryCode);
        user.setPhone(mobile);
        user.setUserType(_User.UserType.regular);
        user.setIMEI(imei);
        user.setNotificationToken(defaultSP.getString(FCM_TOKEN, ""));
        request.setUser(user);

        Subscription subscription = userApi.createUser(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<UserResponse>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        ApiError error = new ApiError(e);
                        signUpView.showError(error.getTitle(), error.getMessage());
                    }

                    @Override
                    public void onNext(UserResponse userResponse) {
                        if(!userResponse.isSuccess()) {
                            signUpView.showError(userResponse.getError().getTitle(), userResponse.getError().getMessage());
                        } else {
                            UserSession userSession = new UserSession(userResponse.getAccessToken(), userResponse.getExpires(), password);
                            userSession.setName(userResponse.getUser().getName());
                            userSession.setEmail(userResponse.getUser().getEmail());
                            userSessionManager.save(userSession);
                            ApiManager.getInstance().setAuthorization(userSession.getAccessToken());

                            DatabaseManager.getSQLiteHelper().clearData(DatabaseManager.getInstance().openConnection());
                            if(userResponse.isOtpSent()) {
                                signUpView.showVerifyingOtp(userResponse.getVerificationUuid());
                            } else {
                                signUpView.navigateToSetUserID();
                            }
                        }
                    }
                });
        subscriptions.add(subscription);
    }

    @Override
    public void verifyOTP(String countryCode, String phone, String verificationCode, String verificationUUID) {
        VerifyRequest verifyRequest = new VerifyRequest(countryCode, phone, verificationCode, verificationUUID);
        Subscription subscription = userApi.verifyUser(verifyRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<StatusResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(StatusResponse statusResponse) {
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