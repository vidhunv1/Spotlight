package com.stairway.spotlight.screens.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.stairway.data.config.Logger;
import com.stairway.data.source.message.MessageResult;
import com.stairway.data.source.user.UserApi;
import com.stairway.data.source.user.UserSessionResult;
import com.stairway.data.source.user.gson_models.User;
import com.stairway.data.source.user.gson_models.UserResponse;
import com.stairway.spotlight.R;
import com.stairway.spotlight.core.BaseActivity;
import com.stairway.spotlight.core.FCMRegistrationIntentService;
import com.stairway.spotlight.core.di.component.ComponentContainer;
import com.stairway.spotlight.screens.contacts.ContactsActivity;
import com.stairway.spotlight.screens.home.chats.ChatListFragment;
import com.stairway.spotlight.screens.new_chat.NewChatActivity;
import com.stairway.spotlight.screens.my_profile.ProfileActivity;
import com.stairway.spotlight.screens.search.SearchFragment;
import com.stairway.spotlight.screens.search.SearchActivity;

import org.jivesoftware.smackx.chatstates.ChatState;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Subscriber;

import static com.stairway.spotlight.core.FCMRegistrationIntentService.SENT_TOKEN_TO_SERVER;

public class HomeActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private ActionBarDrawerToggle toggle;
    @Bind(R.id.fab)
    FloatingActionButton fab;

    @Bind(R.id.drawer_layout)
    DrawerLayout drawer;

    @Bind(R.id.nav_view)
    NavigationView navigationView;

    UserApi userApi;
    UserSessionResult userSession;
    ChatListFragment chatListFragment;
    SearchFragment searchFragment;

    final String FRAGMENT_CHAT = "CHAT_FRAGMENT";

    private Toolbar toolbar;

    public static Intent callingIntent(Context context) {
        Intent intent = new Intent(context, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ButterKnife.bind(this);

        toolbar = (Toolbar) findViewById(R.id.tb_home);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        setChatFragment();

        fab.setOnClickListener(view -> {
            Logger.d(this, "fab clicked");
            startActivity(NewChatActivity.callingIntent(this));
        });

        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        navigationView.getHeaderView(0).setOnClickListener(v -> {
            startActivity(ProfileActivity.callingIntent(this));
            drawer.closeDrawer(GravityCompat.START);
        });
        uploadFCMToken();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            toggle.setDrawerIndicatorEnabled(true);
            fab.setVisibility(View.VISIBLE);
            setChatFragment();
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if(id == R.id.action_search) {
            startActivity(SearchActivity.callingIntent(this));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            case R.id.nav_settings:
                break;
            case R.id.nav_contacts:
                startActivity(ContactsActivity.callingIntent(this));
                break;
            case R.id.nav_manage:
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

//    private void setNewChatFragment() {
//        String FRAGMENT_NEW_CHAT = "NEW_CHAT_FRAGMENT";
//        String TITLE_NEW_CHAT = "New Chat";
//
//        if(newChatFragment == null)
//            newChatFragment = NewChatFragment.getInstance();
//        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
//        fragmentTransaction.replace(R.id.register_FragmentContainer, newChatFragment, FRAGMENT_NEW_CHAT);
//        fragmentTransaction.addToBackStack(null);
//        fragmentTransaction.commit();
//
//        toggle.setDrawerIndicatorEnabled(false);
//        getSupportActionBar().setTitle(TITLE_NEW_CHAT);
//        fab.setVisibility(View.GONE);
//        chatListFragment = null;
//    }

//    private void setProfileFragment() {
//        String FRAGMENT_PROFILE = "PROFILE_FRAGMENT";
//        String TITLE_PROFILE = "Profile";
//
//        if(profileFragment == null)
//            profileFragment = ProfileFragment.getInstance();
//        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
//        fragmentTransaction.replace(R.id.register_FragmentContainer, profileFragment, FRAGMENT_PROFILE);
//        fragmentTransaction.addToBackStack(null);
//        fragmentTransaction.commit();
//
//        toggle.setDrawerIndicatorEnabled(false);
//        getSupportActionBar().setTitle(TITLE_PROFILE);
//        fab.setVisibility(View.GONE);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//    }

    private void setChatFragment() {
        String TITLE_HOME = "  Messages";

        if(chatListFragment == null)
            chatListFragment = ChatListFragment.getInstance();
        setSupportActionBar(toolbar);
        chatListFragment = ChatListFragment.getInstance();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.register_FragmentContainer, chatListFragment, FRAGMENT_CHAT);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void setSearchFragment() {
        if(searchFragment == null)
            searchFragment = SearchFragment.getInstance();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        String FRAGMENT_SEARCH = "SEARCH_FRAGMENT";
        fragmentTransaction.replace(R.id.register_FragmentContainer, searchFragment, FRAGMENT_SEARCH);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    protected void injectComponent(ComponentContainer componentContainer) {
        userSession = componentContainer.userSessionComponent().getUserSession();
    }

    @Override
    public void onMessageReceived(MessageResult messageId) {
        if(getVisibleFragmentTag().equals(FRAGMENT_CHAT))
            chatListFragment.addNewMessage(messageId);
    }

    @Override
    public void onChatStateReceived(String from, ChatState chatState) {
        if(getVisibleFragmentTag().equals(FRAGMENT_CHAT))
            chatListFragment.showChatState(from, chatState);
    }

    private void uploadFCMToken() {
        //Upload to token to server if FCM token not updated
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(! sharedPreferences.getBoolean(SENT_TOKEN_TO_SERVER, false)) {
            String fcmToken = sharedPreferences.getString(FCMRegistrationIntentService.FCM_TOKEN, "");
            Logger.d(this, "FCM TOKEN:"+fcmToken);
            User updateUser = new User();
            updateUser.setNotificationToken(fcmToken);
            userApi = new UserApi();
            userApi.updateUser(updateUser, userSession.getAccessToken()).subscribe(new Subscriber<UserResponse>() {
                @Override
                public void onCompleted() {}
                @Override
                public void onError(Throwable e) {
                    sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, false).apply();
                }
                @Override
                public void onNext(UserResponse userResponse) {
                    sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, true).apply();
                }
            });
        }
    }

    public String getVisibleFragmentTag(){
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        List<android.support.v4.app.Fragment> fragments = fragmentManager.getFragments();
        if(fragments != null){
            for(android.support.v4.app.Fragment fragment : fragments){
                if(fragment != null && fragment.isVisible())
                    return fragment.getTag();
            }
        }
        return null;
    }
}