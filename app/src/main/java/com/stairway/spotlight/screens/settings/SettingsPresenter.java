package com.stairway.spotlight.screens.settings;

import android.content.SharedPreferences;

import com.stairway.spotlight.UserSessionManager;
import com.stairway.spotlight.api.ApiError;
import com.stairway.spotlight.api.StatusResponse;
import com.stairway.spotlight.api.user.UserApi;
import com.stairway.spotlight.core.FCMRegistrationIntentService;

import okhttp3.Response;
import okhttp3.ResponseBody;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static com.stairway.spotlight.core.FCMRegistrationIntentService.SENT_TOKEN_TO_SERVER;

/**
 * Created by vidhun on 14/03/17.
 */

public class SettingsPresenter implements SettingsContract.Presenter {
    private SettingsContract.View settingsView;
    private CompositeSubscription subscriptions;

    private UserApi userApi;
    private UserSessionManager userSessionManager;
    private SharedPreferences sharedPreferences;

    public SettingsPresenter(UserApi userApi, UserSessionManager userSessionManager, SharedPreferences defaultSharedPreference) {
        this.userApi = userApi;
        this.userSessionManager = userSessionManager;
        this.sharedPreferences = defaultSharedPreference;
        subscriptions = new CompositeSubscription();
    }

    @Override
    public void logoutUser() {
        Subscription subscription = userApi.logout()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<StatusResponse>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        ApiError error = new ApiError(e);
                        settingsView.showError(error.getTitle(), error.getMessage());
                    }

                    @Override
                    public void onNext(StatusResponse response) {
                        if(!response.isSuccess()) {
                            settingsView.showError(response.getError().getTitle(), response.getError().getMessage());
                        } else {
                            userSessionManager.clear();
                            sharedPreferences.edit().putBoolean(FCMRegistrationIntentService.SENT_TOKEN_TO_SERVER, false).apply();
                            settingsView.onLogoutSuccess();
                        }
                    }
                });
        subscriptions.add(subscription);
    }

    @Override
    public void attachView(SettingsContract.View view) {
        this.settingsView = view;
    }

    @Override
    public void detachView() {
        subscriptions.clear();
        this.settingsView = null;
    }
}
