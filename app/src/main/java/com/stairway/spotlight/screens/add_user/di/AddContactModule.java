package com.stairway.spotlight.screens.add_user.di;

import com.stairway.spotlight.core.di.scope.ViewScope;
import com.stairway.spotlight.screens.add_user.AddUserPresenter;
import com.stairway.spotlight.screens.add_user.AddUserUseCase;

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
