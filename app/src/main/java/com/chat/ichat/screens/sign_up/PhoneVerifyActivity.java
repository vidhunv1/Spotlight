package com.chat.ichat.screens.sign_up;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;
import android.widget.TextView;

import com.chat.ichat.R;
import com.chat.ichat.UserSessionManager;
import com.chat.ichat.api.ApiManager;
import com.chat.ichat.api.user.UserResponse;
import com.chat.ichat.api.user.VerifyRequest;
import com.chat.ichat.application.SpotlightApplication;
import com.chat.ichat.config.AnalyticsConstants;
import com.chat.ichat.core.Logger;
import com.chat.ichat.db.core.DatabaseManager;
import com.chat.ichat.models.UserSession;
import com.chat.ichat.screens.home.HomeActivity;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.chat.ichat.core.FCMRegistrationIntentService.FCM_TOKEN;

/**
 * Created by vidhun on 04/07/17.
 */

public class PhoneVerifyActivity extends AppCompatActivity {
    @Bind(R.id.tb)
    Toolbar toolbar;
    @Bind(R.id.verify_code)
    EditText code;
    @Bind(R.id.country_selector)
    TextView text;
    private String countryCode, phone, verificationUUID;
    final ProgressDialog[] progressDialog = new ProgressDialog[1];
    private FirebaseAnalytics firebaseAnalytics;

    public static Intent callingIntent(Context context, String countryCode, String phone, String verificationUUID) {
        Intent intent = new Intent(context, PhoneVerifyActivity.class);
        intent.putExtra("PhoneVerifyActivity.COUNTRYCODE", countryCode);
        intent.putExtra("PhoneVerifyActivity.PHONE", phone);
        intent.putExtra("PhoneVerifyActivity.VERIFICATION_UUID", verificationUUID);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.d(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_verify);
        ButterKnife.bind(this);
        this.firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        firebaseAnalytics.setCurrentScreen(this, AnalyticsConstants.Event.SIGNUP_SCREEN, null);
        code.requestFocus();
        text.setText("We've send an SMS with an activation code to your phone "+countryCode+" "+phone+".");
        Intent receivedIntent = getIntent();
        if(!receivedIntent.hasExtra("PhoneVerifyActivity.PHONE"))
            return;

        countryCode = receivedIntent.getStringExtra("PhoneVerifyActivity.COUNTRYCODE");
        phone = receivedIntent.getStringExtra("PhoneVerifyActivity.PHONE");
        verificationUUID = receivedIntent.getStringExtra("PhoneVerifyActivity.VERIFICATION_UUID");

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
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
        firebaseAnalytics.logEvent(AnalyticsConstants.Event.PHONE_VERIFY_DONE, null);

        PhoneVerifyActivity phoneVerifyActivity = PhoneVerifyActivity.this;
        progressDialog[0] = ProgressDialog.show(phoneVerifyActivity, "", "Verifying. Please wait...", true);
        final String password = UUID.randomUUID().toString().replaceAll("-", "");
        SharedPreferences defaultSP = PreferenceManager.getDefaultSharedPreferences(this);
        final String notificationToken = defaultSP.getString(FCM_TOKEN, "");
        VerifyRequest verifyRequest = new VerifyRequest(countryCode, phone, code.getText().toString(), verificationUUID, password, notificationToken);
        ApiManager.getUserApi().verifyUser(verifyRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<UserResponse>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {}

                    @Override
                    public void onNext(UserResponse userResponse) {
//                        signUpView.navigateToSetUserID();
                        if(!userResponse.isSuccess()) {
                            if(userResponse.getError().getCode() == 401) {
                                //incorrect otp
                                firebaseAnalytics.logEvent(AnalyticsConstants.Event.PHONE_VERIFY_ERROR_OTP, null);
                                phoneVerifyActivity.showError("Wrong OTP", "The OTP is incorrect.");
                            }
                        } else {
                            UserSession ss = new UserSession();
                            ss.setExpires(userResponse.getExpires());
                            ss.setAccessToken(userResponse.getAccessToken());
                            ss.setPassword(password);
                            ss.setUserName(userResponse.getUser().getUsername());
                            ss.setName(userResponse.getUser().getName());
                            ss.setMobile(userResponse.getUser().getPhone());
                            ss.setCountryCode(userResponse.getUser().getCountryCode());
                            UserSessionManager.getInstance().save(ss);
                            ApiManager.getInstance().setAuthorization(ss.getAccessToken());
                            SpotlightApplication.getContext().initSession();
                            DatabaseManager.getSQLiteHelper().clearData(DatabaseManager.getInstance().openConnection());
                            firebaseAnalytics.logEvent(AnalyticsConstants.Event.PHONE_VERIFY_SUCCESS, null);
                            startActivity(HomeActivity.callingIntent(phoneVerifyActivity,0,null));
                            finish();
                        }
                    }
                });
    }
}
