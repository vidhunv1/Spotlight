package com.stairway.spotlight.core.di.component;

import android.content.Context;

import com.stairway.data.source.user.UserSessionResult;
import com.stairway.spotlight.core.di.module.UserSessionModule;
import com.stairway.spotlight.screens.register.RegisterActivity;

/**
 * Created by vidhun on 13/07/16.
 */
public class ComponentContainer {
    private AppComponent appComponent;
    private UserSessionComponent userSessionComponent;

    public ComponentContainer(AppComponent appComponent) {
        if(appComponent == null)
            throw new IllegalStateException("AppComponent null");
        this.appComponent = appComponent;
    }

    public AppComponent getAppComponent() {
        if(appComponent == null)
            throw new IllegalStateException("AppComponent null");
        return appComponent;
    }

    public void initUserSession(UserSessionResult userSessionResult) {
        userSessionComponent = getAppComponent().plus(new UserSessionModule(userSessionResult));
    }

    public UserSessionComponent userSessionComponent() {
        if(userSessionComponent == null) {
            Context context = getAppComponent().appContext();
            context.startActivity(RegisterActivity.callingIntent(context));
        }
        return userSessionComponent;
    }
}
