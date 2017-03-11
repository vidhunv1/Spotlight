//package com.stairway.spotlight.screens.register.signup;
//
//import com.stairway.spotlight.api.user.StatusResponse;
//import com.stairway.spotlight.api.user.UserApi;
//import com.stairway.spotlight.api.user.UserRequest;
//import com.stairway.spotlight.core.Logger;
//
//import rx.Subscriber;
//import rx.Subscription;
//import rx.subscriptions.CompositeSubscription;
//
///**
// * Created by vidhun on 16/10/16.
// */
//
//public class SignUpPresenter implements SignUpContract.Presenter {
//    private SignUpContract.View signUpView;
//    private CompositeSubscription compositeSubscription;
//
//    private UserApi userApi;
//
//    public SignUpPresenter(UserApi userApi) {
//        this.userApi = userApi;
//        compositeSubscription = new CompositeSubscription();
//    }
//
//    @Override
//    public void createUser(String countryCode, String phoneNumber) {
//
//        Subscription subscription = userApi.createUser(new UserRequest(countryCode, phoneNumber)).subscribe(new Subscriber<StatusResponse>() {
//            @Override
//            public void onCompleted() {}
//
//            @Override
//            public void onError(Throwable e) {
//                Logger.d(this, " goterror"+e.getMessage());
//            }
//
//            @Override
//            public void onNext(StatusResponse createResponse) {
//                Logger.d(this, " gotresutl"+createResponse);
//                signUpView.navigateToVerifyOtp(countryCode, phoneNumber);
//            }
//        });
//
//        compositeSubscription.add(subscription);
//    }
//
//    @Override
//    public void attachView(SignUpContract.View view) {
//        this.signUpView = view;
//    }
//
//    @Override
//    public void detachView() {
//        compositeSubscription.clear();
//        signUpView = null;
//    }
//}
