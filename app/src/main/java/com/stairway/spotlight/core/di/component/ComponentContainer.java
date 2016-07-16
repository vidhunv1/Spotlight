package com.stairway.spotlight.core.di.component;

import com.stairway.spotlight.core.di.component.AppComponent;

/**
 * Created by vidhun on 13/07/16.
 */
public class ComponentContainer {
    private AppComponent appComponent;

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
}
