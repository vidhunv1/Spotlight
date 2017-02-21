package com.stairway.spotlight.core;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.stairway.spotlight.api.ApiManager;
import com.stairway.spotlight.api.user.UserRequest;
import com.stairway.spotlight.api.user.UserResponse;
import com.stairway.spotlight.api.user._User;

import rx.Subscriber;

/**
 * Created by vidhun on 17/10/16.
 */
public class FCMRegistrationIntentService extends FirebaseInstanceIdService {
    public static final String SENT_TOKEN_TO_SERVER = "FCM_IS_TOKEN_SENT";
    public static final String FCM_TOKEN = "FCM_TOKEN";

    public FCMRegistrationIntentService() {
    }

    @Override
    public void onTokenRefresh() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        FirebaseInstanceId instanceId = FirebaseInstanceId.getInstance();
        try {
            String token = instanceId.getToken();
            sharedPreferences.edit().putString(FCM_TOKEN, token).apply();
            uploadFCMToken(token);
            Logger.d(this, "FCM token refresh: "+token);
        } catch (Exception e) {
            e.printStackTrace();
            sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, false).apply();
        }
    }

    private void uploadFCMToken(String fcmToken) {
        //Upload to token to server if FCM token not updated
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Logger.d(this, "FCM TOKEN:"+fcmToken);
        _User updateUser = new _User();
        updateUser.setNotificationToken(fcmToken);
        UserRequest userRequest = new UserRequest();
        userRequest.setUser(updateUser);
        ApiManager.getUserApi().updateUser(userRequest)
                .subscribe(new Subscriber<UserResponse>() {
                    @Override
                    public void onCompleted() {}
                    @Override
                    public void onError(Throwable e) {
                        sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, false).apply();
                    }
                    @Override
                    public void onNext(UserResponse userResponse) {
                        sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, true).apply();
                    }
                });
    }
}
