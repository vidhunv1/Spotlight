package com.stairway.spotlight.screens.user_id;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.stairway.spotlight.R;
import com.stairway.spotlight.UserSessionManager;
import com.stairway.spotlight.api.ApiManager;
import com.stairway.spotlight.core.BaseActivity;
import com.stairway.spotlight.screens.home.HomeActivity;
import com.stairway.spotlight.screens.login.LoginActivity;
import com.stairway.spotlight.screens.sign_up.SignUpActivity;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;

/**
 * Created by vidhun on 09/03/17.
 */

public class SetUserIdActivity extends BaseActivity implements SetUserIdContract.View{
    @Bind(R.id.tb_set_user_id)
    Toolbar toolbar;

    SetUserIdPresenter presenter;

    @Bind(R.id.set_user_id_et)
    TextInputEditText userIdEt;

    @Bind(R.id.set_user_id_tilId)
    TextInputLayout userIdTIL;

    @Bind(R.id.set_user_id_divider)
    View divider;

    private Menu menu;

    final ProgressDialog[] progressDialog = new ProgressDialog[1];

    private String dividerColor = "#c9c9c9";

    public static Intent callingIntent(Context context) {
        Intent intent = new Intent(context, SetUserIdActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_user_id);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        presenter = new SetUserIdPresenter(ApiManager.getUserApi(), UserSessionManager.getInstance());
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.attachView(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.set_user_id_toolbar, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void showError(String title, String message) {
        if(progressDialog[0].isShowing()) {
            progressDialog[0].dismiss();
        }
        super.showError(title, message);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_done) {
            if(!isUserIdValid(userIdEt.getText())) {
            } else {
                progressDialog[0] = ProgressDialog.show(SetUserIdActivity.this, "", "Loading. Please wait...", true);
                presenter.setUserId(userIdEt.getText().toString());
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @OnTextChanged(R.id.set_user_id_et)
    public void onUserIdChanged() {
        MenuItem item = menu.findItem(R.id.action_done);
        if(isUserIdValid(userIdEt.getText())) {
            item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_done_active));
        } else {
            item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_done_inactive));
        }
    }

    @OnFocusChange(R.id.set_user_id_et)
    public void onPasswordChanged() {
        if(userIdEt.isFocused()) {
            divider.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        } else {
            divider.setBackgroundColor(Color.parseColor(dividerColor));
        }
    }

    @Override
    public void showUserIdNotAvailableError() {
        if(progressDialog[0].isShowing()) {
            progressDialog[0].dismiss();
        }
        super.showError("User ID", "This User ID is not available.");
    }

    @Override
    public void navigateToHome() {
        if(progressDialog[0].isShowing()) {
            progressDialog[0].dismiss();
        }
        startActivity(HomeActivity.callingIntent(this));
        finish();
    }

    private boolean isUserIdValid(CharSequence userId) {
        return userId.length()>=6;
    }
}
