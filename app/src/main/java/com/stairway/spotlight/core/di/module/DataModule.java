package com.stairway.spotlight.core.di.module;

import android.content.Context;

import com.stairway.data.source.auth.UserSessionStore;
import com.stairway.data.source.auth.UserAuthApi;
import com.stairway.spotlight.core.di.scope.ApplicationScope;

import dagger.Module;
import dagger.Provides;

/**
 * Created by vidhun on 20/07/16.
 */

@Module
public class DataModule {

    @Provides
    @ApplicationScope
    public UserSessionStore userSessionStore(Context context) {
        return new UserSessionStore(context);
    }

    @Provides
    @ApplicationScope
    public UserAuthApi userSessionApi() {
        return new UserAuthApi();
    }
}
