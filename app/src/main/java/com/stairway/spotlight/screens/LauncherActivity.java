package com.stairway.spotlight.screens;

import android.os.Bundle;
import android.support.v7.app.ActionBar;

import com.stairway.data.GenericCache;
import com.stairway.data.manager.Logger;
import com.stairway.spotlight.R;
import com.stairway.spotlight.internal.di.component.AppComponent;
import com.stairway.spotlight.internal.di.component.ComponentContainer;
import com.stairway.spotlight.screens.home.HomeActivity;

import javax.inject.Inject;


public class LauncherActivity extends BaseActivity {
    @Inject GenericCache genericCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        Logger.v("[Launcher Activity]");
        startActivity(HomeActivity.callingIntent(this));
    }

    @Override
    protected void injectComponent(ComponentContainer componentContainer) {
        componentContainer.getAppComponent().inject(this);
    }
}
