package com.stairway.spotlight.core.di.component;

import com.stairway.data.model.UserSession;
import com.stairway.spotlight.core.di.module.UserSessionModule;
import com.stairway.spotlight.core.di.scope.UserSessionScope;

import dagger.Component;
import dagger.Subcomponent;

/**
 * Created by vidhun on 19/07/16.
 */
@UserSessionScope
@Subcomponent(modules = UserSessionModule.class)
public interface UserSessionComponent {
    UserSession getUserSession();


}
