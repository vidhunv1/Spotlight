package com.stairway.spotlight.ui.flows;

import android.os.Bundle;

import com.stairway.data.GenericCache;
import com.stairway.data.manager.Logger;
import com.stairway.spotlight.R;
import com.stairway.spotlight.internal.di.component.AppComponent;
import com.stairway.spotlight.ui.BaseActivity;
import com.stairway.spotlight.ui.flows.home.HomeActivity;

import javax.inject.Inject;


public class LauncherActivity extends BaseActivity {
    @Inject GenericCache genericCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        genericCache.put("dagger_test", "success");
        Logger.d(genericCache.get("dagger_test"));
        startActivity(HomeActivity.callingIntent(this));
    }

    @Override
    protected void injectComponent(AppComponent appComponent) {
        appComponent.inject(this);
    }
}
