package com.stairway.spotlight.screens.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.stairway.data.config.Logger;
import com.stairway.data.source.message.MessageResult;
import com.stairway.data.source.user.UserAuthApi;
import com.stairway.data.source.user.UserSessionResult;
import com.stairway.data.source.user.gson_models.User;
import com.stairway.data.source.user.gson_models.UserResponse;
import com.stairway.spotlight.R;
import com.stairway.spotlight.core.BaseActivity;
import com.stairway.spotlight.core.FCMRegistrationIntentService;
import com.stairway.spotlight.core.di.component.ComponentContainer;
import com.stairway.spotlight.screens.home.chats.ChatListFragment;
import com.stairway.spotlight.screens.home.new_chat.NewChatFragment;
import com.stairway.spotlight.screens.home.profile.ProfileFragment;

import org.jivesoftware.smackx.chatstates.ChatState;

import rx.Subscriber;
import static com.stairway.spotlight.core.FCMRegistrationIntentService.SENT_TOKEN_TO_SERVER;

public class HomeActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private ActionBarDrawerToggle toggle;
    private FloatingActionButton fab;
    UserAuthApi userAuthApi;
    UserSessionResult userSession;
    ChatListFragment chatListFragment;
    private Toolbar toolbar;
    private static String TITLE_HOME = "Messages";
    private static String TITLE_NEW_CHAT = "New Chat";
    private static String TITLE_PROFILE = "Profile";

    public static Intent callingIntent(Context context) {
        Intent intent = new Intent(context, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_drawer);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Permissions
//        int MyVersion = Build.VERSION.SDK_INT;
//        if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
//            if (!checkIfAlreadyhavePermission()) {
//                requestForSpecificPermission();
//            }
//        }

        setChatFragment();

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(view -> setContactsFragment());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            toggle.setDrawerIndicatorEnabled(true);
            fab.setVisibility(View.VISIBLE);
            toolbar.setTitle(TITLE_HOME);
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_activity_drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == android.R.id.home) {
            Logger.d("[HomeActivity] backPressed");
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_contacts) {
            setContactsFragment();

        } else if (id == R.id.nav_profile) {
            setProfileFragment();
        } else if (id == R.id.nav_manage) {
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setContactsFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.register_FragmentContainer, NewChatFragment.getInstance());
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

        toggle.setDrawerIndicatorEnabled(false);
        toolbar.setTitle(TITLE_NEW_CHAT);
        fab.setVisibility(View.GONE);
        chatListFragment = null;
    }

    private void setProfileFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.register_FragmentContainer, ProfileFragment.getInstance());
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

        toggle.setDrawerIndicatorEnabled(false);
        toolbar.setTitle(TITLE_PROFILE);
        fab.setVisibility(View.GONE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        chatListFragment = null;
    }

    private void setChatFragment() {
        setSupportActionBar(toolbar);
        chatListFragment = ChatListFragment.getInstance();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.register_FragmentContainer, chatListFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        toolbar.setTitle(TITLE_HOME);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

//    private boolean checkIfAlreadyhavePermission() {
//        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
//        if (result == PackageManager.PERMISSION_GRANTED) {
//            return true;
//        } else
//            return false;
//    }
//
//    private void requestForSpecificPermission() {
//        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS}, 101);
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

    @Override
    protected void injectComponent(ComponentContainer componentContainer) {
        userSession = componentContainer.userSessionComponent().getUserSession();
    }

    @Override
    public void onMessageReceived(MessageResult messageId) {
        if(chatListFragment!=null) {
            chatListFragment.addNewMessage(messageId);
        }
    }

    @Override
    public void onChatStateReceived(String from, ChatState chatState) {
        if(chatListFragment!=null)
            chatListFragment.showChatState(from, chatState);
    }
}
