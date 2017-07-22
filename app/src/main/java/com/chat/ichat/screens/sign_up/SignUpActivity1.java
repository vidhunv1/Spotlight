package com.chat.ichat.screens.sign_up;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.chat.ichat.R;
import com.chat.ichat.UserSessionManager;
import com.chat.ichat.api.ApiError;
import com.chat.ichat.api.ApiManager;
import com.chat.ichat.api.user.UserRequest;
import com.chat.ichat.api.user.UserResponse;
import com.chat.ichat.api.user._User;
import com.chat.ichat.components.HintEditText;
import com.chat.ichat.config.AnalyticsConstants;
import com.chat.ichat.core.Logger;
import com.chat.ichat.core.lib.PhoneFormat;
import com.chat.ichat.models.UserSession;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by vidhun on 03/07/17.
 */
public class SignUpActivity1 extends AppCompatActivity {
    @Bind(R.id.tb_sign_up)
    Toolbar toolbar;
    @Bind(R.id.verify_code)
    HintEditText phoneET;
    @Bind(R.id.country_code)
    EditText countryCodeET;
    @Bind(R.id.country_selector)
    TextView countrySelector;
    private int countryState = 0;

    private FirebaseAnalytics firebaseAnalytics;

    private HashMap<String, String> phoneFormatMap = new HashMap<>();
    private ArrayList<String> countriesArray = new ArrayList<>();
    private HashMap<String, String> countriesMap = new HashMap<>();
    private HashMap<String, String> codesMap = new HashMap<>();

    private boolean ignoreSelection = false;
    private boolean ignoreOnTextChange = false;
    private boolean ignoreOnPhoneChange = false;

    final ProgressDialog[] progressDialog = new ProgressDialog[1];

    public static Intent callingIntent(Context context) {
        Intent intent = new Intent(context, SignUpActivity1.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.d(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.bind(this);
        this.firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        firebaseAnalytics.setCurrentScreen(this, AnalyticsConstants.Event.SIGNUP_SCREEN, null);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        HashMap<String, String> languageMap = new HashMap<>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getResources().getAssets().open("countries.txt")));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] args = line.split(";");
                countriesArray.add(0, args[2]);
                countriesMap.put(args[2], args[0]);
                codesMap.put(args[0], args[2]);
                if (args.length > 3) {
                    phoneFormatMap.put(args[0], args[3]);
                }
                languageMap.put(args[1], args[2]);
            }
            reader.close();
        } catch (Exception e) {
            Logger.e(this, e.toString());
        }

        countryCodeET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (ignoreOnTextChange) {
                    return;
                }
                ignoreOnTextChange = true;
                String text = PhoneFormat.stripExceptNumbers(countryCodeET.getText().toString());
                countryCodeET.setText(text);
                if (text.length() == 0) {
                    countrySelector.setText("Enter Country Code");
                    phoneET.setHintText(null);
                    countryState = 1;
                } else {
                    String country;
                    boolean ok = false;
                    String textToSet = null;
                    if (text.length() > 4) {
                        for (int a = 4; a >= 1; a--) {
                            String sub = text.substring(0, a);
                            country = codesMap.get(sub);
                            if (country != null) {
                                ok = true;
                                textToSet = text.substring(a, text.length()) + phoneET.getText().toString();
                                countryCodeET.setText(text = sub);
                                break;
                            }
                        }
                        if (!ok) {
                            textToSet = text.substring(1, text.length()) + phoneET.getText().toString();
                            countryCodeET.setText(text = text.substring(0, 1));
                        }
                    }
                    country = codesMap.get(text);
                    if (country != null) {
                        int index = countriesArray.indexOf(country);
                        if (index != -1) {
                            ignoreSelection = true;
                            countrySelector.setText(countriesArray.get(index));
                            String hint = phoneFormatMap.get(text);
                            phoneET.setHintText(hint != null ? hint.replace('X', '–') : null);
                            countryState = 0;
                        } else {
                            countrySelector.setText("Wrong country code");
                            phoneET.setHintText(null);
                            countryState = 2;
                        }
                    } else {
                        countrySelector.setText("Wrong country code");
                        phoneET.setHintText(null);
                        countryState = 2;
                    }
                    if (!ok) {
                        countryCodeET.setSelection(countryCodeET.getText().length());
                    }
                    if (textToSet != null) {
                        phoneET.requestFocus();
                        phoneET.setText(textToSet);
                        phoneET.setSelection(phoneET.length());
                    }
                }
                ignoreOnTextChange = false;
            }
        });

        String hint = phoneFormatMap.get("91");
        Logger.d(this, "hint: "+hint);
        phoneET.setHintText(hint != null ? hint.replace('X', '–') : null);
        phoneET.requestFocus();

        phoneET.setInputType(InputType.TYPE_CLASS_PHONE);
        phoneET.setHintTextColor(ContextCompat.getColor(this, R.color.appElement));
        phoneET.setMaxLines(1);
        phoneET.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        phoneET.setImeOptions(EditorInfo.IME_ACTION_NEXT | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        phoneET.addTextChangedListener(new TextWatcher() {

            private int characterAction = -1;
            private int actionPosition;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (count == 0 && after == 1) {
                    characterAction = 1;
                } else if (count == 1 && after == 0) {
                    if (s.charAt(start) == ' ' && start > 0) {
                        characterAction = 3;
                        actionPosition = start - 1;
                    } else {
                        characterAction = 2;
                    }
                } else {
                    characterAction = -1;
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                phoneET.onTextChange();
                if (ignoreOnPhoneChange) {
                    return;
                }
                int start = phoneET.getSelectionStart();
                String phoneChars = "0123456789";
                String str = phoneET.getText().toString();
                if (characterAction == 3) {
                    str = str.substring(0, actionPosition) + str.substring(actionPosition + 1, str.length());
                    start--;
                }
                StringBuilder builder = new StringBuilder(str.length());
                for (int a = 0; a < str.length(); a++) {
                    String ch = str.substring(a, a + 1);
                    if (phoneChars.contains(ch)) {
                        builder.append(ch);
                    }
                }
                ignoreOnPhoneChange = true;
                String hint = phoneET.getHintText();
                if (hint != null) {
                    for (int a = 0; a < builder.length(); a++) {
                        if (a < hint.length()) {
                            if (hint.charAt(a) == ' ') {
                                builder.insert(a, ' ');
                                a++;
                                if (start == a && characterAction != 2 && characterAction != 3) {
                                    start++;
                                }
                            }
                        } else {
                            builder.insert(a, ' ');
                            if (start == a + 1 && characterAction != 2 && characterAction != 3) {
                                start++;
                            }
                            break;
                        }
                    }
                }
                phoneET.setText(builder);
                if (start >= 0) {
                    phoneET.setSelection(start <= phoneET.length() ? start : phoneET.length());
                }
                phoneET.onTextChange();
                ignoreOnPhoneChange = false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        firebaseAnalytics.logEvent(AnalyticsConstants.Event.SIGNUP_DONE, null);
        if(countryState!=0) {
            firebaseAnalytics.logEvent(AnalyticsConstants.Event.SIGNUP_ERROR_COUNTRYCODE, null);
            showError(this.getResources().getString(R.string.app_name), "Wrong country code");
        } else if(phoneET.getHintText().length() != phoneET.getText().length()) {
            firebaseAnalytics.logEvent(AnalyticsConstants.Event.SIGNUP_ERROR_PHONE, null);
            showError(this.getResources().getString(R.string.app_name), "Invalid phone number");
        } else {
            firebaseAnalytics.logEvent(AnalyticsConstants.Event.SIGNUP_SUCCESS, null);
            String imei = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
            registerUser(countryCodeET.getText().toString(), phoneET.getText().toString().replace(" ", ""), imei);
//            "", "", UUID.randomUUID().toString().replaceAll("-", ""),
        }
    }

    public void registerUser(String countryCode, String mobile, String imei) {
        progressDialog[0] = ProgressDialog.show(SignUpActivity1.this, "", "Loading. Please wait...", true);
        UserSessionManager userSessionManager = UserSessionManager.getInstance();

        UserRequest request = new UserRequest();
        _User user = new _User();
        user.setCountryCode(countryCode);
        user.setPhone(mobile);
        user.setUserType(_User.UserType.regular);
        user.setIMEI(imei);
        request.setUser(user);

        SignUpActivity1 signUpActivity1 = SignUpActivity1.this;
        ApiManager.getUserApi().createUser(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<UserResponse>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        ApiError error = new ApiError(e);
                        signUpActivity1.showError(error.getTitle(), error.getMessage());
                    }

                    @Override
                    public void onNext(UserResponse userResponse) {
                        if(!userResponse.isSuccess()) {
                            signUpActivity1.showError(userResponse.getError().getTitle(), userResponse.getError().getMessage());
                        } else {
                            UserSession userSession = new UserSession();
                            if(userResponse.getUser().getUsername()!=null && !userResponse.getUser().getUsername().isEmpty())
                                userSession.setName(userResponse.getUser().getName());
                            if(userResponse.getUser().getUserId()!=null && !userResponse.getUser().getUserId().isEmpty())
                                userSession.setUserId(userResponse.getUser().getUserId());
                            if(userResponse.getUser().getUsername()!=null && !userResponse.getUser().getUsername().isEmpty())
                                userSession.setUserName(userResponse.getUser().getUsername());
                            userSessionManager.save(userSession);
                            progressDialog[0].dismiss();
                            startActivity(PhoneVerifyActivity.callingIntent(signUpActivity1, countryCode, mobile, userResponse.getVerificationUuid()));
                        }
                    }
                });
    }
}
