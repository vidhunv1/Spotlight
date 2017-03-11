package com.stairway.spotlight;


import android.content.Context;
import android.content.SharedPreferences;
import com.stairway.spotlight.application.SpotlightApplication;
import com.stairway.spotlight.core.Logger;
import com.stairway.spotlight.models.UserSession;
import java.util.Date;
import rx.Observable;
import rx.functions.Func0;
import rx.schedulers.Schedulers;
/**
 * Created by vidhun on 29/01/17.
 */
public class UserSessionManager {
    static final String KEY_ACCESS_TOKEN = "UserSession";
    static final String KEY_EXPIRES_AT = "ExpiresAt";
    static final String KEY_LAST_REFRESH = "LastRefresh";
    static final String KEY_USER_NAME = "UserName";
    static final String KEY_NAME = "Name";
    static final String KEY_EMAIL = "Email";
    static final String KEY_PASSWORD = "Password";
    static final String KEY_MOBILE = "Mobile";
    static final String KEY_COUNTRY_CODE = "CountryCode";
    static final String KEY_USER_ID = "UserId";

    private static UserSessionManager instance;
    private UserSession userSession;
    private final SharedPreferences sharedPreferences;
    private UserSessionManager() {
        this.sharedPreferences = SpotlightApplication.getContext().getSharedPreferences("UserSessionManager", Context.MODE_PRIVATE);
    }

    public static UserSessionManager getInstance() {
        if (instance == null)
            throw new IllegalStateException("UserSessionManager is not initialized");

        return instance;
    }

    public static void init() {
        instance = new UserSessionManager();
    }

    public UserSession load() {
        if (this.userSession ==null && hasAccessToken())
            this.userSession = getUserSession();
        return userSession;
    }

    public void save(UserSession userSession) {
        this.userSession = null;
        Observable.defer(new Func0<Observable<Integer>>() {
            @Override
            public Observable<Integer> call() {
                return Observable.create(subscriber -> {
                    if(userSession.getAccessToken()!=null && !userSession.getAccessToken().isEmpty()) {
                        sharedPreferences.edit().putString(KEY_ACCESS_TOKEN, userSession.getAccessToken()).apply();
                    }
                    if(userSession.getUserName()!=null && !userSession.getUserName().isEmpty()) {
                        sharedPreferences.edit().putString(KEY_USER_NAME, userSession.getUserName()).apply();
                    }
                    if(userSession.getExpires()!=null) {
                        sharedPreferences.edit().putLong(KEY_EXPIRES_AT, userSession.getExpires().getTime()).apply();
                    }
                    if(userSession.getExpires()!=null) {
                        sharedPreferences.edit().putLong(KEY_LAST_REFRESH, userSession.getExpires().getTime()).apply();
                    }
                    if(userSession.getName()!=null && !userSession.getName().isEmpty()) {
                        sharedPreferences.edit().putString(KEY_NAME, userSession.getName()).apply();
                    }
                    if(userSession.getEmail()!=null && !userSession.getEmail().isEmpty()) {
                        sharedPreferences.edit().putString(KEY_EMAIL, userSession.getEmail()).apply();
                    }
                    if(userSession.getPassword()!=null && !userSession.getPassword().isEmpty()) {
                        sharedPreferences.edit().putString(KEY_PASSWORD, userSession.getPassword()).apply();
                    }
                    if(userSession.getCountryCode()!=null && !userSession.getCountryCode().isEmpty()) {
                        sharedPreferences.edit().putString(KEY_COUNTRY_CODE, userSession.getCountryCode()).apply();
                    }
                    if(userSession.getMobile()!=null && !userSession.getMobile().isEmpty()) {
                        sharedPreferences.edit().putString(KEY_MOBILE, userSession.getMobile()).apply();
                    }
                    if(userSession.getUserId()!=null && !userSession.getUserId().isEmpty()) {
                        Logger.d(this, "UserId Saved");
                        sharedPreferences.edit().putString(KEY_USER_ID, userSession.getUserId()).apply();
                    }
                });
            }
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    public void clear() {
        Observable.defer(new Func0<Observable<Integer>>() {
            @Override
            public Observable<Integer> call() {
                return Observable.create(subscriber -> {
                    sharedPreferences.edit().remove(KEY_ACCESS_TOKEN).apply();
                    sharedPreferences.edit().remove(KEY_USER_NAME).apply();
                    sharedPreferences.edit().remove(KEY_EXPIRES_AT).apply();
                    sharedPreferences.edit().remove(KEY_LAST_REFRESH).apply();
                });
            }
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    public boolean hasAccessToken() {
        if(userSession !=null)
            return true;
        
        return sharedPreferences.contains(KEY_ACCESS_TOKEN);
    }

    private UserSession getUserSession() {
        if (userSession != null)
            return userSession;

        String access = sharedPreferences.getString(KEY_ACCESS_TOKEN, null);
        Date expiresAt = new Date(sharedPreferences.getLong(KEY_EXPIRES_AT, 0));
//        Date lastRefresh = new Date(sharedPreferences.getLong(KEY_LAST_REFRESH, 0));
        String userName = sharedPreferences.getString(KEY_USER_NAME, null);
        String name = sharedPreferences.getString(KEY_NAME, null);
        String email = sharedPreferences.getString(KEY_EMAIL, null);
        String password = sharedPreferences.getString(KEY_PASSWORD, null);
        String mobile = sharedPreferences.getString(KEY_MOBILE, null);
        String countryCode = sharedPreferences.getString(KEY_COUNTRY_CODE, null);
        String userId = sharedPreferences.getString(KEY_USER_ID, null);

        if(access!=null && !access.isEmpty()) {
            UserSession userSession = new UserSession(access, userName, expiresAt, name, email, password);
            userSession.setCountryCode(countryCode);
            userSession.setMobile(mobile);
            userSession.setUserId(userId);

            return userSession;
        }

        return null;
    }
}