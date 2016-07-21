package com.stairway.spotlight.core.di.module;

import com.stairway.data.source.auth.UserSessionResult;
import com.stairway.spotlight.core.di.scope.UserSessionScope;

import dagger.Module;
import dagger.Provides;

/**
 * Created by vidhun on 16/07/16.
 */
@Module
public class UserSessionModule {
    private UserSessionResult session;
    public UserSessionModule(UserSessionResult appSession) {
        this.session = appSession;
    }

    @Provides
    @UserSessionScope
    public UserSessionResult providesSession() {
        return session;
    }

}
