package com.chat.ichat.screens;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;

import com.chat.ichat.R;
import com.chat.ichat.api.ApiManager;
import com.chat.ichat.api.StatusResponse;
import com.chat.ichat.api.user.VerifyRequest;
import com.chat.ichat.screens.user_id.SetUserIdActivity;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by vidhun on 15/06/17.
 */
public class VerifyOtpActivity extends AppCompatActivity {
    private static String KEY_PHONE = "phone";
    private static String KEY_COUNTRY_CODE = "country_code";
    private static String KEY_VERIFICATION_UUID = "verification_uuid";
    @Bind(R.id.btn_verify)
    Button verify;
    @Bind(R.id.et_otp)
    EditText otp;

    private String phone, countryCode, verificationUUID;

    public static Intent callingIntent(Context context, String phone, String countryCode, String verificationUUID) {
        Intent intent = new Intent(context, VerifyOtpActivity.class);
        intent.putExtra(KEY_PHONE, phone);
        intent.putExtra(KEY_COUNTRY_CODE, countryCode);
        intent.putExtra(KEY_VERIFICATION_UUID, verificationUUID);
        return intent;
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);
        ButterKnife.bind(this);

        Intent receivedIntent = getIntent();
        countryCode = receivedIntent.getStringExtra(KEY_COUNTRY_CODE);
        phone = receivedIntent.getStringExtra(KEY_PHONE);
        verificationUUID = receivedIntent.getStringExtra(KEY_VERIFICATION_UUID);
    }

    @OnClick(R.id.btn_verify)
    public void onVerifyClicked() {
        Context context = this;
        VerifyRequest verifyRequest = new VerifyRequest(countryCode, phone, otp.getText().toString(), verificationUUID);
        ApiManager.getUserApi().verifyUser(verifyRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<StatusResponse>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {}

                    @Override
                    public void onNext(StatusResponse statusResponse) {
                        startActivity(SetUserIdActivity.callingIntent(context));
                        finish();
                    }
                });
    }
}
