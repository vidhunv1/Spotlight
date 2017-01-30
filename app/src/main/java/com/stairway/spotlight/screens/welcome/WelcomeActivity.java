package com.stairway.spotlight.screens.welcome;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.stairway.spotlight.R;
import com.stairway.spotlight.screens.register.RegisterActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class WelcomeActivity extends AppCompatActivity implements WelcomeContract.View {
    private WelcomePresenter welcomePresenter;

    public static Intent callingIntent(Context context) {
        Intent intent = new Intent(context, WelcomeActivity.class);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.welcomePresenter = new WelcomePresenter();
        setContentView(R.layout.activity_welcome);
        ButterKnife.bind(this);
        welcomePresenter.attachView(this);
    }

    @OnClick(R.id.btn_welcome_signup)
    public void signUpClicked() {
        startActivity(RegisterActivity.callingIntent(this));
    }

}
