package com.stairway.spotlight.screens.home;

import android.Manifest;
import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;
import com.stairway.data.manager.Logger;
import com.stairway.data.source.user.UserAuthApi;
import com.stairway.data.source.user.UserSessionResult;
import com.stairway.data.source.user.models.User;
import com.stairway.data.source.user.models.UserResponse;
import com.stairway.spotlight.R;
import com.stairway.spotlight.core.FCMRegistrationIntentService;
import com.stairway.spotlight.core.di.component.ComponentContainer;
import com.stairway.spotlight.core.BaseActivity;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Subscriber;

import static com.stairway.spotlight.core.FCMRegistrationIntentService.SENT_TOKEN_TO_SERVER;

public class HomeActivity extends BaseActivity{

    UserSessionResult userSession;
    UserAuthApi userAuthApi;

    public static Intent callingIntent(Context context) {
        Intent intent = new Intent(context, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        return intent;
    }

    View callView, chatView, contactView, profileView, actionBarView;
    TextView callText, chatText, contactText, profileText;
    ImageView callImage, chatImage, contactImage, profileImage;
    ViewPager viewPager;
    TabLayout tabLayout;
    Drawable profileIcon, chatIcon, contactIcon, callIcon;
    ImageButton actionBarIB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        viewPager  = (ViewPager) findViewById(R.id.home_viewpager);
        tabLayout  = (TabLayout) findViewById(R.id.home_sliding_tabs);

        callView = LayoutInflater.from(this).inflate(R.layout.navigation_indicatior_text, null);
        callText = (TextView) callView.findViewById(R.id.navigation_text);
        callImage = (ImageView) callView.findViewById(R.id.navigation_icon);

        chatView = LayoutInflater.from(this).inflate(R.layout.navigation_indicatior_text, null);
        chatText = (TextView) chatView.findViewById(R.id.navigation_text);
        chatImage = (ImageView) chatView.findViewById(R.id.navigation_icon);

        contactView = LayoutInflater.from(this).inflate(R.layout.navigation_indicatior_text, null);
        contactText = (TextView) contactView.findViewById(R.id.navigation_text);
        contactImage = (ImageView) contactView.findViewById(R.id.navigation_icon);

        profileView = LayoutInflater.from(this).inflate(R.layout.navigation_indicatior_text, null);
        profileText = (TextView) profileView.findViewById(R.id.navigation_text);
        profileImage = (ImageView) profileView.findViewById(R.id.navigation_icon);


        profileIcon = ContextCompat.getDrawable(this, R.drawable.ic_profile_tab);
        contactIcon = ContextCompat.getDrawable(this, R.drawable.ic_contacts_tab);
        chatIcon = ContextCompat.getDrawable(this, R.drawable.ic_chat_tab);
        callIcon = ContextCompat.getDrawable(this, R.drawable.ic_call_tab);

        android.support.v7.app.ActionBar ab = getSupportActionBar();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int MyVersion = Build.VERSION.SDK_INT;

        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        ab.setCustomView(R.layout.actionbar_home);
        ab.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        ab.setElevation(0);
        actionBarView = ab.getCustomView();
        actionBarIB = (ImageButton) actionBarView.findViewById(R.id.actionbar_image);

        // Permissions
        if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (!checkIfAlreadyhavePermission()) {
                requestForSpecificPermission();
            }
        }

        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(new HomePagerAdapter(getSupportFragmentManager(), HomeActivity.this));
        tabLayout.setupWithViewPager(viewPager);

        setTab(0, false);
        setTab(1, false);
        setTab(2, false);
        setTab(3, false);

        viewPager.setCurrentItem(0);
        // bug workaround
        setTab(0, true);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setTab(tab.getPosition(), true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                setTab(tab.getPosition(), false);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        Intent intent = new Intent(this, FCMRegistrationIntentService.class);
        startService(intent);
        FirebaseInstanceId instanceId = FirebaseInstanceId.getInstance();
        String fCMToken = instanceId.getToken();

        //Upload to token to server if FCM token not updated
        if(! sharedPreferences.getBoolean(SENT_TOKEN_TO_SERVER, false)) {
            String fcmToken = sharedPreferences.getString(FCMRegistrationIntentService.FCM_TOKEN, "");
            Logger.d("[HomeActivity] FCM TOKEN:"+fcmToken);
            User updateUser = new User();
            updateUser.setNotificationToken(fcmToken);
            userAuthApi = new UserAuthApi();
            userAuthApi.updateUser(updateUser, userSession.getAccessToken()).subscribe(new Subscriber<UserResponse>() {
                @Override
                public void onCompleted() {}
                @Override
                public void onError(Throwable e) {
                    sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, true).apply();
                }
                @Override
                public void onNext(UserResponse userResponse) {
                    sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, true).apply();
                }
            });
        }
    }

    /*
    * Works only if all tabs are initialized first and then isActive set to 'true'.
    * */
    private void setTab(int tabPosition, boolean isActive){
        switch (tabPosition){
            case 0:
                if(isActive) {
                    callIcon.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), PorterDuff.Mode.SRC_IN));
                    callText.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
                    actionBarIB.setImageResource(R.drawable.ic_new_call);
                }
                else {
                    callIcon.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorPrimaryDark), PorterDuff.Mode.SRC_IN));
                    callText.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
                }
                callImage.setImageDrawable(callIcon);
                callText.setText("Calls");
                tabLayout.getTabAt(0).setCustomView(callView);
                break;
            case 1:
                if(isActive) {
                    chatIcon.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), PorterDuff.Mode.SRC_IN));
                    chatText.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
                    actionBarIB.setImageResource(R.drawable.ic_new_chat);
                }
                else {
                    chatIcon.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorPrimaryDark), PorterDuff.Mode.SRC_IN));
                    chatText.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
                }
                chatImage.setImageDrawable(chatIcon);
                chatText.setText("Chats");
                tabLayout.getTabAt(1).setCustomView(chatView);
                break;
            case 2:
                if(isActive) {
                    contactIcon.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), PorterDuff.Mode.SRC_IN));
                    contactText.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
                    actionBarIB.setImageResource(R.drawable.ic_new_contact);
                }
                else {
                    contactIcon.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorPrimaryDark), PorterDuff.Mode.SRC_IN));
                    contactText.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
                }
                contactImage.setImageDrawable(contactIcon);
                contactText.setText("Contacts");
                tabLayout.getTabAt(2).setCustomView(contactView);
                break;
            case 3:
                if(isActive) {
                    profileIcon.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), PorterDuff.Mode.SRC_IN));
                    profileText.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
                    actionBarIB.setImageResource(R.drawable.ic_camera);
                }
                else {
                    profileIcon.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorPrimaryDark), PorterDuff.Mode.SRC_IN));
                    profileText.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
                }
                profileImage.setImageDrawable(profileIcon);
                profileText.setText("Profile");
                tabLayout.getTabAt(3).setCustomView(profileView);
                break;
            default:
                throw new IllegalArgumentException("Unspecified tab");
        }
    }

    private boolean checkIfAlreadyhavePermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else
            return false;
    }

    private void requestForSpecificPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS}, 101);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //granted
                } else {
                    //not granted
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void injectComponent(ComponentContainer componentContainer) {
        userSession = componentContainer.userSessionComponent().getUserSession();
    }
}
