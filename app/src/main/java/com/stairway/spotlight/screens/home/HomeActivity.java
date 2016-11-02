package com.stairway.spotlight.screens.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

import com.stairway.spotlight.R;
import com.stairway.spotlight.core.BaseActivity;
import com.stairway.spotlight.core.di.component.ComponentContainer;
import com.stairway.spotlight.screens.home.chats.ChatListFragment;
import com.stairway.spotlight.screens.home.contacts.ContactListFragment;
import com.stairway.spotlight.screens.home.profile.ProfileFragment;

public class HomeActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private View actionBarView;
    private ActionBarDrawerToggle toggle;
    private FloatingActionButton fab;
    private Toolbar toolbar;
    private static String TITLE_HOME = "Messages";
    private static String TITLE_CONTACTS = "Contacts";
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
        setChatFragment();

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setContactsFragment();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
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

    @Override
    protected void injectComponent(ComponentContainer componentContainer) {
    }

    private void setContactsFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.register_FragmentContainer, ContactListFragment.getInstance());
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

        toggle.setDrawerIndicatorEnabled(false);
//        android.support.v7.app.ActionBar ab = getSupportActionBar();
//        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
//        ab.setCustomView(R.layout.actionbar_home);
//        ab.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
//        ab.setElevation(0);
//        actionBarView = ab.getCustomView();
//        EditText search = (EditText) actionBarView.findViewById(R.id.actionbar_search);
//        search.setHint("Search Contacts");

        toolbar.setTitle(TITLE_CONTACTS);
        fab.setVisibility(View.GONE);
    }

    private void setProfileFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.register_FragmentContainer, ProfileFragment.getInstance());
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        toggle.setDrawerIndicatorEnabled(false);
        toolbar.setTitle(TITLE_PROFILE);
        fab.setVisibility(View.GONE);
    }

    private void setChatFragment() {
        setSupportActionBar(toolbar);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.register_FragmentContainer, ChatListFragment.getInstance());
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        toolbar.setTitle(TITLE_HOME);
    }
}
