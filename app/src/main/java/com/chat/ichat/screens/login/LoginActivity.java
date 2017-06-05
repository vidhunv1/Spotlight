package com.chat.ichat.screens.login;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.chat.ichat.R;
import com.chat.ichat.UserSessionManager;
import com.chat.ichat.api.ApiManager;
import com.chat.ichat.config.AnalyticsContants;
import com.chat.ichat.db.ContactStore;
import com.chat.ichat.db.ContactsContent;
import com.chat.ichat.screens.home.HomeActivity;
import com.chat.ichat.screens.user_id.SetUserIdActivity;
import com.chat.ichat.screens.welcome.WelcomeActivity;
import com.google.firebase.analytics.FirebaseAnalytics;

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

    @Bind(R.id.id_error)
    TextView idErrorView;

    @Bind(R.id.password_error)
    TextView passwordErrorView;

    private String dividerColor = "#c9c9c9";

    final ProgressDialog[] progressDialog = new ProgressDialog[1];

    LoginPresenter loginPresenter;

    private FirebaseAnalytics firebaseAnalytics;
    private final String SCREEN_NAME = "login";

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

        loginPresenter = new LoginPresenter(ApiManager.getUserApi(), UserSessionManager.getInstance(), PreferenceManager.getDefaultSharedPreferences(this), ContactStore.getInstance(), new ContactsContent(this));
        if(UserSessionManager.getInstance().getCacheID()!=null) {
            accountET.setText(UserSessionManager.getInstance().getCacheID());
            accountDivider.setBackgroundColor(Color.parseColor(dividerColor));

            passwordET.requestFocus();
        } else {
            accountET.requestFocus();
        }
        this.firebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    @Override
    public void onBackPressed() {
        startActivity(WelcomeActivity.callingIntent(this));
        this.overridePendingTransition(0,0);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loginPresenter.attachView(this);

        /*              Analytics           */
        firebaseAnalytics.setCurrentScreen(this, SCREEN_NAME, null);
    }

    @Override
    public void navigateToHome() {
        if(progressDialog[0].isShowing()) {
            progressDialog[0].dismiss();
        }
        startActivity(HomeActivity.callingIntent(this, 0 ,null));
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
    public void showError(String title, String message) {
        if(progressDialog[0].isShowing()) {
            progressDialog[0].dismiss();
        }
        if(title.toLowerCase().contains("not found")) {
            idErrorView.setVisibility(View.VISIBLE);
            idErrorView.setText(message);
            accountDivider.setBackgroundColor(ContextCompat.getColor(this, R.color.error));
        } else if(title.toLowerCase().contains("invalid password")) {
            passwordErrorView.setVisibility(View.VISIBLE);
            passwordErrorView.setText(message);
            passwordDivider.setBackgroundColor(ContextCompat.getColor(this, R.color.error));
        }
        AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage("\n"+message);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> dialog.dismiss());
        alertDialog.show();
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

    @OnTextChanged(R.id.login_password)
    public void onPasswordTextChanged() {
        passwordDivider.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));

        passwordErrorView.setVisibility(View.INVISIBLE);
        // TODO: clear cross button
    }

    @OnTextChanged(R.id.login_account)
    public void onAccountTextChanged() {
        accountDivider.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        idErrorView.setVisibility(View.INVISIBLE);

        if(accountET.getText().length()>=1) {
            // TODO: set clear button
        } else {
            // TODO: remove clear button
        }

        idErrorView.setVisibility(View.INVISIBLE);
        // TODO: clear cross button
    }

    @OnClick(R.id.login_btn)
    public void onLoginClicked() {
        if(!isEmailValid(accountET.getText()) && !isUserIdValid(accountET.getText())) {
            AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
            alertDialog.setTitle("Invalid");
            alertDialog.setMessage("\nThis iChat ID/email is invalid.");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> dialog.dismiss());
            alertDialog.show();

            idErrorView.setVisibility(View.VISIBLE);
            idErrorView.setText("This iChat ID/email is invalid.");
            accountDivider.setBackgroundColor(ContextCompat.getColor(this, R.color.error));
            return;
        }

        progressDialog[0] = ProgressDialog.show(LoginActivity.this, "", "Loading. Please wait...", true);

        loginPresenter.loginUser(accountET.getText().toString(), passwordET.getText().toString());
        firebaseAnalytics.logEvent(AnalyticsContants.Event.LOGIN_BUTTON_CLICK, null);
    }

    @Override
    public void setInitializing() {
        if(progressDialog[0].isShowing())
            progressDialog[0].dismiss();
        progressDialog[0] = ProgressDialog.show(LoginActivity.this, "", "Initializing...", true);
        loginPresenter.fetchContacts();
    }

    private boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isUserIdValid(CharSequence userId) {
        return userId.length()>=6;
    }
}