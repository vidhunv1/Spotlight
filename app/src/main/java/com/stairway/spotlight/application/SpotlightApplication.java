package com.stairway.spotlight.application;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;
import com.stairway.spotlight.AccessTokenManager;
import com.stairway.spotlight.XMPPManager;
import com.stairway.spotlight.api.ApiManager;
import com.stairway.spotlight.core.EventBus;
import com.stairway.spotlight.core.Logger;
import com.stairway.spotlight.local.core.DatabaseManager;
import com.stairway.spotlight.models.AccessToken;

/**
 * Created by vidhun on 05/07/16.
 */
public class SpotlightApplication extends Application {

    private static SpotlightApplication instance;

    public static SpotlightApplication getContext() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
        Bus bus = new Bus(ThreadEnforcer.ANY);
        bus.register(this);

        DatabaseManager.init(this);
        EventBus.init(bus);
        AccessTokenManager.init(this);

        if(AccessTokenManager.getInstance().hasAccessToken()) {
            AccessToken accessToken = AccessTokenManager.getInstance().load();
            XMPPManager.init(accessToken.getUserName(), accessToken.getAccessToken());
            ApiManager.getInstance().setAuthorization(accessToken.getAccessToken());
        }

        if(com.stairway.spotlight.BuildConfig.DEBUG) {
            // Initialize facebook Stetho
            Stetho.initialize(
                    Stetho.newInitializerBuilder(this)
                            .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                            .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                            .build());

            Logger.init();
        }
    }
}