package com.stairway.spotlight.screens.add_contact.di;

import com.stairway.spotlight.core.di.scope.ViewScope;
import com.stairway.spotlight.screens.add_contact.AddUserPresenter;
import com.stairway.spotlight.screens.add_contact.AddUserUseCase;

import dagger.Module;
import dagger.Provides;

/**
 * Created by vidhun on 07/01/17.
 */

@Module
public class AddContactModule {
    @Provides
    @ViewScope
    public AddUserPresenter providesPresenter(AddUserUseCase addUserUseCase) {
        return new AddUserPresenter(addUserUseCase);
    }
}
