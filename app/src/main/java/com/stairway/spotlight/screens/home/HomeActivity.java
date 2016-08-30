package com.stairway.spotlight.screens.home;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;

import com.stairway.data.manager.Logger;
import com.stairway.spotlight.R;
import com.stairway.spotlight.core.di.component.ComponentContainer;
import com.stairway.spotlight.core.BaseActivity;
import com.stairway.spotlight.screens.home.chatlist.ChatListFragment;

public class HomeActivity extends BaseActivity{

    public static Intent callingIntent(Context context) {
        Intent intent = new Intent(context, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Logger.v("[Home Activity]");

//        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
//        fragmentTransaction.add(R.id.home_FragmentContainer, ChatListFragment.newInstance());
//        fragmentTransaction.commit();

        ViewPager viewPager = (ViewPager) findViewById(R.id.home_viewpager);
        viewPager.setAdapter(new HomePagerAdapter(getSupportFragmentManager(), HomeActivity.this));

        TabLayout tabLayout = (TabLayout) findViewById(R.id.home_sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    protected void injectComponent(ComponentContainer componentContainer) {
    }
}
