package com.stairway.spotlight.screens.contacts.di;

import com.stairway.spotlight.core.di.scope.ViewScope;
import com.stairway.spotlight.screens.contacts.AddContactUseCase;
import com.stairway.spotlight.screens.contacts.ContactsPresenter;
import com.stairway.spotlight.screens.contacts.GetContactsUseCase;

import dagger.Module;
import dagger.Provides;

/**
 * Created by vidhun on 13/12/16.
 */
@Module
public class ContactsViewModule {
    @Provides
    @ViewScope
    public ContactsPresenter providesContactsPresenter(AddContactUseCase addContactUseCase, GetContactsUseCase getContactsUseCase) {
        if(addContactUseCase == null || getContactsUseCase == null)
            throw  new IllegalStateException("UseCase is null");
        return new ContactsPresenter(addContactUseCase, getContactsUseCase);
    }
}
