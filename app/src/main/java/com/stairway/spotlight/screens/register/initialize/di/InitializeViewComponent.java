package com.stairway.spotlight.screens.register.initialize.di;

/**
 * Created by vidhun on 09/12/16.
 */

import com.stairway.spotlight.core.di.scope.ViewScope;
import com.stairway.spotlight.screens.register.initialize.InitializeFragment;
import dagger.Subcomponent;

@ViewScope
@Subcomponent(modules = InitializeViewModule.class)
public interface InitializeViewComponent {
    void inject(InitializeFragment initializeFragment);
}
