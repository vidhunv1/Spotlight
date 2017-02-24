package com.stairway.spotlight;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.stairway.spotlight.application.SpotlightApplication;
import com.stairway.spotlight.core.Logger;
import com.stairway.spotlight.models.AccessToken;

import java.util.Date;

import rx.Observable;
import rx.functions.Func0;
import rx.schedulers.Schedulers;

/**
 * Created by vidhun on 29/01/17.
 */

public class AccessTokenManager {
    static final String ACCESS_TOKEN_KEY = "AccessToken";
    static final String EXPIRES_AT_KEY = "ExpiresAt";
    static final String LAST_REFRESH_KEY = "LastRefresh";
    static final String USER_NAME_KEY = "UserId";
    private static AccessTokenManager instance;
    private AccessToken accessToken;
    private final SharedPreferences sharedPreferences;
    private AccessTokenManager() {
        this.sharedPreferences = SpotlightApplication.getContext().getSharedPreferences("AccessTokenManager", Context.MODE_PRIVATE);
    }

    public static AccessTokenManager getInstance() {
        if (instance == null)
            throw new IllegalStateException("AccessTokenManager is not initialized");

        return instance;
    }

    public static void init() {
        instance = new AccessTokenManager();
    }

    public AccessToken load() {
        if (this.accessToken==null && hasAccessToken())
            this.accessToken = getAccessToken();
        return accessToken;
    }

    public void save(AccessToken accessToken) {
        Observable.defer(new Func0<Observable<Integer>>() {
            @Override
            public Observable<Integer> call() {
                return Observable.create(subscriber -> {
                    sharedPreferences.edit().putString(ACCESS_TOKEN_KEY, accessToken.getAccessToken()).apply();
                    sharedPreferences.edit().putString(USER_NAME_KEY, accessToken.getUserName()).apply();
                    sharedPreferences.edit().putLong(EXPIRES_AT_KEY, accessToken.getExpires().getTime()).apply();
                    sharedPreferences.edit().putLong(LAST_REFRESH_KEY, accessToken.getExpires().getTime()).apply();
                });
            }
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    public void clear() {
        Observable.defer(new Func0<Observable<Integer>>() {
            @Override
            public Observable<Integer> call() {
                return Observable.create(subscriber -> {
                    sharedPreferences.edit().remove(ACCESS_TOKEN_KEY).apply();
                    sharedPreferences.edit().remove(USER_NAME_KEY).apply();
                    sharedPreferences.edit().remove(EXPIRES_AT_KEY).apply();
                    sharedPreferences.edit().remove(LAST_REFRESH_KEY).apply();
                });
            }
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    public boolean hasAccessToken() {
        if(accessToken!=null)
            return true;
        
        return sharedPreferences.contains(ACCESS_TOKEN_KEY);
    }

    private AccessToken getAccessToken() {
        if (accessToken != null)
            return accessToken;

        String access = sharedPreferences.getString(ACCESS_TOKEN_KEY, null);
        Date expiresAt = new Date(sharedPreferences.getLong(EXPIRES_AT_KEY, 0));
//        Date lastRefresh = new Date(sharedPreferences.getLong(LAST_REFRESH_KEY, 0));
        String userName = sharedPreferences.getString(USER_NAME_KEY, null);

        if(access!=null && !access.isEmpty())
            return new AccessToken(access, userName,  expiresAt);
        return null;
    }
}
