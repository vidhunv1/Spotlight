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
import com.stairway.spotlight.screens.home.new_chat.NewChatFragment;
import com.stairway.spotlight.screens.home.profile.ProfileFragment;
import com.stairway.spotlight.screens.search.SearchFragment;
import com.stairway.spotlight.screens.search.SearchActivity;

import org.jivesoftware.smackx.chatstates.ChatState;
import java.util.List;

import rx.Subscriber;

import static com.stairway.spotlight.core.FCMRegistrationIntentService.SENT_TOKEN_TO_SERVER;

public class HomeActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private ActionBarDrawerToggle toggle;
    private FloatingActionButton fab;
    UserApi userApi;
    UserSessionResult userSession;
    ChatListFragment chatListFragment;
    ProfileFragment profileFragment;
    NewChatFragment newChatFragment;
    SearchFragment searchFragment;

    private Toolbar toolbar;
    private static String TITLE_HOME = "Messages";
    private static String TITLE_NEW_CHAT = "New Chat";
    private static String TITLE_PROFILE = "Profile";
    private final String FRAGMENT_CHAT = "CHAT_FRAGMENT", FRAGMENT_PROFILE = "PROFILE_FRAGMENT", FRAGMENT_NEW_CHAT = "NEW_CHAT_FRAGMENT", FRAGMENT_SEARCH = "SEARCH_FRAGMENT";

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
        setChatFragment();

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(view -> setNewChatFragment());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        uploadFCMToken();
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
        getMenuInflater().inflate(R.menu.home_activity_drawer, menu);
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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        int id = item.getItemId();
        if (id == R.id.nav_settings) {
        } else if (id == R.id.nav_contacts) {
            startActivity(ContactsActivity.callingIntent(this));
        } else if (id == R.id.nav_profile) {
            setProfileFragment();
        } else if (id == R.id.nav_manage) {
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setNewChatFragment() {
        if(newChatFragment == null)
            newChatFragment = NewChatFragment.getInstance();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.register_FragmentContainer, newChatFragment, FRAGMENT_NEW_CHAT);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

        toggle.setDrawerIndicatorEnabled(false);
        toolbar.setTitle(TITLE_NEW_CHAT);
        fab.setVisibility(View.GONE);
        chatListFragment = null;
    }

    private void setProfileFragment() {
        if(profileFragment == null)
            profileFragment = ProfileFragment.getInstance();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.register_FragmentContainer, profileFragment, FRAGMENT_PROFILE);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

        toggle.setDrawerIndicatorEnabled(false);
        toolbar.setTitle(TITLE_PROFILE);
        fab.setVisibility(View.GONE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setChatFragment() {
        if(chatListFragment == null)
            chatListFragment = ChatListFragment.getInstance();
        setSupportActionBar(toolbar);
        chatListFragment = ChatListFragment.getInstance();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.register_FragmentContainer, chatListFragment, FRAGMENT_CHAT);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        toolbar.setTitle(TITLE_HOME);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setSearchFragment() {
        if(searchFragment == null)
            searchFragment = SearchFragment.getInstance();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
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
            Logger.d("[HomeActivity] FCM TOKEN:"+fcmToken);
            User updateUser = new User();
            updateUser.setNotificationToken(fcmToken);
            userApi = new UserApi();
            userApi.updateUser(updateUser, userSession.getAccessToken()).subscribe(new Subscriber<UserResponse>() {
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
