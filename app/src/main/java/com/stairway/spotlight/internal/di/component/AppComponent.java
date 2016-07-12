package com.stairway.spotlight.internal.di.component;

import com.stairway.spotlight.internal.di.module.AppModule;
import com.stairway.spotlight.internal.di.module.NetModule;
import com.stairway.spotlight.internal.di.module.UtilModule;
import com.stairway.spotlight.internal.di.scope.ApplicationScope;
import com.stairway.spotlight.ui.flows.LauncherActivity;
import com.stairway.spotlight.ui.flows.home.HomeActivity;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by vidhun on 12/07/16.
 */
@ApplicationScope
@Component(modules = {AppModule.class, UtilModule.class, NetModule.class})
public interface AppComponent {
    void inject(LauncherActivity activity);
    void inject(HomeActivity activity);
}
