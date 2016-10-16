package com.stairway.spotlight.core.di.component;

import android.content.Context;

import com.stairway.spotlight.core.di.module.AppModule;
import com.stairway.spotlight.core.di.module.DataModule;
import com.stairway.spotlight.core.di.module.UserSessionModule;
import com.stairway.spotlight.core.di.module.UtilModule;
import com.stairway.spotlight.core.di.scope.ApplicationScope;
import com.stairway.spotlight.screens.launcher.LauncherActivity;
import com.stairway.spotlight.screens.home.HomeActivity;
import com.stairway.spotlight.screens.home.chatlist.di.ChatListViewComponent;
import com.stairway.spotlight.screens.home.chatlist.di.ChatListViewModule;
import com.stairway.spotlight.screens.launcher.di.LauncherComponent;
import com.stairway.spotlight.screens.launcher.di.LauncherModule;
import com.stairway.spotlight.screens.register.signup.di.SignUpViewComponent;
import com.stairway.spotlight.screens.register.signup.di.SignUpViewModule;
import com.stairway.spotlight.screens.register.verifyotp.di.VerifyOtpViewComponent;
import com.stairway.spotlight.screens.register.verifyotp.di.VerifyOtpViewModule;
import com.stairway.spotlight.screens.welcome.di.WelcomeComponent;
import com.stairway.spotlight.screens.welcome.di.WelcomeModule;

import dagger.Component;

/**
 * Created by vidhun on 12/07/16.
 */
@ApplicationScope
@Component(modules = {AppModule.class, UtilModule.class, DataModule.class})

public interface AppComponent {
    Context appContext();

    // Subcomponents

    LauncherComponent plus(LauncherModule launcherModule);
    WelcomeComponent plus(WelcomeModule welcomeModule);

    // User Session Component
    UserSessionComponent plus(UserSessionModule userSessionModule); // ComponentContainer
    VerifyOtpViewComponent plus(VerifyOtpViewModule verifyOtpViewModule);
    SignUpViewComponent plus(SignUpViewModule signUpViewModule);

    // Test
}
