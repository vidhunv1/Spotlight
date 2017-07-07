package com.chat.ichat.screens.sign_up;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;

import com.chat.ichat.R;
import com.chat.ichat.UserSessionManager;
import com.chat.ichat.api.ApiError;
import com.chat.ichat.api.ApiManager;
import com.chat.ichat.api.user.UserRequest;
import com.chat.ichat.api.user.UserResponse;
import com.chat.ichat.api.user._User;
import com.chat.ichat.core.Logger;
import com.chat.ichat.models.UserSession;
import com.chat.ichat.screens.home.HomeActivity;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by vidhun on 04/07/17.
 */

public class SetNameActivity extends AppCompatActivity {
    @Bind(R.id.tb)
    Toolbar toolbar;
    @Bind(R.id.first_name)
    EditText firstName;
    @Bind(R.id.last_name)
    EditText lastName;
    final ProgressDialog[] progressDialog = new ProgressDialog[1];
    public static Intent callingIntent(Context context) {
        Intent intent = new Intent(context, SetNameActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.d(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_name);
        ButterKnife.bind(this);
        firstName.requestFocus();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        UserRequest userRequest = new UserRequest();
        _User user = new _User();
        user.setName("AAA");
    }

    public void showError(String title, String message) {
        if(progressDialog[0]!=null && progressDialog[0].isShowing())
            progressDialog[0].dismiss();
        android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage("\n"+message);
        alertDialog.setButton(android.app.AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }

    @OnClick(R.id.iv_done)
    public void onDone() {
        String fName = firstName.getText().toString();
        String lName = lastName.getText().toString();
        if(fName.isEmpty()) {
            showError(getResources().getString(R.string.app_name), "Name cant be blank");
        } else {
            SetNameActivity setNameActivity = SetNameActivity.this;
            _User user = new _User();
            user.setName(fName+" "+lName);
            UserRequest userRequest = new UserRequest();
            userRequest.setUser(user);
            progressDialog[0] = ProgressDialog.show(SetNameActivity.this, "" , "Setting Name. Please wait...", true);
            ApiManager.getUserApi().updateUser(userRequest)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<UserResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        ApiError error = new ApiError(e);
                        setNameActivity.showError(error.getTitle(), error.getMessage());
                    }

                    @Override
                    public void onNext(UserResponse userResponse) {
                        UserSession ss = new UserSession();
                        ss.setName(fName+" "+lName);
                        UserSessionManager.getInstance().save(ss);

                        startActivity(HomeActivity.callingIntent(setNameActivity,0,null));
                    }
                });
        }
    }
}
