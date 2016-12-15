package com.stairway.spotlight.screens.contacts.di;

import com.stairway.spotlight.core.di.scope.ViewScope;
import com.stairway.spotlight.screens.contacts.ContactsActivity;

import dagger.Subcomponent;

/**
 * Created by vidhun on 13/12/16.
 */
@ViewScope
@Subcomponent(modules = ContactsViewModule.class)
public interface ContactsViewComponent {
    void inject(ContactsActivity contactsActivity);
}
