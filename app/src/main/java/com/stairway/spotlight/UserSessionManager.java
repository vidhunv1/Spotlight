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
    static final String KEY_USER_ID_LOGIN = "UserIdLogin";
    static final String KEY_PROFILE_PIC_PATH = "ProfilePicPath";

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

    public void save(UserSession us) {
        this.userSession = null;

        if(us.getAccessToken()!=null && !us.getAccessToken().isEmpty()) {
            sharedPreferences.edit().putString(KEY_ACCESS_TOKEN, us.getAccessToken()).apply();
        }
        if(us.getUserName()!=null && !us.getUserName().isEmpty()) {
            sharedPreferences.edit().putString(KEY_USER_NAME, us.getUserName()).apply();
        }
        if(us.getExpires()!=null) {
            sharedPreferences.edit().putLong(KEY_EXPIRES_AT, us.getExpires().getTime()).apply();
        }
        if(us.getExpires()!=null) {
            sharedPreferences.edit().putLong(KEY_LAST_REFRESH, us.getExpires().getTime()).apply();
        }
        if(us.getName()!=null && !us.getName().isEmpty()) {
            sharedPreferences.edit().putString(KEY_NAME, us.getName()).apply();
        }
        if(us.getEmail()!=null && !us.getEmail().isEmpty()) {
            sharedPreferences.edit().putString(KEY_EMAIL, us.getEmail()).apply();
        }
        if(us.getPassword()!=null && !us.getPassword().isEmpty()) {
            sharedPreferences.edit().putString(KEY_PASSWORD, us.getPassword()).apply();
        }
        if(us.getCountryCode()!=null && !us.getCountryCode().isEmpty()) {
            sharedPreferences.edit().putString(KEY_COUNTRY_CODE, us.getCountryCode()).apply();
        }
        if(us.getMobile()!=null && !us.getMobile().isEmpty()) {
            sharedPreferences.edit().putString(KEY_MOBILE, us.getMobile()).apply();
        }
        if(us.getUserId()!=null && !us.getUserId().isEmpty()) {
            Logger.d(this, "UserId Saved");
            sharedPreferences.edit().putString(KEY_USER_ID, us.getUserId()).apply();
            sharedPreferences.edit().putString(KEY_USER_ID_LOGIN, us.getUserId()).apply();
        }
        if(us.getProfilePicPath()!=null && !us.getProfilePicPath().isEmpty()) {
            sharedPreferences.edit().putString(KEY_PROFILE_PIC_PATH, us.getProfilePicPath()).apply();
        }
    }

    public void clear() {
        sharedPreferences.edit().remove(KEY_ACCESS_TOKEN).apply();
        sharedPreferences.edit().remove(KEY_USER_NAME).apply();
        sharedPreferences.edit().remove(KEY_EXPIRES_AT).apply();
        sharedPreferences.edit().remove(KEY_LAST_REFRESH).apply();
        sharedPreferences.edit().remove(KEY_EMAIL).apply();
        sharedPreferences.edit().remove(KEY_COUNTRY_CODE).apply();
        sharedPreferences.edit().remove(KEY_MOBILE).apply();
        sharedPreferences.edit().remove(KEY_PASSWORD).apply();
        sharedPreferences.edit().remove(KEY_NAME).apply();
        sharedPreferences.edit().remove(KEY_USER_ID).apply();
        sharedPreferences.edit().remove(KEY_PROFILE_PIC_PATH).apply();
    }

    public boolean hasAccessToken() {
        if(userSession !=null)
            return true;
        
        return sharedPreferences.contains(KEY_ACCESS_TOKEN);
    }

    private UserSession getUserSession() {
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
        String profilePicPath = sharedPreferences.getString(KEY_PROFILE_PIC_PATH, null);

        if(access!=null && !access.isEmpty()) {
            UserSession userSession = new UserSession(access, userName, expiresAt, name, email, password);
            userSession.setCountryCode(countryCode);
            userSession.setMobile(mobile);
            userSession.setUserId(userId);
            userSession.setProfilePicPath(profilePicPath);

            return userSession;
        }

        return null;
    }

    public String getCacheID() {
        return sharedPreferences.getString(KEY_USER_ID_LOGIN, null);
    }
}