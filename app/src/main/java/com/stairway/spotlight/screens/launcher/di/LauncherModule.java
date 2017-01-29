package com.stairway.spotlight.screens.launcher.di;

import android.content.Context;

import com.stairway.spotlight.core.di.scope.ViewScope;
import com.stairway.spotlight.screens.launcher.LauncherPresenter;

import dagger.Module;
import dagger.Provides;

/**
 * Created by vidhun on 21/07/16.
 */
@Module
public class LauncherModule {
    private final Context context;

    public LauncherModule(Context context) {
        this.context = context;
    }

    @Provides
    @ViewScope
    public LauncherPresenter providesLauncherPresenter() {
        return new LauncherPresenter();
    }
}