package com.stairway.spotlight.screens.home.contacts.di;

import com.stairway.spotlight.core.di.scope.ViewScope;
import com.stairway.spotlight.screens.home.contacts.ContactListFragment;

import dagger.Subcomponent;

/**
 * Created by vidhun on 01/09/16.
 */
@ViewScope
@Subcomponent(modules = ContactListViewModule.class)
public interface ContactListViewComponent {
    void inject(ContactListFragment contactListFragment);
}
