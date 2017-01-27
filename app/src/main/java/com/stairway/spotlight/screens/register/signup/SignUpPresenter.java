package com.stairway.spotlight.screens.register.signup;

import com.stairway.data.config.Logger;
import com.stairway.data.source.user.gson_models.StatusResponse;
import com.stairway.spotlight.core.UseCaseSubscriber;

import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by vidhun on 16/10/16.
 */

public class SignUpPresenter implements SignUpContract.Presenter {
    private SignUpContract.View signUpView;
    private CompositeSubscription compositeSubscription;
    private CreateUserUseCase createUserUseCase;

    public SignUpPresenter(CreateUserUseCase createUserUseCase) {
        this.createUserUseCase = createUserUseCase;
        compositeSubscription = new CompositeSubscription();
    }

    @Override
    public void createUser(String countryCode, String phone) {
        Subscription subscription = createUserUseCase.execute(countryCode, phone)
                .subscribeOn(Schedulers.io())
                .observeOn(signUpView.getUiScheduler())
                .subscribe(new UseCaseSubscriber<StatusResponse>(signUpView) {
                    @Override
                    public void onResult(StatusResponse result) {
                        Logger.d(this, " gotresutl"+result);
                        signUpView.navigateToVerifyOtp(countryCode, phone);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        Logger.d(this, " goterror"+e.getMessage());
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
