package com.chat.ichat.screens.settings;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chat.ichat.R;
import com.chat.ichat.config.AnalyticsConstants;
import com.chat.ichat.core.BaseFragment;
import com.google.firebase.analytics.FirebaseAnalytics;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by vidhun on 28/07/17.
 */

public class SettingsFragment extends BaseFragment {
    final ProgressDialog[] progressDialog = new ProgressDialog[1];

    public static final String PREFS_FILE = "settings";
    private FirebaseAnalytics firebaseAnalytics;

    public static SettingsFragment getInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.firebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void showError(String title, String message) {
        if(progressDialog[0]!=null && progressDialog[0].isShowing()) {
            progressDialog[0].dismiss();
        }

        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage("\n"+message);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }

    @OnClick(R.id.ll_settings_account)
    public void onAccountClicked() {
        startActivity(SettingsAccountActivity.callingIntent(getActivity()));
    }

    @OnClick(R.id.ll_settings_notification)
    public void onNotificationClicked() {
        startActivity(SettingsNotificationActivity.callingIntent(getActivity()));
    }

    @OnClick(R.id.ll_settings_chat)
    public void onChatClicked() {
        startActivity(SettingsChatActivity.callingIntent(getActivity()));
    }

    @OnClick(R.id.ll_settings_privacy)
    public void onPrivacyClicked() {
        startActivity(SettingsPrivacyActivity.callingIntent(getActivity()));
    }

    @OnClick(R.id.ll_settings_help)
    public void onHelpClicked() {
        startActivity(SettingsHelpActivity.callingIntent(getActivity()));
    }

}
