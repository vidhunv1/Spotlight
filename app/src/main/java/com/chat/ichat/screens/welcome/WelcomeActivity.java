package com.chat.ichat.screens.welcome;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import com.chat.ichat.R;

import com.chat.ichat.screens.login.LoginActivity;
import com.chat.ichat.screens.sign_up.SignUpActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;


public class WelcomeActivity extends AppCompatActivity implements WelcomeContract.View {
    public static Intent callingIntent(Context context) {
        Intent intent = new Intent(context, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        ButterKnife.bind(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(0xffcfd0d4);
        }
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

    @Override
    public void showError(String title, String message) {}
}
