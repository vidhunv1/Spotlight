package com.chat.ichat.screens.sign_up;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.chat.ichat.UserSessionManager;
import com.chat.ichat.R;
import com.chat.ichat.api.ApiManager;
import com.chat.ichat.config.AnalyticsContants;
import com.chat.ichat.core.Logger;
import com.chat.ichat.screens.new_chat.NewChatActivity;
import com.chat.ichat.screens.user_id.SetUserIdActivity;
import com.chat.ichat.screens.welcome.WelcomeActivity;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import swarajsaaj.smscodereader.interfaces.OTPListener;
import swarajsaaj.smscodereader.receivers.OtpReader;

public class SignUpActivity extends AppCompatActivity implements SignUpContract.View, OTPListener {
    @Bind(R.id.tb_sign_up)
    Toolbar toolbar;

    @Bind(R.id.sign_up_name)
    EditText nameET;

    @Bind(R.id.sign_up_password)
    EditText passwordET;

    @Bind(R.id.sign_up_mobile_number)
    EditText mobileNumberET;

    @Bind(R.id.sign_up_tilMobile)
    TextInputLayout mobileTIL;

    @Bind(R.id.sign_up_tilName)
    TextInputLayout nameTIL;

    @Bind(R.id.sign_up_tilPassword)
    TextInputLayout passwordTIL;

    @Bind(R.id.sign_up_btn)
    Button signupButton;

    @Bind(R.id.sign_up_name_divider)
    View nameDivider;

    @Bind(R.id.sign_up_password_divider)
    View passwordDivider;

    @Bind(R.id.sign_up_mobile_divider)
    View mobileDivider;

    @Bind(R.id.full_name_error)
    TextView nameErrorView;

    @Bind(R.id.password_error)
    TextView passwordErrorView;

    @Bind(R.id.mobile_error)
    TextView mobileErrorView;

    @Bind(R.id.sign_up_country_code)
    EditText countryCodeView;

    private String accountEmail = "";

    final ProgressDialog[] progressDialog = new ProgressDialog[1];

    SignUpPresenter signUpPresenter;

    private String dividerColor = "#c9c9c9";

    private String countryCode;
    private String mobile;
    private String verificationUUID;

    private FirebaseAnalytics firebaseAnalytics;
    private final String SCREEN_NAME = "signup";

    private boolean isInFront = false;

    public static Intent callingIntent(Context context) {
        Intent intent = new Intent(context, SignUpActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.d(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        signUpPresenter = new SignUpPresenter(ApiManager.getUserApi(),
                UserSessionManager.getInstance(),
                PreferenceManager.getDefaultSharedPreferences(this));

        // Permissions
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            int permission = PackageManager.PERMISSION_GRANTED;
            int result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS);
            int result2 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);

            if(!(result1==permission && result2 == permission)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.GET_ACCOUNTS, Manifest.permission.READ_CONTACTS}, 101);
            }

        }

        TelephonyManager manager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        //getNetworkCountryIso
        String CountryID= manager.getSimCountryIso().toUpperCase();
        String[] rl=this.getResources().getStringArray(R.array.CountryCodes);
        for(int i=0;i<rl.length;i++){
            String[] g=rl[i].split(",");
            if(g[1].trim().equals(CountryID.trim())){
                countryCodeView.setText("+"+g[0]);
                break;
            }
        }
        OtpReader.bind(this,"AUT");
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
        signUpPresenter.attachView(this);
        isInFront = true;

        /*              Analytics           */
        firebaseAnalytics.setCurrentScreen(this, SCREEN_NAME, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isInFront = false;
    }

    @Override
    public void navigateToSetUserID() {
        if(progressDialog[0].isShowing()) {
            progressDialog[0].dismiss();
        }
        if(isInFront) {
            startActivity(SetUserIdActivity.callingIntent(this));
            finish();
        }
    }

    @Override
    public void showVerifyingOtp(String verificationUUID) {
        this.verificationUUID = verificationUUID;
        if(progressDialog[0].isShowing()) {
            progressDialog[0].dismiss();
        }
        if(hasReadSMSPermission()) {
            progressDialog[0] = ProgressDialog.show(SignUpActivity.this, "", "Waiting for OTP. Please wait...", true);
        } else {
            navigateToSetUserID();
        }

        new Handler().postDelayed(this::navigateToSetUserID, 60000);
    }

    @Override
    public void showError(String title, String message) {
        if(progressDialog[0].isShowing()) {
            progressDialog[0].dismiss();
        }
        Logger.d(this, "Error: "+title+", "+message);
        AlertDialog alertDialog = new AlertDialog.Builder(SignUpActivity.this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage("\n"+message);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }

    @OnTextChanged(R.id.sign_up_password)
    public void onPasswordTextChanged() {

        if(isPasswordValid(passwordET.getText())) {
            passwordDivider.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
            passwordErrorView.setVisibility(View.INVISIBLE);
        }
    }

    @OnFocusChange(R.id.sign_up_password)
    public void onPasswordFocusChanged() {
        if(passwordET.isFocused()) {
            passwordDivider.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        } else {
            passwordDivider.setBackgroundColor(Color.parseColor(dividerColor));
        }
    }

    @OnTextChanged(R.id.sign_up_name)
    public void onNameTextChanged() {
        nameDivider.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        nameErrorView.setVisibility(View.INVISIBLE);
    }

    @OnFocusChange(R.id.sign_up_name)
    public void onNameFocusChanged() {
        if(nameET.isFocused()) {
            nameDivider.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        } else {
            nameDivider.setBackgroundColor(Color.parseColor(dividerColor));
        }
    }

    @OnTextChanged(R.id.sign_up_mobile_number)
    public void onMobileTextChanged() {
        mobileDivider.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        mobileErrorView.setVisibility(View.INVISIBLE);
    }

    @OnFocusChange(R.id.sign_up_mobile_number)
    public void onMobileFocusChanged() {
        if(mobileNumberET.isFocused()) {
            mobileDivider.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        } else {
            mobileDivider.setBackgroundColor(Color.parseColor(dividerColor));
        }
    }

    @OnClick(R.id.sign_up_btn)
    public void onSignUpClicked() {
        if(!isNameValid(nameET.getText())) {
            AlertDialog alertDialog = new AlertDialog.Builder(SignUpActivity.this).create();
            alertDialog.setTitle("Invalid");
            alertDialog.setMessage("\nPlease enter your full name");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> dialog.dismiss());
            alertDialog.show();

            nameET.requestFocus();

            nameErrorView.setVisibility(View.VISIBLE);
            nameErrorView.setText("Please enter your full name");
            nameDivider.setBackgroundColor(ContextCompat.getColor(this, R.color.error));
            return;
        }
        if(!isPasswordValid(passwordET.getText())) {
            AlertDialog alertDialog = new AlertDialog.Builder(SignUpActivity.this).create();
            alertDialog.setTitle("Sign-up Failed");
            alertDialog.setMessage("\nPassword must be 6 to 16 characters long.");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> dialog.dismiss());
            alertDialog.show();

            passwordET.requestFocus();

            passwordErrorView.setVisibility(View.VISIBLE);
            passwordErrorView.setText("6-16 characters long");
            passwordDivider.setBackgroundColor(ContextCompat.getColor(this, R.color.error));
            return;
        }
        if(!isMobileNumberValid(mobileNumberET.getText())) {
            AlertDialog alertDialog = new AlertDialog.Builder(SignUpActivity.this).create();
            alertDialog.setTitle("Invalid Phone Number");
            alertDialog.setMessage("\nThis phone number is invalid.");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> dialog.dismiss());
            alertDialog.show();

            mobileNumberET.requestFocus();

            mobileErrorView.setVisibility(View.VISIBLE);
            mobileErrorView.setText("Please enter a valid phone number");
            mobileDivider.setBackgroundColor(ContextCompat.getColor(this, R.color.error));
            return;
        }

        if (!hasReadSMSPermission()) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS}, 102);
        } else {
            registerUser();
        }
        firebaseAnalytics.logEvent(AnalyticsContants.Event.SIGNUP_BUTTON_CLICK, null);
    }

    private void registerUser() {
        String imei = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        this.mobile = mobileNumberET.getText().toString();
        this.countryCode = countryCodeView.getText().toString();
        signUpPresenter.registerUser(nameET.getText().toString(), accountEmail, passwordET.getText().toString(), this.countryCode, this.mobile, imei);
        progressDialog[0] = ProgressDialog.show(SignUpActivity.this, "", "Loading. Please wait...", true);
    }

    private boolean isPasswordValid(CharSequence pass) {
        return pass.length()>=6 && pass.length()<=16;
    }

    private boolean isMobileNumberValid(CharSequence mobile) {
        return mobile.length()>=10;
    }

    private boolean isNameValid(CharSequence name) {
        return name.length()>=3;
    }

    private String getEmailAddress() {
        Pattern emailPattern = Patterns.EMAIL_ADDRESS;
        Account[] accounts = AccountManager.get(this).getAccounts();
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                return account.name;
            }
        }
        return "";
    }

    public boolean hasReadSMSPermission() {
        int permission = PackageManager.PERMISSION_GRANTED;
        int result3 = ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS);
        int result4 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1)
            return (result3 == permission && result4 == permission);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //granted
                    accountEmail = getEmailAddress();
                } else {
                    //not granted
                }
                break;
            case 102:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //granted
                    registerUser();
                } else {
                    //not granted
                    registerUser();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void otpReceived(String messageText) {
        Logger.d(this, "OTP MESSAGE: "+messageText);

        Pattern pattern = Pattern.compile("(\\d{4,6})");

        Matcher matcher = pattern.matcher(messageText);
        String val = "";
        if (matcher.find()) {
            val = matcher.group(1);

            if(progressDialog[0].isShowing()) {
                progressDialog[0].dismiss();
            }
            progressDialog[0] = ProgressDialog.show(SignUpActivity.this, "", "Verifying OTP...", true);
            signUpPresenter.verifyOTP(countryCode, mobile, val, verificationUUID);
        }
    }
}
