package com.stairway.spotlight.internal.di.component;

import com.stairway.spotlight.internal.di.module.NetModule;
import com.stairway.spotlight.ui.flows.LauncherActivity;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by vidhun on 12/07/16.
 */
@Singleton
@Component(modules = NetModule.class)
public interface NetComponent {
    void inject(LauncherActivity activity);
}
