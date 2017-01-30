package com.stairway.spotlight;


import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.stairway.data.config.Logger;
import com.stairway.spotlight.application.SpotlightApplication;
import com.stairway.spotlight.models.AccessToken;

import java.util.Date;

import rx.Observable;
import rx.functions.Func0;
import rx.schedulers.Schedulers;

/**
 * Created by vidhun on 29/01/17.
 */

public class AccessTokenManager {
    private static volatile AccessTokenManager instance;

    public static AccessTokenManager getInstance() {
        if (instance == null)
            instance = new AccessTokenManager();

        return instance;
    }

    static final String ACCESS_TOKEN_KEY = "AccessTokenCache.AccessToken";
    static final String EXPIRES_AT_KEY = "AccessTokenCache.ExpiresAt";
    static final String LAST_REFRESH_KEY = "AccessTokenCache.LastRefresh";
    static final String USER_NAME_KEY = "AccessTokenCache.UserId";

    private final SharedPreferences sharedPreferences;

    public AccessTokenManager() {
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(SpotlightApplication.getContext());
    }

    public AccessToken load() {
        AccessToken accessToken = null;
        if (hasAccessToken())
            accessToken = getAccessToken();
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
        Logger.d(this, sharedPreferences.contains(ACCESS_TOKEN_KEY)+"");
        return sharedPreferences.contains(ACCESS_TOKEN_KEY);
    }

    private AccessToken getAccessToken() {
        String accessToken = sharedPreferences.getString(ACCESS_TOKEN_KEY, null);
        Date expiresAt = new Date(sharedPreferences.getLong(EXPIRES_AT_KEY, 0));
//        Date lastRefresh = new Date(sharedPreferences.getLong(LAST_REFRESH_KEY, 0));
        String userName = sharedPreferences.getString(USER_NAME_KEY, null);

        if (accessToken != null)
            return new AccessToken(accessToken, userName,  expiresAt);
        return null;
    }
}
