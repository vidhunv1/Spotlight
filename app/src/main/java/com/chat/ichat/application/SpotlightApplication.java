package com.chat.ichat.application;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.facebook.stetho.Stetho;
import com.chat.ichat.UserSessionManager;
import com.chat.ichat.ForegroundDetector;
import com.chat.ichat.MessageController;
import com.chat.ichat.XMPPManager;
import com.chat.ichat.api.ApiManager;
import com.chat.ichat.api.user.UserRequest;
import com.chat.ichat.api.user.UserResponse;
import com.chat.ichat.api.user._User;
import com.chat.ichat.core.Logger;
import com.chat.ichat.MessageService;
import com.chat.ichat.db.ContactStore;
import com.chat.ichat.db.MessageStore;
import com.chat.ichat.db.core.DatabaseManager;
import com.chat.ichat.models.UserSession;

import net.danlew.android.joda.JodaTimeAndroid;

import rx.Subscriber;

import static com.chat.ichat.core.FCMRegistrationIntentService.FCM_TOKEN;
import static com.chat.ichat.core.FCMRegistrationIntentService.SENT_TOKEN_TO_SERVER;

/**
 * Created by vidhun on 05/07/16.
 */
public class SpotlightApplication extends Application implements ForegroundDetector.Listener{

    private static SpotlightApplication instance;

    public static SpotlightApplication getContext() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        DatabaseManager.init(this);
        UserSessionManager.init();

        if(com.chat.ichat.BuildConfig.DEBUG) {
            // Initialize facebook Stetho
            Stetho.initialize(
                    Stetho.newInitializerBuilder(this)
                            .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                            .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                            .build());

            Logger.init();
        }

        new ForegroundDetector(this);
        ForegroundDetector.getInstance().addListener(this);
        initSession();
    }

    public void initSession() {
        if(UserSessionManager.getInstance().hasAccessToken()) {
            Logger.d(this, "initUserSession");
            UserSession userSession = UserSessionManager.getInstance().load();
            Logger.d(this, "UserSession: "+userSession.toString());
            XMPPManager.init(userSession.getUserName(), userSession.getPassword());
            ApiManager.getInstance().setAuthorization(userSession.getAccessToken());
            MessageController.init(XMPPManager.getInstance().getConnection(), MessageStore.getInstance(), ContactStore.getInstance());
            JodaTimeAndroid.init(this);

            checkUploadFCMToken();
            onBecameForeground();
        }
    }

    private void checkUploadFCMToken() {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean shouldUploadToken = !sharedPreferences.getBoolean(SENT_TOKEN_TO_SERVER, false);
        Logger.d(this, "ShouldUploadFCMToken: "+shouldUploadToken);
        if(shouldUploadToken) {
            String pushString = sharedPreferences.getString(FCM_TOKEN, "");
            _User updateUser = new _User();
            updateUser.setNotificationToken(pushString);
            UserRequest userRequest = new UserRequest();
            userRequest.setUser(updateUser);
            Logger.d(this, "Access token:"+ UserSessionManager.getInstance().load().getAccessToken());
            ApiManager.getInstance().setAuthorization(UserSessionManager.getInstance().load().getAccessToken());
            ApiManager.getUserApi().updateUser(userRequest)
                    .subscribe(new Subscriber<UserResponse>() {
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

    @Override
    public void onBecameForeground() {
        if(UserSessionManager.getInstance().hasAccessToken()) {
            XMPPManager.getInstance().getConnection();
            Logger.d(this, "Starting MessageService");
            Intent intent = new Intent(this, MessageService.class);
            intent.putExtra(MessageService.TAG_ACTIVITY_NAME, this.getClass().getName());
            startService(intent);
        }
    }

    @Override
    public void onBecameBackground() {
        if(UserSessionManager.getInstance().hasAccessToken()) {
            Logger.d(this, "Stopping MessageService");
            stopService(new Intent(this, MessageService.class));
        }
    }
}