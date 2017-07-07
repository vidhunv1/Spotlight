package com.chat.ichat.screens.settings;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.chat.ichat.R;
import com.chat.ichat.config.AnalyticsConstants;
import com.chat.ichat.core.BaseActivity;
import com.google.firebase.analytics.FirebaseAnalytics;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingsActivity1 extends BaseActivity {
    final ProgressDialog[] progressDialog = new ProgressDialog[1];

    public static final String PREFS_FILE = "settings";
    private FirebaseAnalytics firebaseAnalytics;

    public static Intent callingIntent(Context context) {
        return new Intent(context, SettingsActivity1.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings1);
        ButterKnife.bind(this);

        this.firebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        /*              Analytics           */
        firebaseAnalytics.setCurrentScreen(this, AnalyticsConstants.Event.SETTINGS_SCREEN, null);
    }

    public void showError(String title, String message) {
        if(progressDialog[0]!=null && progressDialog[0].isShowing()) {
            progressDialog[0].dismiss();
        }

        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage("\n"+message);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }

    @OnClick(R.id.ll_settings_account)
    public void onAccountClicked() {
        startActivity(SettingsAccountActivity.callingIntent(this));
    }

    @OnClick(R.id.ll_settings_notification)
    public void onNotificationClicked() {
        startActivity(SettingsNotificationActivity.callingIntent(this));
    }

    @OnClick(R.id.ll_settings_chat)
    public void onChatClicked() {
        startActivity(SettingsChatActivity.callingIntent(this));
    }

    @OnClick(R.id.ll_settings_privacy)
    public void onPrivacyClicked() {
        startActivity(SettingsPrivacyActivity.callingIntent(this));
    }

    @OnClick(R.id.ll_settings_help)
    public void onHelpClicked() {
        startActivity(SettingsHelpActivity.callingIntent(this));
    }

    @OnClick(R.id.iv_back)
    public void onBackClicked() {
        super.onBackPressed();
    }
}