package com.chat.ichat.screens.launcher;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.chat.ichat.UserSessionManager;
import com.chat.ichat.R;
import com.chat.ichat.core.Logger;
import com.chat.ichat.models.UserSession;
import com.chat.ichat.screens.home.HomeActivity;
import com.chat.ichat.screens.user_id.SetUserIdActivity;
import com.chat.ichat.screens.welcome.WelcomeActivity;

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
        String id = userSessionManager.getCacheID();
        this.firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        if(id!=null && !id.isEmpty()) {
            firebaseAnalytics.setUserId(id);
        }
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
