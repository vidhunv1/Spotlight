package com.stairway.spotlight.core.di.component;

import com.stairway.data.source.auth.UserSessionResult;
import com.stairway.spotlight.core.di.module.UserSessionModule;
import com.stairway.spotlight.core.di.scope.UserSessionScope;

import dagger.Subcomponent;

/**
 * Created by vidhun on 19/07/16.
 */
@UserSessionScope
@Subcomponent(modules = UserSessionModule.class)
public interface UserSessionComponent {
    UserSessionResult getUserSession();

}
