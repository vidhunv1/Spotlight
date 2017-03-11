package com.stairway.spotlight.screens.sign_up;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.EditText;

import com.stairway.spotlight.UserSessionManager;
import com.stairway.spotlight.R;
import com.stairway.spotlight.api.ApiManager;
import com.stairway.spotlight.core.Logger;
import com.stairway.spotlight.screens.home.HomeActivity;
import com.stairway.spotlight.screens.new_chat.NewChatActivity;
import com.stairway.spotlight.screens.user_id.SetUserIdActivity;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
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

    @Bind(R.id.sign_up_country_code)
    EditText countryCodeET;

    @Bind(R.id.sign_up_btn)
    Button signupButton;

    final ProgressDialog[] progressDialog = new ProgressDialog[1];

    SignUpPresenter signUpPresenter;
    public static Intent callingIntent(Context context) {
        Intent intent = new Intent(context, SignUpActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
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

        changeSignUpButton();
        signUpPresenter = new SignUpPresenter(ApiManager.getUserApi(), UserSessionManager.getInstance());

        // Permissions
//        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
//            if (!checkIfAlreadyhavePermission()) {
//                requestForSpecificPermission();
//            }
//        }
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

    @OnTextChanged(R.id.sign_up_email)
    public void onEmailTextChanged() {
        changeSignUpButton();
//        if(isEmailValid(emailET.getText())) {
//            emailTIL.setErrorEnabled(false);
//            emailTIL.setError(null);
//        }
    }
//
//    @OnFocusChange(R.id.sign_up_email)
//    public void onEmailFocusChanged() {
//        if(emailET.isFocused()) {
//        } else {
//            if (!isEmailValid(emailET.getText()) && emailET.getText().length()>0) {
//                emailTIL.setErrorEnabled(true);
//                emailTIL.setError("Please enter a valid email.");
//            } else {
//                emailTIL.setError(null);
//                emailTIL.setErrorEnabled(false);
//            }
//        }
//    }
//
    @OnTextChanged(R.id.sign_up_password)
    public void onPasswordTextChanged() {
        changeSignUpButton();
//        if(isPasswordValid(passwordET.getText())) {
//            passwordTIL.setErrorEnabled(false);
//            passwordTIL.setError(null);
//        }
    }
//
//    @OnFocusChange(R.id.sign_up_password)
//    public void onPasswordFocuChanged() {
//        if(passwordET.isFocused()) {
//        } else {
//            if(!isPasswordValid(passwordET.getText()) && passwordET.getText().length()>0) {
//                passwordTIL.setErrorEnabled(true);
//                passwordTIL.setError("Minimum 5 characters required.");
//            } else {
//                passwordTIL.setError(null);
//                passwordTIL.setErrorEnabled(false);
//            }
//        }
//    }
//
    @OnTextChanged(R.id.sign_up_name)
    public void onNameTextChanged() {
        changeSignUpButton();
//        if(isNameValid(nameET.getText())) {
//            nameTIL.setErrorEnabled(false);
//            nameTIL.setError(null);
//        }
    }
//
//    @OnFocusChange(R.id.sign_up_name)
//    public void onNameFocusChanged() {
//        if(nameET.isFocused()) {
//        } else {
//            if(!isNameValid(nameET.getText()) && nameET.getText().length()>0) {
//                nameTIL.setErrorEnabled(true);
//                nameTIL.setError("Please enter your full name.");
//            } else {
//                nameTIL.setError(null);
//                nameTIL.setErrorEnabled(false);
//            }
//        }
//    }
//
    @OnTextChanged(R.id.sign_up_mobile_number)
    public void onMobileTextChanged() {
        changeSignUpButton();
//        if(isMobileNumberValid(mobileNumberET.getText())) {
//            mobileTIL.setErrorEnabled(false);
//            mobileTIL.setError(null);
//        }
    }
//
//    @OnFocusChange(R.id.sign_up_tilMobile)
//    public void onMobileFocusChanged() {
//        if(mobileNumberET.isFocused()) {
//        } else {
//            if(!isMobileNumberValid(mobileNumberET.getText()) && mobileNumberET.getText().length()>0) {
//                mobileTIL.setErrorEnabled(true);
//                mobileTIL.setError("Please enter a valid mobile number.");
//            } else {
//                mobileTIL.setError(null);
//                mobileTIL.setErrorEnabled(false);
//            }
//        }
//    }

    @OnClick(R.id.sign_up_btn)
    public void onSignUpClicked() {
        if(!(nameET.getText().length()>=1 && emailET.getText().length()>=1 && passwordET.getText().length()>=1 && mobileNumberET.getText().length()>=1)) {
            signupButton.setActivated(false);
            return;
        } else {
            signupButton.setActivated(true);
        }
        if(!isEmailValid(emailET.getText())) {
            AlertDialog alertDialog = new AlertDialog.Builder(SignUpActivity.this).create();
            alertDialog.setTitle("Invalid Email ID");
            alertDialog.setMessage("\nThis email is invalid.");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> dialog.dismiss());
            alertDialog.show();
            return;
        }
        if(!isPasswordValid(passwordET.getText())) {
            AlertDialog alertDialog = new AlertDialog.Builder(SignUpActivity.this).create();
            alertDialog.setTitle("Sign-up Failed");
            alertDialog.setMessage("\nPassword must be 8 to 16 characters long.");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> dialog.dismiss());
            alertDialog.show();
            return;
        }
        if(!isMobileNumberValid(mobileNumberET.getText())) {
            AlertDialog alertDialog = new AlertDialog.Builder(SignUpActivity.this).create();
            alertDialog.setTitle("Invalid Phone Number");
            alertDialog.setMessage("\nThis phone number is invalid.");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> dialog.dismiss());
            alertDialog.show();
            return;
        }

        signUpPresenter.registerUser(nameET.getText().toString(), emailET.getText().toString(), passwordET.getText().toString(), countryCodeET.getText().toString(), mobileNumberET.getText().toString());

        progressDialog[0] = ProgressDialog.show(SignUpActivity.this, "", "Loading. Please wait...", true);
    }

    public void changeSignUpButton() {
        if(nameET.getText().length()>=1 && emailET.getText().length()>=1 && passwordET.getText().length()>=1 && mobileNumberET.getText().length()>=1) {
            signupButton.setAlpha(1f);
        } else {
            signupButton.setAlpha(0.6f);
        }
    }

    private boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isPasswordValid(CharSequence pass) {
        return pass.length()>=8 && pass.length()<=16;
    }

    private boolean isMobileNumberValid(CharSequence mobile) {
        return mobile.length()>=10;
    }

    private boolean isNameValid(CharSequence name) {
        return name.length()>=3;
    }

//    private boolean checkIfAlreadyhavePermission() {
//        int result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
//        int result2 = ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS);
//        int permission = PackageManager.PERMISSION_GRANTED;
//
//        return result1 == permission && result2 == permission;
//    }
//
//    private void requestForSpecificPermission() {
//        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS,
//                Manifest.permission.RECEIVE_SMS,
//                Manifest.permission.READ_SMS}, 101);
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        switch (requestCode) {
//            case 101:
//                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    //granted
//                } else {
//                    //not granted
//                }
//                break;
//            default:
//                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        }
//    }
}
