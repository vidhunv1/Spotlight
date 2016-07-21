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
    private String KEY_REFRESH_TOKEN = "SESSION_REFRESH_TOKEN";


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
        Logger.d("PutUserSession, store: "+ userSessionResult.toString());

        Observable<Boolean> putObservable = Observable.create( subscriber -> {
            final SharedPreferences.Editor editor = getSharedPreferences(context).edit();

            if(userSessionResult.getUserId()!=null && !userSessionResult.getUserId().isEmpty())
                editor.putString(KEY_USER_ID, userSessionResult.getUserId());
            if(userSessionResult.getAccessToken()!=null && !userSessionResult.getAccessToken().isEmpty())
                editor.putString(KEY_ACCESS_TOKEN, userSessionResult.getAccessToken());
            if(userSessionResult.getRefreshToken()!=null && !userSessionResult.getRefreshToken().isEmpty())
                editor.putString(KEY_REFRESH_TOKEN, userSessionResult.getRefreshToken());
            editor.commit();

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

                if(userId == null || userId.isEmpty()) {
                    Logger.d("UserSession not available");

                    //TODO: Wrap Throwable to include custom error code(User Unregistered)
                    subscriber.onError(new Throwable("User Not Logged in"));
                } else {
                    subscriber.onNext(new UserSessionResult(accessToken, refreshToken, userId));
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
