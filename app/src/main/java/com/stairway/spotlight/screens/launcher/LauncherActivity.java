package com.stairway.spotlight.screens.launcher;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.stairway.spotlight.AccessTokenManager;
import com.stairway.spotlight.R;
import com.stairway.spotlight.core.BaseActivity;
import com.stairway.spotlight.core.Logger;
import com.stairway.spotlight.screens.home.HomeActivity;
import com.stairway.spotlight.screens.welcome.WelcomeActivity;

import butterknife.ButterKnife;


public class LauncherActivity extends AppCompatActivity implements LauncherContract.View {

    AccessTokenManager accessTokenManager;

    public static Intent callingIntent(Context context) {
        Intent intent = new Intent(context, LauncherActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.d(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        accessTokenManager = AccessTokenManager.getInstance();
        if(accessTokenManager.hasAccessToken()) {
            navigateToHomeActivity();
        } else {
            navigateToWelcomeActivity();
        }

        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void navigateToHomeActivity() {
        startActivity(HomeActivity.callingIntent(this));
        this.overridePendingTransition(0,0);
        finish();
    }

    @Override
    public void navigateToWelcomeActivity() {
        startActivity(WelcomeActivity.callingIntent(this));
        this.overridePendingTransition(0,0);
        finish();
    }
}
