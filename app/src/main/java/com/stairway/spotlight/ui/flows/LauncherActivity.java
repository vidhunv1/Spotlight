package com.stairway.spotlight.ui.flows;

import android.app.Activity;
import android.os.Bundle;

import com.stairway.data.manager.Logger;
import com.stairway.spotlight.R;
import com.stairway.spotlight.SpotlightApplication;
import com.stairway.spotlight.ui.flows.home.HomeActivity;

import javax.inject.Inject;

import okhttp3.OkHttpClient;

public class LauncherActivity extends Activity {
    @Inject
    OkHttpClient okHttpClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        ((SpotlightApplication) getApplication()).getNetComponent().inject(this);
        Logger.d(okHttpClient.toString());

        startActivity(HomeActivity.callingIntent(this));
    }
}
