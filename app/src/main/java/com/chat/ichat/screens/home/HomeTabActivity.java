package com.chat.ichat.screens.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.chat.ichat.R;
import com.chat.ichat.config.AnalyticsConstants;
import com.chat.ichat.core.BaseActivity;
import com.chat.ichat.core.NotificationController;
import com.chat.ichat.models.ContactResult;
import com.chat.ichat.models.MessageResult;
import com.chat.ichat.screens.discover_bots.DiscoverBotsFragment;
import com.chat.ichat.screens.message.MessageActivity;
import com.chat.ichat.screens.new_chat.NewChatFragment;
import com.chat.ichat.screens.search.SearchActivity;
import com.chat.ichat.screens.settings.SettingsActivity1;
import com.google.firebase.analytics.FirebaseAnalytics;
/**
 * Created by vidhun on 26/07/17.
 */
public class HomeTabActivity extends BaseActivity {

    //This is our tablayout
    private TabLayout tabLayout;

    //This is our viewPager
    private ViewPager viewPager;

    ViewPagerAdapter adapter;

    private static final String KEY_CHAT_USER_NAME = "HomeActivity.CHAT_USERNAME";
    private static final String KEY_ENTRY = "HomeActivity.Entry";

    private FirebaseAnalytics firebaseAnalytics;
    public static final String APP_PREFS_FILE = "app_prefs";
    public static final String KEY_LAST_SYNC = "last_sync";

    public static Intent callingIntent(Context context, int entry, String chatUsername) {
        Intent intent = new Intent(context, HomeTabActivity.class);
        intent.putExtra(KEY_CHAT_USER_NAME, chatUsername);
        intent.putExtra(KEY_ENTRY, entry);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_tab);

        Intent receivedIntent = getIntent();
        if(receivedIntent.getIntExtra(KEY_ENTRY, 0) == 1) {
            startActivity(MessageActivity.callingIntent(this, receivedIntent.getStringExtra(KEY_CHAT_USER_NAME)));
        }
        setSupportActionBar((Toolbar)findViewById(R.id.tb_home));
        this.firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        //Initializing viewPager
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(3);
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(NewChatFragment.getInstance(), "FAVORITES");
        adapter.addFragment(ChatListFragment.getInstance(), "CHAT");
        adapter.addFragment(DiscoverBotsFragment.getInstance(), "DISCOVER");
        viewPager.setAdapter(adapter);

        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(v -> {
            startActivity(SearchActivity.callingIntent(this));
        });

        tabLayout = (TabLayout) findViewById(R.id.tablayout);
        Context context = this;
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition(),false);
                if(tab.getPosition() == 0) {
                    firebaseAnalytics.logEvent(AnalyticsConstants.Event.HOME_TAB_FAVORITES, null);
                } else if(tab.getPosition() == 1) {
                    firebaseAnalytics.logEvent(AnalyticsConstants.Event.HOME_TAB_CHAT, null);
                } else if(tab.getPosition() == 2) {
                    firebaseAnalytics.logEvent(AnalyticsConstants.Event.HOME_TAB_DISCOVER, null);
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                tabLayout.getTabAt(position).select();
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        viewPager.setCurrentItem(1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewPager.setCurrentItem(1, false);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.home_toolbar, menu);
        // Associate searchable configuration with the SearchView
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(SettingsActivity1.callingIntent(this));
                return true;
            case R.id.action_search:
                startActivity(SearchActivity.callingIntent(this));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onMessageReceived(MessageResult messageId, ContactResult from) {
        NotificationController.getInstance().showNotificationAndAlert(true);
    }
}