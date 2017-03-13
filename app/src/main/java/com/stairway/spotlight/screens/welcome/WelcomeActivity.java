package com.stairway.spotlight.screens.welcome;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.stairway.spotlight.R;

import com.stairway.spotlight.screens.login.LoginActivity;
import com.stairway.spotlight.screens.sign_up.SignUpActivity;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class WelcomeActivity extends AppCompatActivity implements WelcomeContract.View {
    public static Intent callingIntent(Context context) {
        Intent intent = new Intent(context, WelcomeActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.welcome_signup)
    public void signUpClicked() {
        startActivity(SignUpActivity.callingIntent(this));
        finish();
    }

    @OnClick(R.id.welcome_login)
    public void loginClicked() {
        startActivity(LoginActivity.callingIntent(this));
        finish();
    }
}
