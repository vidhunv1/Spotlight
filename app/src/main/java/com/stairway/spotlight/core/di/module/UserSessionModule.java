package com.stairway.spotlight.core.di.module;

import com.stairway.data.model.UserSession;
import com.stairway.spotlight.core.di.scope.UserSessionScope;

import dagger.Module;
import dagger.Provides;

/**
 * Created by vidhun on 16/07/16.
 */
@Module
public class UserSessionModule {
    private UserSession session;
    public UserSessionModule(UserSession appSession) {
        this.session = appSession;
    }

    @Provides
    @UserSessionScope
    public UserSession providesSession() {
        return session;
    }
}
