package com.stairway.spotlight.screens.home;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.stairway.spotlight.screens.home.calls.CallListFragment;
import com.stairway.spotlight.screens.home.chats.ChatListFragment;
import com.stairway.spotlight.screens.home.contacts.ContactListFragment;
import com.stairway.spotlight.screens.home.profile.ProfileFragment;

/**
 * Created by Dell on 8/27/2016.
 */
public class HomePagerAdapter extends FragmentPagerAdapter {
    private final int PAGE_COUNT = 4;
    private String tabTitles[] = new String[] {"chats", "calls", "contacts", "profile"};
    private Context context;

    public HomePagerAdapter(FragmentManager fragmentManager, Context context) {
        super(fragmentManager);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return CallListFragment.getInstance();
            case 1:
                return ChatListFragment.getInstance();
            case 2:
                return ContactListFragment.getInstance();
            case 3:
                return ProfileFragment.getInstance();
            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }
}
