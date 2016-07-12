package com.stairway.spotlight.internal.di.module;

import android.app.Application;

import dagger.Module;

/**
 * Created by vidhun on 12/07/16.
 */
@Module
public class AppModule {
    Application application;

    public AppModule(Application application) {
        this.application = application;
    }

    Application providesApplication() {
        return application;
    }
}
