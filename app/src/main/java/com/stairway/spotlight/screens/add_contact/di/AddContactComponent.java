package com.stairway.spotlight.screens.add_contact.di;

import com.stairway.spotlight.core.di.scope.ViewScope;
import com.stairway.spotlight.screens.add_contact.AddUserActivity;

import dagger.Subcomponent;

/**
 * Created by vidhun on 07/01/17.
 */

@ViewScope
@Subcomponent(modules = AddContactModule.class)
public interface AddContactComponent {
    void inject(AddUserActivity addUserActivity);
}
