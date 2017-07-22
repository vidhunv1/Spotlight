//package com.chat.ichat.screens.settings;
//
//import android.content.Context;
//import android.content.Intent;
//import android.os.Bundle;
//
//import com.chat.ichat.R;
//import com.chat.ichat.core.BaseActivity;
//
//import butterknife.ButterKnife;
//import butterknife.OnClick;
//
///**
// * Created by vidhun on 02/07/17.
// */
//public class SettingsNotificationActivity extends BaseActivity {
//    public static Intent callingIntent(Context context) {
//        return new Intent(context, SettingsNotificationActivity.class);
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_settings_notifications);
//        ButterKnife.bind(this);
//    }
//
//    @OnClick(R.id.iv_back)
//    public void onBackClicked() {
//        super.onBackPressed();
//    }
//}
