package com.stairway.spotlight.screens.register.signup.di;

import com.stairway.spotlight.core.di.scope.ViewScope;
import com.stairway.spotlight.screens.register.signup.CreateUserUseCase;
import com.stairway.spotlight.screens.register.signup.SignUpPresenter;

import dagger.Module;
import dagger.Provides;

/**
 * Created by vidhun on 16/10/16.
 */
@Module
public class SignUpViewModule {
    @Provides
    @ViewScope
    public SignUpPresenter providesSignUpPresenter(CreateUserUseCase createUserUseCase){
        if(createUserUseCase == null)
            throw new IllegalStateException("createUserUseCase is null");
        return new SignUpPresenter(createUserUseCase);
    }
}
