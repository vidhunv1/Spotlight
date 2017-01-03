package com.stairway.spotlight.screens.home.profile.di;

import com.stairway.spotlight.core.di.scope.ViewScope;
import com.stairway.spotlight.screens.home.profile.ProfileFragment;

import dagger.Subcomponent;

/**
 * Created by vidhun on 04/01/17.
 */

@ViewScope
@Subcomponent(modules = ProfileViewModule.class)
public interface ProfileViewComponent {
    void inject(ProfileFragment profileFragment);
}
