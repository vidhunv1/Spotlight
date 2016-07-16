package com.stairway.spotlight.screens.home;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;

import android.os.Bundle;

import com.stairway.data.manager.Logger;
import com.stairway.spotlight.R;
import com.stairway.spotlight.core.di.component.ComponentContainer;
import com.stairway.spotlight.core.BaseActivity;
import com.stairway.spotlight.screens.home.chatlist.ChatListFragment;

public class HomeActivity extends BaseActivity{

    public static Intent callingIntent(Context context) {
        Intent intent = new Intent(context, HomeActivity.class);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Logger.v("[Home Activity]");

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.FragmentContainer, ChatListFragment.newInstance());
        fragmentTransaction.commit();
    }

    @Override
    protected void injectComponent(ComponentContainer componentContainer) {
    }
}
