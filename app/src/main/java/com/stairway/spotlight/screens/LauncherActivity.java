package com.stairway.spotlight.screens;

import android.os.Bundle;

import com.stairway.data.GenericCache;
import com.stairway.spotlight.R;
import com.stairway.spotlight.internal.di.component.AppComponent;
import com.stairway.spotlight.screens.home.HomeActivity;

import javax.inject.Inject;


public class LauncherActivity extends BaseActivity {
    @Inject GenericCache genericCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        startActivity(HomeActivity.callingIntent(this));
    }

    @Override
    protected void injectComponent(AppComponent appComponent) {
        appComponent.inject(this);
    }
}
