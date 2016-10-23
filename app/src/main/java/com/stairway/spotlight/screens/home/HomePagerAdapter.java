package com.stairway.spotlight.screens.home;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.stairway.data.manager.Logger;
import com.stairway.spotlight.screens.home.chatlist.ChatListFragment;
import com.stairway.spotlight.screens.home.contactlist.ContactListFragment;
import com.stairway.spotlight.screens.home.profile.ProfileFragment;
import com.stairway.spotlight.screens.register.signup.SignUpFragment;

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
                return ContactListFragment.getInstance();
            case 1:
                return ChatListFragment.getInstance();
            case 2:
                return ChatListFragment.getInstance();
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
