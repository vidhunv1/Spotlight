package com.stairway.spotlight.core.di.module;

import android.app.Application;
import android.content.Context;

import com.stairway.spotlight.SpotlightApplication;
import com.stairway.spotlight.core.di.scope.ApplicationScope;

import dagger.Module;
import dagger.Provides;

/**
 * Created by vidhun on 12/07/16.
 */
@Module
public class AppModule {
    private SpotlightApplication application;

    public AppModule(SpotlightApplication application) {
        this.application = application;
    }

    @Provides
    @ApplicationScope
    Application providesApplication() {
        return application;
    }

    @Provides
    @ApplicationScope
    Context provideApplicationContext() {
        return application;
    }
}
