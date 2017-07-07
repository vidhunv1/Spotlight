package com.chat.ichat.screens.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.chat.ichat.R;
import com.chat.ichat.core.BaseActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by vidhun on 02/07/17.
 */
public class SettingsChatActivity extends BaseActivity {
    public static Intent callingIntent(Context context) {
        return new Intent(context, SettingsChatActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_chat);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.iv_back)
    public void onBackClicked() {
        super.onBackPressed();
    }
}
