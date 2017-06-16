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
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.chat.ichat.UserSessionManager;
import com.chat.ichat.R;
import com.chat.ichat.api.ApiManager;
import com.chat.ichat.api.StatusResponse;
import com.chat.ichat.api.user.UserResponse;
import com.chat.ichat.config.AnalyticsContants;
import com.chat.ichat.core.Logger;
import com.chat.ichat.db.ContactStore;
import com.chat.ichat.db.ContactsContent;
import com.chat.ichat.models.ContactResult;
import com.chat.ichat.screens.home.HomeActivity;
import com.chat.ichat.screens.welcome.WelcomeActivity;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SignUpActivity extends AppCompatActivity implements SignUpContract.View {
    @Bind(R.id.tb_sign_up)
    Toolbar toolbar;

    @Bind(R.id.sign_up_name)
    EditText nameET;
    @Bind(R.id.sign_up_password)
    EditText passwordET;
    @Bind(R.id.sign_up_mobile_number)
    EditText mobileNumberET;
    @Bind(R.id.sign_up_userid)
    EditText useridET;

    @Bind(R.id.sign_up_btn)
    Button signupButton;

    @Bind(R.id.sign_up_name_divider)
    View nameDivider;
    @Bind(R.id.sign_up_password_divider)
    View passwordDivider;
    @Bind(R.id.sign_up_mobile_divider)
    View mobileDivider;
    @Bind(R.id.sign_up_userid_divider)
    View useridDivider;

    @Bind(R.id.full_name_error)
    TextView nameErrorView;
    @Bind(R.id.password_error)
    TextView passwordErrorView;
    @Bind(R.id.mobile_error)
    TextView mobileErrorView;
    @Bind(R.id.userid_error)
    TextView useridErrorView;

    @Bind(R.id.sign_up_tilName)
    TextInputLayout tilName;

    @Bind(R.id.pb_checking_userid)
    ProgressBar userIdPB;

    @Bind(R.id.iv_wrong_userid)
    ImageView wrongUserIdIV;
    @Bind(R.id.iv_wrong_password)
    ImageView wrondPasswordIV;

    @Bind(R.id.iv_clear_name)
    ImageView clearNameIV;
    @Bind(R.id.iv_clear_userid)
    ImageView clearUseridIV;
    @Bind(R.id.iv_clear_password)
    ImageView clearPasswordIV;

    @Bind(R.id.terms_privacy)
    TextView termsTV;

    private String accountEmail = "";

    final ProgressDialog[] progressDialog = new ProgressDialog[1];

    SignUpPresenter signUpPresenter;

    private String dividerColor = "#c9c9c9";

    private String countryCode;
    private String mobile;

    private String lastValidUserId = null;

    private FirebaseAnalytics firebaseAnalytics;
    private final String SCREEN_NAME = "signup";

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
                new ContactsContent(this),
                ApiManager.getPhoneContactsApi(),
                UserSessionManager.getInstance(),
                PreferenceManager.getDefaultSharedPreferences(this));

        TelephonyManager manager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        //getNetworkCountryIso
        String CountryID= manager.getSimCountryIso().toUpperCase();
        String[] rl=this.getResources().getStringArray(R.array.CountryCodes);
        for(int i=0;i<rl.length;i++) {
            String[] g=rl[i].split(",");
            if(g[1].trim().equals(CountryID.trim())) {
                this.countryCode = "+"+g[0];
                break;
            }
        }
        String text2 = "By tapping \"Sign Up\" you agree to iChat's terms and privacy policy";

        Spannable spannable = new SpannableString(text2);

        spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.colorPrimary)), 41, 47, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.colorPrimary)), 51, 66, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        termsTV.setText(spannable, TextView.BufferType.SPANNABLE);
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

        /*              Analytics           */
        firebaseAnalytics.setCurrentScreen(this, SCREEN_NAME, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onUserRegistered() {
        if(hasReadContactPermission()) {
            if(progressDialog[0]!=null && progressDialog[0].isShowing()) {
                progressDialog[0].dismiss();
                progressDialog[0] = ProgressDialog.show(SignUpActivity.this, "", "Initializing...", true);
            }
            signUpPresenter.initialize();
        } else {
            navigateToHome();
        }
    }

    @Override
    public void showUserIdAvailable(String userId, boolean isAvailable) {
        Logger.d(this, "UserIdAvailable: "+userId+", "+isAvailable);
        if(useridET.getText().toString().equals(userId)) {
            userIdPB.setVisibility(View.GONE);
            if (isAvailable) {
                lastValidUserId = userId;
                useridErrorView.setVisibility(View.INVISIBLE);
                wrongUserIdIV.setVisibility(View.VISIBLE);
                wrongUserIdIV.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_correct));
            } else {
                useridErrorView.setVisibility(View.VISIBLE);
                wrongUserIdIV.setVisibility(View.VISIBLE);
                wrongUserIdIV.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_wrong));
                useridErrorView.setText("User ID taken");
            }
        }
    }

    @Override
    public void navigateToHome() {
        Context context = this;

        ApiManager.getAppApi().appInit()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<StatusResponse>() {
                    @Override
                    public void onCompleted() {}

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
                                                    public void onCompleted() {}

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
    public void showError(String title, String message) {
        if(progressDialog[0]!=null && progressDialog[0].isShowing()) {
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
        if(passwordET.getText().length()>0) {
            clearPasswordIV.setVisibility(View.VISIBLE);
        } else {
            clearPasswordIV.setVisibility(View.GONE);
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
        if(nameET.getText().toString().length()>0) {
            clearNameIV.setVisibility(View.VISIBLE);
        } else {
            clearNameIV.setVisibility(View.GONE);
        }
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

    @OnTextChanged(R.id.sign_up_userid)
    public void onUserIdTextChanged() {
        CharSequence userid = useridET.getText();
        useridDivider.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        useridErrorView.setVisibility(View.INVISIBLE);
        wrongUserIdIV.setVisibility(View.GONE);
        userIdPB.setVisibility(View.GONE);
        if(userid.length()>0) {
            clearUseridIV.setVisibility(View.VISIBLE);
            if (isUserIdValid(userid)) {
                userIdPB.setVisibility(View.VISIBLE);
                signUpPresenter.checkUserIdAvailable(useridET.getText().toString());
            } else if (userid.length() < 6) {
                wrongUserIdIV.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_wrong));
                useridErrorView.setVisibility(View.VISIBLE);
                wrongUserIdIV.setVisibility(View.VISIBLE);
                useridErrorView.setText("User ID must have atleast 6 characters.");
            } else {
                wrongUserIdIV.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_wrong));
                useridErrorView.setVisibility(View.VISIBLE);
                wrongUserIdIV.setVisibility(View.VISIBLE);
                useridErrorView.setText("This User ID is invalid.");
            }
        } else {
            clearUseridIV.setVisibility(View.GONE);
        }
    }

    @OnFocusChange(R.id.sign_up_userid)
    public void onUseridFocusChanged() {
        if(useridET.isFocused()) {
            useridDivider.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        } else {
            useridDivider.setBackgroundColor(Color.parseColor(dividerColor));
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

        if (!hasReadContactPermission()) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.GET_ACCOUNTS, Manifest.permission.READ_CONTACTS}, 101);
        } else {
            registerUser();
        }
        firebaseAnalytics.logEvent(AnalyticsContants.Event.SIGNUP_BUTTON_CLICK, null);
    }

    @OnClick(R.id.iv_clear_name)
    public void onClearNameClicked() {
        nameET.setText("");
    }

    @OnClick(R.id.iv_clear_password)
    public void onClearPasswordClicked() {
        passwordET.setText("");
    }

    @OnClick(R.id.iv_clear_userid)
    public void onClearUseridClicked() {
        useridET.setText("");
    }

    private void registerUser() {
        String imei = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        this.mobile = mobileNumberET.getText().toString();
        if(lastValidUserId!=null && lastValidUserId.equals(useridET.getText().toString())) {
            signUpPresenter.registerUser(nameET.getText().toString(), accountEmail, passwordET.getText().toString(), this.countryCode, this.mobile, lastValidUserId, imei);
        } else {
            showError("Invalid UserId", "Please enter a valid User ID.");
        }
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

    public boolean hasReadContactPermission() {
        int permission = PackageManager.PERMISSION_GRANTED;
        int result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS);
        int result2 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        return Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1 || (result1 == permission && result2 == permission);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //granted
                    accountEmail = getEmailAddress();
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

    private boolean isUserIdValid(CharSequence userId) {
        return userId.length()>=6 && !hasIllegalChars(userId);
    }

    private boolean hasIllegalChars(CharSequence userId) {
        Pattern pattern = Pattern.compile("^[A-Z0-9a-z_]+$");
        Matcher matcher = pattern.matcher(userId);
        return !matcher.find();
    }
}
