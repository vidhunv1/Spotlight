//package com.chat.ichat.screens.settings;
//
//import android.app.AlertDialog;
//import android.app.ProgressDialog;
//import android.content.Context;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.support.v4.content.ContextCompat;
//import android.support.v7.widget.LinearLayoutCompat;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//import com.chat.ichat.MessageService;
//import com.chat.ichat.R;
//import com.chat.ichat.UserSessionManager;
//import com.chat.ichat.api.ApiError;
//import com.chat.ichat.api.ApiManager;
//import com.chat.ichat.api.StatusResponse;
//import com.chat.ichat.application.SpotlightApplication;
//import com.chat.ichat.core.BaseActivity;
//import com.chat.ichat.core.FCMRegistrationIntentService;
//import com.chat.ichat.core.lib.AndroidUtils;
//import com.chat.ichat.models.UserSession;
//import com.chat.ichat.screens.home.HomeActivity;
//import com.chat.ichat.screens.welcome.WelcomeActivity;
//import com.google.firebase.analytics.FirebaseAnalytics;
//
//import butterknife.Bind;
//import butterknife.ButterKnife;
//import butterknife.OnClick;
//import rx.Subscriber;
//import rx.Subscription;
//import rx.android.schedulers.AndroidSchedulers;
//import rx.schedulers.Schedulers;
//
//import static com.chat.ichat.screens.settings.SettingsActivity1.PREFS_FILE;
//
///**
// * Created by vidhun on 02/07/17.
// */
//
//public class SettingsAccountActivity extends BaseActivity {
//    @Bind(R.id.settings_account_name)
//    TextView name;
//
//    @Bind(R.id.settings_accout_phone)
//    TextView phone;
//
//    final ProgressDialog[] progressDialog = new ProgressDialog[1];
//
//    public static Intent callingIntent(Context context) {
//        return new Intent(context, SettingsAccountActivity.class);
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_settings_account);
//        ButterKnife.bind(this);
//
//        UserSession userSession = UserSessionManager.getInstance().load();
//        name.setText(userSession.getName());
//        phone.setText(userSession.getMobile());
//    }
//
//    @OnClick(R.id.iv_back)
//    public void onBackClicked() {
//        super.onBackPressed();
//    }
//
//    @OnClick(R.id.ll_settings_logout)
//    public void showLogoutPopup() {
//        LinearLayout parent = new LinearLayout(this);
//
//        parent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
//        parent.setOrientation(LinearLayout.VERTICAL);
//        parent.setPadding((int) AndroidUtils.px(24),(int)AndroidUtils.px(18), (int)AndroidUtils.px(24), 0);
//
//        TextView textView1 = new TextView(this);
//        textView1.setText("Are you sure want to log out?");
//        textView1.setTextColor(ContextCompat.getColor(this, R.color.textColor));
//        textView1.setTextSize(16);
//
//        parent.addView(textView1);
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle(getResources().getString(R.string.app_name));
//        builder.setPositiveButton("OK", ((dialog, which) -> {
//            if(progressDialog[0]!=null && progressDialog[0].isShowing()) {
//                progressDialog[0].dismiss();
//            }
//            progressDialog[0] = ProgressDialog.show(this, "", "Logging out. Please wait...", true);
//            SettingsAccountActivity settingsAccountActivity = SettingsAccountActivity.this;
//            ApiManager.getUserApi().logout()
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(new Subscriber<StatusResponse>() {
//                        @Override
//                        public void onCompleted() {}
//
//                        @Override
//                        public void onError(Throwable e) {
//                            ApiError error = new ApiError(e);
//                            settingsAccountActivity.showError(error.getTitle(), error.getMessage());
//                        }
//
//                        @Override
//                        public void onNext(StatusResponse response) {
//                            if(!response.isSuccess()) {
//                                settingsAccountActivity.showError(response.getError().getTitle(), response.getError().getMessage());
//                            } else {
//                                UserSessionManager.getInstance().clear();
//                                ApiManager.reset();
//                                SharedPreferences sharedPreferences = SpotlightApplication.getContext().getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
//                                sharedPreferences.edit().putBoolean(FCMRegistrationIntentService.SENT_TOKEN_TO_SERVER, false).apply();
//                                SharedPreferences sp = SpotlightApplication.getContext().getSharedPreferences(HomeActivity.APP_PREFS_FILE, Context.MODE_PRIVATE);
//                                sp.edit().putLong(HomeActivity.KEY_LAST_SYNC, -1).apply();
//
//                                if(progressDialog[0].isShowing()) {
//                                    progressDialog[0].dismiss();
//                                }
//                                stopService(new Intent(settingsAccountActivity, MessageService.class));
//                                startActivity(WelcomeActivity.callingIntent(settingsAccountActivity));
//                                finish();
//                            }
//                        }
//                    });
//
//        }));
//        builder.setNegativeButton("CANCEL", ((dialog, which) -> {}));
//        builder.setView(parent);
//        AlertDialog alertDialog = builder.create();
//        alertDialog.show();
//    }
//
//    public void showError(String title, String message) {
//        if(progressDialog[0]!=null && progressDialog[0].isShowing()) {
//            progressDialog[0].dismiss();
//        }
//
//        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
//        alertDialog.setTitle(title);
//        alertDialog.setMessage("\n"+message);
//        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> dialog.dismiss());
//        alertDialog.show();
//    }
//}
