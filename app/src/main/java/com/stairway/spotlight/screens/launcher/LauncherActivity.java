package com.stairway.spotlight.screens.launcher;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.stairway.spotlight.UserSessionManager;
import com.stairway.spotlight.R;
import com.stairway.spotlight.core.Logger;
import com.stairway.spotlight.models.UserSession;
import com.stairway.spotlight.screens.home.HomeActivity;
import com.stairway.spotlight.screens.user_id.SetUserIdActivity;
import com.stairway.spotlight.screens.welcome.WelcomeActivity;

import butterknife.ButterKnife;


public class LauncherActivity extends AppCompatActivity implements LauncherContract.View {

    UserSessionManager userSessionManager;

    private FirebaseAnalytics firebaseAnalytics;
    private final String SCREEN_NAME = "launcher";

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

        userSessionManager = UserSessionManager.getInstance();
        if(userSessionManager.hasAccessToken()) {
            UserSession userSession = userSessionManager.load();
            if(userSession.getUserId()!=null) {
                navigateToHomeActivity();
            } else {
                startActivity(SetUserIdActivity.callingIntent(this));
            }
        } else {
            navigateToWelcomeActivity();
        }

        ButterKnife.bind(this);

        /*              Analytics           */
        this.firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        firebaseAnalytics.setUserId(userSessionManager.load().getUserName());
        firebaseAnalytics.setCurrentScreen(this, SCREEN_NAME, null);
    }

    @Override
    public void showError(String title, String message) {}

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
        startActivity(HomeActivity.callingIntent(this,0,null));
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
