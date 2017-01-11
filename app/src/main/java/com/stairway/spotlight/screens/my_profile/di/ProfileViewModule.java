package com.stairway.spotlight.screens.my_profile.di;

import com.stairway.spotlight.core.di.scope.ViewScope;
import com.stairway.spotlight.screens.my_profile.ProfilePresenter;
import com.stairway.spotlight.screens.my_profile.UpdateProfileDPUseCase;

import dagger.Module;
import dagger.Provides;

/**
 * Created by vidhun on 04/01/17.
 */

@Module
public class ProfileViewModule {
    @Provides
    @ViewScope
    public ProfilePresenter providesPresenter(UpdateProfileDPUseCase updateProfileDPUseCase) {
        if(updateProfileDPUseCase==null)
            throw new IllegalStateException("UseCase is null");
        return new ProfilePresenter(updateProfileDPUseCase);
    }
}
