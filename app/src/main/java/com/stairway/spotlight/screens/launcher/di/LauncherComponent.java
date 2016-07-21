package com.stairway.spotlight.screens.launcher.di;

import com.stairway.spotlight.core.di.scope.ViewScope;
import com.stairway.spotlight.screens.launcher.LauncherActivity;

import dagger.Subcomponent;

/**
 * Created by vidhun on 21/07/16.
 */
@ViewScope
@Subcomponent(modules = LauncherModule.class)
public interface LauncherComponent {
    void inject(LauncherActivity launcherActivity);
}
