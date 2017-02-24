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
            sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, false).apply();
            Logger.d(this, "Got FCM token: "+token);
        } catch (Exception e) {
            e.printStackTrace();
            sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, false).apply();
        }
    }
}