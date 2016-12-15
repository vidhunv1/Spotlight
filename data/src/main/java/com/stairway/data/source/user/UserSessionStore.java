package com.stairway.data.source.user;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.stairway.data.config.Logger;

import rx.Observable;

/**
 * Created by vidhun on 19/07/16.
 */
public class UserSessionStore {

    private static SharedPreferences prefs;
    private Context context;

    public static String KEY_USER_NAME = "SESSION_USER_NAME";
    public static String KEY_USER_ID = "SESSION_USER_ID";
    public static String  KEY_ACCESS_TOKEN = "SESSION_ACCESS_TOKEN";
    public static String  KEY_COUNTRY_CODE = "SESSION_COUNTRY_CODE";
    public static String  KEY_PHONE = "SESSION_PHONE";
    public static String KEY_REFRESH_TOKEN = "SESSION_REFRESH_TOKEN";
    public static String KEY_EXPIRY = "SESSION_EXPIRY";

    public UserSessionStore(Context context) {
        // TODO: Storing tokens in sharedpreference: right approach?
        this.context = context;
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        if(prefs == null)
            prefs = PreferenceManager.getDefaultSharedPreferences(context);

        return prefs;
    }

    /*
    * Puts UserSession into store. Only the available parameters from the UserSessionResult
    * will be updated, so puUserSession also acts as a refresh for AccessToken.
    * */

    public Observable<Boolean> putUserSession(UserSessionResult userSessionResult) {
        return Observable.create( subscriber -> {
            final SharedPreferences.Editor editor = getSharedPreferences(context).edit();

            if(userSessionResult.getUserName()!=null || !userSessionResult.getUserName().isEmpty())
                editor.putString(KEY_USER_NAME, userSessionResult.getUserName());
            if(userSessionResult.getAccessToken()!=null && !userSessionResult.getAccessToken().isEmpty())
                editor.putString(KEY_ACCESS_TOKEN, userSessionResult.getAccessToken());
            if(userSessionResult.getRefreshToken()!=null && !userSessionResult.getRefreshToken().isEmpty())
                editor.putString(KEY_REFRESH_TOKEN, userSessionResult.getRefreshToken());
            if(userSessionResult.getCountryCode()!=null || !userSessionResult.getCountryCode().isEmpty())
                editor.putString(KEY_COUNTRY_CODE, userSessionResult.getCountryCode());
            if(userSessionResult.getPhone()!=null || !userSessionResult.getPhone().isEmpty())
                editor.putString(KEY_PHONE, userSessionResult.getPhone());
            if(userSessionResult.getExpiry()!=null || !userSessionResult.getExpiry().isEmpty())
                editor.putString(KEY_EXPIRY, userSessionResult.getExpiry());
            if(userSessionResult.getUserId()!=null || !userSessionResult.getUserId().isEmpty())
                editor.putString(KEY_USER_ID, userSessionResult.getUserId());
            editor.commit();
            Logger.d("[PutUserSession]"+userSessionResult.toString());
            subscriber.onNext(true);
            subscriber.onCompleted();
        });
    }

    /*
    * Gets UserSession from store. When Session not available: rx.onError with appropriate
    * error code(User Unregistered) will be called(may not be the right approach). Otherwise
    * calls onNext with the result.
    * */

    public Observable<UserSessionResult> getUserSession() {
        Logger.d("GETUSERSESSION from Store");

        return Observable.create(subscriber -> {

            try {
                prefs = getSharedPreferences(context);
                String userName = prefs.getString(KEY_USER_NAME, "");
                String accessToken = prefs.getString(KEY_ACCESS_TOKEN, "");
                String refreshToken = prefs.getString(KEY_REFRESH_TOKEN, "");
                String phone = prefs.getString(KEY_PHONE, "");
                String countryCode = prefs.getString(KEY_COUNTRY_CODE, "");
                String expiry = prefs.getString(KEY_EXPIRY, "");
                String userId = prefs.getString(KEY_USER_ID, "");

                if(userName.isEmpty()) {
                    Logger.d("UserSession not available");
                    //TODO: Wrap Throwable to include custom error code(User Unregistered)
                    subscriber.onError(new Throwable("User Not Logged in"));
                } else {
                    UserSessionResult userSessionResult = new UserSessionResult(userName);
                    userSessionResult.setAccessToken(accessToken);
                    userSessionResult.setCountryCode(countryCode);
                    userSessionResult.setPhone(phone);
                    userSessionResult.setRefreshToken(refreshToken);
                    userSessionResult.setExpiry(expiry);
                    userSessionResult.setUserId(userId);
                    subscriber.onNext(userSessionResult);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Logger.e("Error getting UserSession from SharedPreferences: "+e.getMessage());

                subscriber.onError(e);
            }
        });
    }
}
