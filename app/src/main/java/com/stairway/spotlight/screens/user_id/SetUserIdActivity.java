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
import android.widget.TextView;

import com.stairway.spotlight.R;
import com.stairway.spotlight.UserSessionManager;
import com.stairway.spotlight.api.ApiManager;
import com.stairway.spotlight.core.BaseActivity;
import com.stairway.spotlight.core.Logger;
import com.stairway.spotlight.db.ContactStore;
import com.stairway.spotlight.db.ContactsContent;
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

    @Bind(R.id.user_id_error)
    TextView userIdErrorView;

    private Menu menu;

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
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        presenter = new SetUserIdPresenter(ApiManager.getUserApi(),
                ApiManager.getAppApi(),
                UserSessionManager.getInstance(),
                ApiManager.getContactsApi(),
                new ContactsContent(this),
                new ContactStore());
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
    protected void onStop() {
        super.onStop();
        presenter.detachView();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void showError(String title, String message) {
        if(progressDialog[0]!=null && progressDialog[0].isShowing()) {
            progressDialog[0].dismiss();
        }
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage("\n"+message);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> dialog.dismiss());
        alertDialog.show();

        userIdErrorView.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_done) {
            if(!isUserIdValid(userIdEt.getText())) {
                showError("User ID", "User ID must have atleast 6 characters.");
            } else {
                progressDialog[0] = ProgressDialog.show(SetUserIdActivity.this, "", "Setting your ID. Please wait...", true);
                presenter.setUserId(userIdEt.getText().toString());
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @OnTextChanged(R.id.set_user_id_et)
    public void onUserIdChanged() {
        userIdErrorView.setVisibility(View.VISIBLE);
        if(isUserIdValid(userIdEt.getText())) {
            userIdErrorView.setText("Checking user ID...");
            presenter.checkUserIdAvailable(userIdEt.getText().toString());
            userIdErrorView.setTextColor(0xff6D726D);
        } else {
            userIdErrorView.setText("User ID must have atleast 6 characters.");
            userIdErrorView.setTextColor(ContextCompat.getColor(this, R.color.error));
        }
    }

    @Override
    public void showUserIdNotAvailableError() {
        userIdErrorView.setText("Sorry, this user ID is already taken.");
        userIdErrorView.setTextColor(ContextCompat.getColor(this, R.color.error));
    }

    @Override
    public void showUserIdAvailable() {
        userIdErrorView.setText(userIdEt.getText()+" is available.");
        userIdErrorView.setTextColor(ContextCompat.getColor(this, R.color.success));
    }

    @Override
    public void navigateToHome() {
        if(progressDialog[0].isShowing()) {
            progressDialog[0].dismiss();
        }
        Logger.d(this, "Navigate to home");
        startActivity(HomeActivity.callingIntent(this,0,null));
        finish();
    }

    @Override
    public void onSetUserIdSuccess() {
        progressDialog[0].setMessage("Initializing...");
        presenter.initialize();
    }

    private boolean isUserIdValid(CharSequence userId) {
        return userId.length()>=6;
    }
}
