package com.stairway.spotlight.screens.home.contactlist.di;

import com.stairway.data.source.contacts.ContactsContent;
import com.stairway.spotlight.core.di.scope.ViewScope;
import com.stairway.spotlight.screens.home.contactlist.ContactListPresenter;
import com.stairway.spotlight.screens.home.contactlist.GetContactsUseCase;

import dagger.Module;
import dagger.Provides;

/**
 * Created by vidhun on 01/09/16.
 */
@Module
public class ContactListViewModule {
    @Provides
    @ViewScope
    public ContactListPresenter providesContactListPresenter(GetContactsUseCase getContactsUseCase) {
        if(getContactsUseCase==null)
            throw new IllegalStateException("UseCase is null");
        return new ContactListPresenter(getContactsUseCase);
    }
}
