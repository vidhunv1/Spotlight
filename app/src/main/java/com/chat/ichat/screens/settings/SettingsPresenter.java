package com.chat.ichat.screens.settings;

import android.content.SharedPreferences;

import com.chat.ichat.UserSessionManager;
import com.chat.ichat.api.ApiError;
import com.chat.ichat.api.StatusResponse;
import com.chat.ichat.api.user.UserApi;
import com.chat.ichat.api.user.UserResponse;
import com.chat.ichat.core.FCMRegistrationIntentService;
import com.chat.ichat.core.Logger;
import com.chat.ichat.models.UserSession;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by vidhun on 14/03/17.
 */

public class SettingsPresenter implements SettingsContract.Presenter {
    private SettingsContract.View settingsView;
    private CompositeSubscription subscriptions;

    private UserApi userApi;
    private UserSessionManager userSessionManager;
    private SharedPreferences sharedPreferences;

    SettingsPresenter(UserApi userApi, UserSessionManager userSessionManager, SharedPreferences defaultSharedPreference) {
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
    public void uploadProfileDP(File image) {
        Logger.d(this, "File size(MB): "+image.length()/(1024*1024));
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        UserSession userSession = UserSessionManager.getInstance().load();
        String filename = userSession.getUserId() + "_" + timeStamp;
        int i = image.getName().lastIndexOf('.');
        if (i > 0) {
            filename = filename + image.getName().substring(i);
        } else {
            filename = filename + "." + image.getName();
        }
        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), image);
        MultipartBody.Part imageFileBody = MultipartBody.Part.createFormData("profile_dp", filename, requestBody);
        Subscription subscription = userApi.uploadProfileDP(imageFileBody)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<UserResponse>() {
            @Override
            public void onCompleted() {}
            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                Logger.d(this, e.getMessage());
            }
            @Override
            public void onNext(UserResponse userResponse) {
                Logger.d(this, "Uploaded DP: "+userResponse.getUser().getProfileDP());
                UserSession u = new UserSession();
                u.setProfilePicPath(userResponse.getUser().getProfileDP());
                UserSessionManager.getInstance().save(u);
                settingsView.updateProfileDP(userResponse.getUser().getProfileDP());
            }
        });
        subscriptions.add(subscription);
    }

    @Override
    public void attachView(SettingsContract.View view) {this.settingsView = view;}

    @Override
    public void detachView() {
        subscriptions.clear();
        this.settingsView = null;
    }
}
