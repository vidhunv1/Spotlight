package com.stairway.spotlight.screens.register.verifyotp.di;

import com.stairway.spotlight.core.di.scope.ViewScope;
import com.stairway.spotlight.screens.register.verifyotp.RegisterUseCase;
import com.stairway.spotlight.screens.register.verifyotp.VerifyOtpPresenter;

import dagger.Module;
import dagger.Provides;

/**
 * Created by vidhun on 25/07/16.
 */

@Module
public class VerifyOtpViewModule {

    @Provides
    @ViewScope
    public VerifyOtpPresenter providesVerifyOtpPresenter(RegisterUseCase registerUseCase) {
        if(registerUseCase == null)
            throw new IllegalStateException("RegisterUseCase is null");
        return new VerifyOtpPresenter(registerUseCase);
    }
}
