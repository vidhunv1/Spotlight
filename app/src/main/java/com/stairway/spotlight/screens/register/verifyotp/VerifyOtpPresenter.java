package com.stairway.spotlight.screens.register.verifyotp;

import com.stairway.data.error.DataException;
import com.stairway.data.source.user.UserSessionResult;
import com.stairway.spotlight.AccessToken;
import com.stairway.spotlight.core.UseCaseSubscriber;
import com.stairway.spotlight.screens.register.initialize.SyncContactsUseCase;

import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by vidhun on 25/07/16.
 */
public class VerifyOtpPresenter implements VerifyOtpContract.Presenter {

    private VerifyOtpContract.View verifyOtpView;
    private CompositeSubscription compositeSubscription;
    private RegisterUseCase registerUseCase;

    public VerifyOtpPresenter(RegisterUseCase registerUseCase) {
        this.registerUseCase = registerUseCase;
        this.compositeSubscription = new CompositeSubscription();
    }

    @Override
    public void registerUser(String countryCode, String mobile, String otp) {
        Subscription subscription = registerUseCase.execute(countryCode, mobile, otp)
                .subscribeOn(Schedulers.io())
                .observeOn(verifyOtpView.getUiScheduler())
                .subscribe(new UseCaseSubscriber<AccessToken>(verifyOtpView) {
                    @Override
                    public void onResult(AccessToken accessToken) {
                        verifyOtpView.navigateToInitializeFragment(accessToken);
                    }

                    @Override
                    public void onError(Throwable e) {
                        if(e instanceof DataException)
                            if(((DataException) e).getKind()== DataException.Kind.OTP_INVALID)
                                verifyOtpView.invalidOtpError();
                    }
                });

        compositeSubscription.add(subscription);
    }

    @Override
    public void attachView(VerifyOtpContract.View view) {
        this.verifyOtpView = view;
    }

    @Override
    public void detachView() {
        compositeSubscription.clear();
        verifyOtpView = null;
    }
}
