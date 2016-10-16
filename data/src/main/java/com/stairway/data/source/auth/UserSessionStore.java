package com.stairway.data.source.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.stairway.data.manager.Logger;

import rx.Observable;

/**
 * Created by vidhun on 19/07/16.
 */
public class UserSessionStore {

    private static SharedPreferences prefs;
    private Context context;

    private String KEY_USER_ID = "SESSION_USER_ID";
    private String  KEY_ACCESS_TOKEN = "SESSION_ACCESS_TOKEN";
    private String  KEY_COUNTRY_CODE = "SESSION_COUNTRY_CODE";
    private String  KEY_PHONE = "SESSION_PHONE";
    private String KEY_REFRESH_TOKEN = "SESSION_REFRESH_TOKEN";
    private String KEY_EXPIRY = "SESSION_EXPIRY";
    private String KEY_CHAT_ID = "SESSION_CHAT_ID";

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
        Observable<Boolean> putObservable = Observable.create( subscriber -> {
            final SharedPreferences.Editor editor = getSharedPreferences(context).edit();

            if(userSessionResult.getUserId()!=null || !userSessionResult.getUserId().isEmpty())
                editor.putString(KEY_USER_ID, userSessionResult.getUserId());
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
            if(userSessionResult.getChatId()!=null || !userSessionResult.getChatId().isEmpty())
                editor.putString(KEY_CHAT_ID, userSessionResult.getChatId());
            editor.commit();
            Logger.d("[PutUserSession]"+userSessionResult.toString());
            subscriber.onNext(true);
            subscriber.onCompleted();
        });
        return putObservable;
    }

    /*
    * Gets UserSession from store. When Session not available: rx.onError with appropriate
    * error code(User Unregistered) will be called(may not be the right approach). Otherwise
    * calls onNext with the result.
    * */

    public Observable<UserSessionResult> getUserSession() {
        Logger.d("GETUSERSESSION from Store");

        Observable<UserSessionResult> getSessionObservable = Observable.create(subscriber -> {

            try {
                prefs = getSharedPreferences(context);
                String userId = prefs.getString(KEY_USER_ID, "");
                String accessToken = prefs.getString(KEY_ACCESS_TOKEN, "");
                String refreshToken = prefs.getString(KEY_REFRESH_TOKEN, "");
                String phone = prefs.getString(KEY_PHONE, "");
                String countryCode = prefs.getString(KEY_COUNTRY_CODE, "");
                String expiry = prefs.getString(KEY_EXPIRY, "");
                String chatId = prefs.getString(KEY_CHAT_ID, "");

                if(userId == null || userId.isEmpty()) {
                    Logger.d("UserSession not available");

                    //TODO: Wrap Throwable to include custom error code(User Unregistered)
                    subscriber.onError(new Throwable("User Not Logged in"));
                } else {
                    UserSessionResult userSessionResult = new UserSessionResult(userId);
                    userSessionResult.setAccessToken(accessToken);
                    userSessionResult.setCountryCode(countryCode);
                    userSessionResult.setPhone(phone);
                    userSessionResult.setRefreshToken(refreshToken);
                    userSessionResult.setExpiry(expiry);
                    userSessionResult.setChatId(chatId);
                    subscriber.onNext(userSessionResult);
                    Logger.v("Got UserSession, store: ["+accessToken+", "+refreshToken+", "+userId+"]");
                }
            } catch (Exception e) {
                e.printStackTrace();
                Logger.e("Error getting UserSession from SharedPreferences: "+e.getMessage());

                subscriber.onError(e);
            }
        });
        return getSessionObservable;
    }
}
