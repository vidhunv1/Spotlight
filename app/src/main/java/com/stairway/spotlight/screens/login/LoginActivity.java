package com.stairway.spotlight.screens.login;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.stairway.spotlight.R;
import com.stairway.spotlight.UserSessionManager;
import com.stairway.spotlight.api.ApiManager;
import com.stairway.spotlight.screens.home.HomeActivity;
import com.stairway.spotlight.screens.user_id.SetUserIdActivity;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;

/**
 * Created by vidhun on 12/03/17.
 */

public class LoginActivity extends AppCompatActivity implements LoginContract.View {
    @Bind(R.id.tb_login)
    Toolbar toolbar;

    @Bind(R.id.login_account)
    EditText accountET;

    @Bind(R.id.login_password)
    EditText passwordET;

    @Bind(R.id.login_tilAccount)
    TextInputLayout accountTIL;

    @Bind(R.id.login_tilPassword)
    TextInputLayout passwordTIL;

    @Bind(R.id.login_btn)
    Button loginButton;

    @Bind(R.id.login_account_divider)
    View accountDivider;

    @Bind(R.id.login_password_divider)
    View passwordDivider;

    private String dividerColor = "#c9c9c9";

    final ProgressDialog[] progressDialog = new ProgressDialog[1];

    LoginPresenter loginPresenter;

    public static Intent callingIntent(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        loginPresenter = new LoginPresenter(ApiManager.getUserApi(), UserSessionManager.getInstance());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loginPresenter.attachView(this);
    }

    @Override
    public void navigateToHome() {
        if(progressDialog[0].isShowing()) {
            progressDialog[0].dismiss();
        }
        startActivity(HomeActivity.callingIntent(this));
        finish();
    }

    @Override
    public void navigateToSetUserId() {
        if(progressDialog[0].isShowing()) {
            progressDialog[0].dismiss();
        }
        startActivity(SetUserIdActivity.callingIntent(this));
        finish();
    }

    @Override
    public void showInvalidPasswordError() {
        if(progressDialog[0].isShowing()) {
            progressDialog[0].dismiss();
        }
        AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
        alertDialog.setTitle("Invalid Password");
        alertDialog.setMessage("\nThe password you entered is invalid.");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }

    @OnTextChanged(R.id.login_account)
    public void onAccountTextChanged() {
        changeLoginButton();
    }

    @OnTextChanged(R.id.login_password)
    public void onPasswordTextChanged() {
        changeLoginButton();
    }

    @OnFocusChange(R.id.login_account)
    public void onLoginChanged() {
        if(accountET.isFocused()) {
            accountDivider.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        } else {
            accountDivider.setBackgroundColor(Color.parseColor(dividerColor));
        }
    }

    @OnFocusChange(R.id.login_password)
    public void onPasswordChanged() {
        if(passwordET.isFocused()) {
            passwordDivider.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        } else {
            passwordDivider.setBackgroundColor(Color.parseColor(dividerColor));
        }
    }

    @OnClick(R.id.login_btn)
    public void onLoginClicked() {
        if(!isEmailValid(accountET.getText()) && !isUserIdValid(accountET.getText())) {
            AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
            alertDialog.setTitle("Invalid");
            alertDialog.setMessage("\nThis iChat ID/email is invalid.");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> dialog.dismiss());
            alertDialog.show();
            return;
        }

        progressDialog[0] = ProgressDialog.show(LoginActivity.this, "", "Loading. Please wait...", true);

        loginPresenter.loginUser(accountET.getText().toString(), passwordET.getText().toString());
    }

    public void changeLoginButton() {
        if(accountET.getText().length()>=1 && passwordET.getText().length()>=1) {
            loginButton.setAlpha(1f);
        } else {
            loginButton.setAlpha(0.6f);
        }
    }

    private boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isUserIdValid(CharSequence userId) {
        return userId.length()>=6;
    }
}
