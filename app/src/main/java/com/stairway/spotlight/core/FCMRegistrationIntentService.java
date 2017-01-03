package com.stairway.spotlight.core;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.stairway.data.config.Logger;
import com.stairway.data.source.user.UserApi;
import com.stairway.data.source.user.gson_models.User;
import com.stairway.data.source.user.gson_models.UserResponse;

import rx.Subscriber;

import static com.stairway.data.source.user.UserSessionStore.KEY_ACCESS_TOKEN;

/**
 * Created by vidhun on 17/10/16.
 */
public class FCMRegistrationIntentService extends FirebaseInstanceIdService {
    public static final String SENT_TOKEN_TO_SERVER = "FCM_IS_TOKEN_SENT";
    public static final String FCM_TOKEN = "FCM_TOKEN";
    UserApi userApi;

    public FCMRegistrationIntentService() {
    }

    @Override
    public void onTokenRefresh() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        FirebaseInstanceId instanceId = FirebaseInstanceId.getInstance();
        try {
            String token = instanceId.getToken();
            sharedPreferences.edit().putString(FCM_TOKEN, token).apply();
            sendTokenToServer(token);
            Logger.d("FCM token refresh: "+token);
        } catch (Exception e) {
            e.printStackTrace();
            sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, false).apply();
        }
    }

    public void sendTokenToServer(String token){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        String accessToken = sharedPreferences.getString(KEY_ACCESS_TOKEN, "");
        User updateUser = new User();
        updateUser.setNotificationToken(token);

        userApi.updateUser(updateUser, accessToken).subscribe(new Subscriber<UserResponse>() {
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
