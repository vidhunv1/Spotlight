package com.stairway.spotlight.screens.register.verifyotp.di;

import android.content.Context;

import com.stairway.spotlight.core.di.scope.ViewScope;
import com.stairway.spotlight.screens.register.verifyotp.RegisterUseCase;
import com.stairway.spotlight.screens.register.initialize.SyncContactsUseCase;
import com.stairway.spotlight.screens.register.verifyotp.VerifyOtpPresenter;

import dagger.Module;
import dagger.Provides;

/**
 * Created by vidhun on 25/07/16.
 */

@Module
public class VerifyOtpViewModule {
    private final Context context;

    public VerifyOtpViewModule(Context context) {
        this.context = context;
    }

    @Provides
    @ViewScope
    public VerifyOtpPresenter providesVerifyOtpPresenter(RegisterUseCase registerUseCase) {
        if(registerUseCase == null)
            throw new IllegalStateException("UseCase is null");
        return new VerifyOtpPresenter(registerUseCase);
    }

    @Provides
    @ViewScope
    public Context providesContext() {
        return this.context;
    }
}
