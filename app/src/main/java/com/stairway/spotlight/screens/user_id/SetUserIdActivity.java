package com.stairway.spotlight.screens.user_id;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.widget.Button;

import com.stairway.spotlight.R;
import com.stairway.spotlight.UserSessionManager;
import com.stairway.spotlight.api.ApiManager;
import com.stairway.spotlight.core.BaseActivity;
import com.stairway.spotlight.screens.home.HomeActivity;
import com.stairway.spotlight.screens.sign_up.SignUpActivity;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
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

    @Bind(R.id.set_user_id_btn)
    Button continutBtn;

    @Bind(R.id.set_user_id_tilId)
    TextInputLayout userIdTIL;

    final ProgressDialog[] progressDialog = new ProgressDialog[1];

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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        presenter = new SetUserIdPresenter(ApiManager.getUserApi(), UserSessionManager.getInstance());
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.attachView(this);
    }

    @OnTextChanged(R.id.set_user_id_et)
    public void onUserIdChanged() {
        if(isUserIdValid(userIdEt.getText())) {
            userIdTIL.setErrorEnabled(false);
        }
    }

    @Override
    public void showUserIdNotAvailableError() {
        userIdTIL.setErrorEnabled(true);
        userIdTIL.setError("User ID is taken.");
    }

    @OnClick(R.id.set_user_id_btn)
    public void onContinueClicked() {
        if(!isUserIdValid(userIdEt.getText())) {
            userIdTIL.setErrorEnabled(true);
            userIdTIL.setError("ID should be minimum 6 characters.");
        } else {
            progressDialog[0] = ProgressDialog.show(SetUserIdActivity.this, "", "Loading. Please wait...", true);
            presenter.setUserId(userIdEt.getText().toString());
        }
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
