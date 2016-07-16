package com.stairway.spotlight.core.di.module;

import com.stairway.data.model.AppSession;
import com.stairway.spotlight.core.di.scope.AppSessionScope;

import dagger.Module;
import dagger.Provides;

/**
 * Created by vidhun on 16/07/16.
 */
@Module
public class AppSessionModule {
    private AppSession session;
    public AppSessionModule(AppSession appSession) {
        this.session = appSession;
    }

    @Provides
    @AppSessionScope
    public AppSession providesSession() {
        return session;
    }
}
