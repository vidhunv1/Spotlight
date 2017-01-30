package com.stairway.spotlight.core.di.module;

import com.stairway.spotlight.models.AccessToken;
import com.stairway.spotlight.core.di.scope.UserSessionScope;
import com.stairway.data.config.XMPPManager;

import dagger.Module;
import dagger.Provides;

/**
 * Created by vidhun on 16/07/16.
 */
@Module
public class UserSessionModule {
    private AccessToken accessToken;
    public UserSessionModule(AccessToken accessToken) {
        this.accessToken = accessToken;
    }

    @Provides
    @UserSessionScope
    public AccessToken providesSession() {
        return accessToken;
    }

    @Provides
    @UserSessionScope
    public XMPPManager providesXMPPConnection() {
        XMPPManager connection = new XMPPManager(accessToken.getUserName(), accessToken.getAccessToken());
        return connection;
    }

}
