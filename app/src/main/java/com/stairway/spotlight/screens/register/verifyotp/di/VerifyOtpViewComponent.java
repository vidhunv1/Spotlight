package com.stairway.spotlight.screens.register.verifyotp.di;

import com.stairway.spotlight.core.di.scope.ViewScope;
import com.stairway.spotlight.screens.register.verifyotp.VerifyOtpFragment;

import dagger.Subcomponent;

/**
 * Created by vidhun on 25/07/16.
 */
@ViewScope
@Subcomponent(modules = VerifyOtpViewModule.class)
public interface VerifyOtpViewComponent {
    void inject(VerifyOtpFragment verifyOtpFragment);
}
