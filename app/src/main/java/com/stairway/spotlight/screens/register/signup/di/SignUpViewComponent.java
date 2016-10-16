package com.stairway.spotlight.screens.register.signup.di;

import com.stairway.spotlight.core.di.scope.ViewScope;
import com.stairway.spotlight.screens.register.signup.SignUpFragment;
import com.stairway.spotlight.screens.register.verifyotp.VerifyOtpFragment;

import dagger.Subcomponent;

/**
 * Created by vidhun on 16/10/16.
 */
@ViewScope
@Subcomponent(modules = SignUpViewModule.class)
public interface SignUpViewComponent {
    void inject(SignUpFragment signUpFragment);
}
