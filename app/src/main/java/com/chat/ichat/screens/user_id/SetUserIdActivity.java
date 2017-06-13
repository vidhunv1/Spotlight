package com.chat.ichat.screens.user_id;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.chat.ichat.R;
import com.chat.ichat.UserSessionManager;
import com.chat.ichat.api.ApiManager;
import com.chat.ichat.api.StatusResponse;
import com.chat.ichat.api.user.UserResponse;
import com.chat.ichat.config.AnalyticsContants;
import com.chat.ichat.core.BaseActivity;
import com.chat.ichat.core.Logger;
import com.chat.ichat.db.ContactStore;
import com.chat.ichat.db.ContactsContent;
import com.chat.ichat.models.ContactResult;
import com.chat.ichat.screens.home.HomeActivity;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnTextChanged;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

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

    final ProgressDialog[] progressDialog = new ProgressDialog[1];

    private FirebaseAnalytics firebaseAnalytics;
    private final String SCREEN_NAME = "set_userid";

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
                UserSessionManager.getInstance(),
                ApiManager.getPhoneContactsApi(),
                new ContactsContent(this),
                new ContactStore());

        ApiManager.getUserApi().findUserByUserId("teamichat")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<UserResponse>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(UserResponse userResponse) {
                        ContactResult contactResult1 = new ContactResult();
                        contactResult1.setUserId(userResponse.getUser().getUserId());
                        contactResult1.setContactName(userResponse.getUser().getName());
                        contactResult1.setUsername(userResponse.getUser().getUsername());
                        contactResult1.setAdded(true);
                        contactResult1.setBlocked(false);
                        contactResult1.setUserType(userResponse.getUser().getUserType());
                        contactResult1.setProfileDP(userResponse.getUser().getProfileDP());

                        ContactStore.getInstance().storeContact(contactResult1)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Subscriber<Boolean>() {
                                    @Override
                                    public void onCompleted() {}

                                    @Override
                                    public void onError(Throwable e) {
                                        e.printStackTrace();
                                    }

                                    @Override
                                    public void onNext(Boolean aBoolean) {
                                        Logger.d(this, "Added: "+contactResult1.toString());
                                    }
                                });
                    }
                });
        this.firebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.attachView(this);

        /*              Analytics           */
        firebaseAnalytics.setCurrentScreen(this, SCREEN_NAME, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(progressDialog[0]!=null) {
            progressDialog[0].dismiss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
            firebaseAnalytics.logEvent(AnalyticsContants.Event.SET_USERID_BUTTON_CLICK, null);
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
        } else if(hasIllegalChars(userIdEt.getText())){
            userIdErrorView.setText("Sorry, this user ID is invalid.");
            userIdErrorView.setTextColor(ContextCompat.getColor(this, R.color.error));
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
        Logger.d(this, "Navigate to home");

        Context context = this;

        ApiManager.getAppApi().appInit()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<StatusResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        startActivity(HomeActivity.callingIntent(context,0,null));
                        finish();
                    }

                    @Override
                    public void onNext(StatusResponse statusResponse) {
                        ApiManager.getUserApi().findUserByUserId("teamichat")
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Subscriber<UserResponse>() {
                                    @Override
                                    public void onCompleted() {}

                                    @Override
                                    public void onError(Throwable e) {
                                        startActivity(HomeActivity.callingIntent(context,0,null));
                                        finish();
                                    }

                                    @Override
                                    public void onNext(UserResponse userResponse) {
                                        ContactResult contactResult1 = new ContactResult();
                                        contactResult1.setUserId(userResponse.getUser().getUserId());
                                        contactResult1.setContactName(userResponse.getUser().getName());
                                        contactResult1.setUsername(userResponse.getUser().getUsername());
                                        contactResult1.setAdded(true);
                                        contactResult1.setBlocked(false);
                                        contactResult1.setUserType(userResponse.getUser().getUserType());
                                        contactResult1.setProfileDP(userResponse.getUser().getProfileDP());

                                        ContactStore.getInstance().storeContact(contactResult1)
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(new Subscriber<Boolean>() {
                                                    @Override
                                                    public void onCompleted() {

                                                    }

                                                    @Override
                                                    public void onError(Throwable e) {
                                                        startActivity(HomeActivity.callingIntent(context,0,null));
                                                        finish();
                                                    }

                                                    @Override
                                                    public void onNext(Boolean aBoolean) {
                                                        Logger.d(this, "Added: "+contactResult1.toString());
                                                        startActivity(HomeActivity.callingIntent(context,0,null));
                                                        finish();
                                                    }
                                                });
                                    }
                                });
                    }
                });
    }

    @Override
    public void onSetUserIdSuccess() {
        progressDialog[0].setMessage("Initializing...");
        presenter.initialize();
    }

    private boolean isUserIdValid(CharSequence userId) {
        return userId.length()>=6 && !hasIllegalChars(userId);
    }

    private boolean hasIllegalChars(CharSequence userId) {
        Pattern pattern = Pattern.compile("^[A-Z0-9a-z_]+$");
        Matcher matcher = pattern.matcher(userId);
        return !matcher.find();
    }
}
