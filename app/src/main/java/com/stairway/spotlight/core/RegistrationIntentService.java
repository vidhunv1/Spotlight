package com.stairway.spotlight.core;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.stairway.data.manager.Logger;

import java.io.IOException;

/**
 * Created by vidhun on 17/10/16.
 */
public class RegistrationIntentService extends FirebaseInstanceIdService {
    private static final String TAG = "RegistrationIntentService";
    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String FCM_TOKEN = "FCMToken";

    public RegistrationIntentService() {
    }

    @Override
    public void onTokenRefresh() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        FirebaseInstanceId instanceId = FirebaseInstanceId.getInstance();
        try {
            String token = instanceId.getToken();
            sharedPreferences.edit().putString(FCM_TOKEN, token).apply();

            Logger.d("FCM token refresh: "+token);
        } catch (Exception e) {
            e.printStackTrace();
            sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, false).apply();
        }
    }

    public void sendTokenToServer(String token) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, true).apply();
    }
}
