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
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.chat.ichat.UserSessionManager;
import com.chat.ichat.R;
import com.chat.ichat.api.ApiManager;
import com.chat.ichat.core.Logger;
import com.chat.ichat.screens.user_id.SetUserIdActivity;
import com.chat.ichat.screens.welcome.WelcomeActivity;

import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;

public class SignUpActivity extends AppCompatActivity implements SignUpContract.View{
    @Bind(R.id.tb_sign_up)
    Toolbar toolbar;

    @Bind(R.id.sign_up_name)
    EditText nameET;

    @Bind(R.id.sign_up_password)
    EditText passwordET;

    @Bind(R.id.sign_up_email)
    EditText emailET;

    @Bind(R.id.sign_up_mobile_number)
    EditText mobileNumberET;

    @Bind(R.id.sign_up_tilEmail)
    TextInputLayout emailTIL;

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

    @Bind(R.id.sign_up_email_divider)
    View emailDivider;

    @Bind(R.id.sign_up_password_divider)
    View passwordDivider;

    @Bind(R.id.sign_up_mobile_divider)
    View mobileDivider;

    @Bind(R.id.full_name_error)
    TextView nameErrorView;

    @Bind(R.id.email_error)
    TextView emailErrorView;

    @Bind(R.id.password_error)
    TextView passwordErrorView;

    @Bind(R.id.mobile_error)
    TextView mobileErrorView;

    final ProgressDialog[] progressDialog = new ProgressDialog[1];

    SignUpPresenter signUpPresenter;

    private String dividerColor = "#c9c9c9";

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
            if (!checkIfAlreadyhavePermission()) {
                requestForSpecificPermission();
            } else {
                getEmailAddress();
            }
        }
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
    }

    @Override
    public void navigateToSetUserID() {
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
        Logger.d(this, "Error: "+title+", "+message);
        AlertDialog alertDialog = new AlertDialog.Builder(SignUpActivity.this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage("\n"+message);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> dialog.dismiss());
        alertDialog.show();

        if(message.contains("email")) {
            emailErrorView.setVisibility(View.VISIBLE);
            emailErrorView.setText(message);
            emailDivider.setBackgroundColor(ContextCompat.getColor(this, R.color.error));
        }
    }

    @OnTextChanged(R.id.sign_up_email)
    public void onEmailTextChanged() {
        emailDivider.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        emailErrorView.setVisibility(View.INVISIBLE);
    }

    @OnFocusChange(R.id.sign_up_email)
    public void onEmailFocusChanged() {
        if(emailET.isFocused()) {
            emailDivider.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        } else {
            emailDivider.setBackgroundColor(Color.parseColor(dividerColor));
        }
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
        if(!isEmailValid(emailET.getText())) {
            AlertDialog alertDialog = new AlertDialog.Builder(SignUpActivity.this).create();
            alertDialog.setTitle("Invalid Email ID");
            alertDialog.setMessage("\nThis email is invalid.");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> dialog.dismiss());
            alertDialog.show();

            emailET.requestFocus();

            emailErrorView.setVisibility(View.VISIBLE);
            emailErrorView.setText("Please enter a valid email");
            emailDivider.setBackgroundColor(ContextCompat.getColor(this, R.color.error));
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

        String imei = "";
        String carrierName = "";
        imei = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

        signUpPresenter.registerUser(nameET.getText().toString(), emailET.getText().toString(), passwordET.getText().toString(), "+91", mobileNumberET.getText().toString(), imei, carrierName);

        progressDialog[0] = ProgressDialog.show(SignUpActivity.this, "", "Loading. Please wait...", true);
    }

    private boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
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

    private void getEmailAddress() {
        Pattern emailPattern = Patterns.EMAIL_ADDRESS;
        Account[] accounts = AccountManager.get(this).getAccounts();
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                String possibleEmail = account.name;
                emailET.setText(possibleEmail);
                emailDivider.setBackgroundColor(Color.parseColor(dividerColor));
            }
        }
    }

    private boolean checkIfAlreadyhavePermission() {
        int result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS);
        int result2 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        int permission = PackageManager.PERMISSION_GRANTED;

        return result1 == permission && result2 == permission;
    }

    private void requestForSpecificPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.GET_ACCOUNTS, Manifest.permission.READ_CONTACTS}, 101);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //granted
                    getEmailAddress();
                } else {
                    //not granted
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
