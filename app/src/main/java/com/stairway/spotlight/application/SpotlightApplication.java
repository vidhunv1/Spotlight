package com.stairway.spotlight.application;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.view.Window;
import android.view.WindowManager;

import com.facebook.stetho.Stetho;
import com.stairway.spotlight.R;
import com.stairway.spotlight.UserSessionManager;
import com.stairway.spotlight.ForegroundDetector;
import com.stairway.spotlight.MessageController;
import com.stairway.spotlight.XMPPManager;
import com.stairway.spotlight.api.ApiManager;
import com.stairway.spotlight.api.user.UserRequest;
import com.stairway.spotlight.api.user.UserResponse;
import com.stairway.spotlight.api.user._User;
import com.stairway.spotlight.core.Logger;
import com.stairway.spotlight.MessageService;
import com.stairway.spotlight.db.ContactStore;
import com.stairway.spotlight.db.MessageStore;
import com.stairway.spotlight.db.core.DatabaseManager;
import com.stairway.spotlight.models.UserSession;

import net.danlew.android.joda.JodaTimeAndroid;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.IOException;

import rx.Subscriber;

import static com.stairway.spotlight.core.FCMRegistrationIntentService.FCM_TOKEN;
import static com.stairway.spotlight.core.FCMRegistrationIntentService.SENT_TOKEN_TO_SERVER;

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

        if(com.stairway.spotlight.BuildConfig.DEBUG) {
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