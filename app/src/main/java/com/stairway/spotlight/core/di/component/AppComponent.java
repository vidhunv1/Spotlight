package com.stairway.spotlight.core.di.component;

import android.content.Context;

import com.stairway.spotlight.core.di.module.AppModule;
import com.stairway.spotlight.core.di.module.DataModule;
import com.stairway.spotlight.core.di.module.UserSessionModule;
import com.stairway.spotlight.core.di.module.UtilModule;
import com.stairway.spotlight.core.di.scope.ApplicationScope;

import dagger.Component;

/**
 * Created by vidhun on 12/07/16.
 */
@ApplicationScope
@Component(modules = {AppModule.class, UtilModule.class, DataModule.class})

public interface AppComponent {
    Context appContext();

    // Subcomponents

    // User Session Component
    UserSessionComponent plus(UserSessionModule userSessionModule); // ComponentContainer
    // Test
}
