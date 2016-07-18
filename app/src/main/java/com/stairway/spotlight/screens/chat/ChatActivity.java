package com.stairway.spotlight.screens.chat;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.stairway.spotlight.R;
import com.stairway.spotlight.core.di.component.ComponentContainer;
import com.stairway.spotlight.core.BaseActivity;

public class ChatActivity extends BaseActivity {
    private static String USER_ID = "USERID";

    public static Intent callingIntent(Context context, long userId) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(USER_ID, userId);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent receivedIntent = getIntent();
        if(!receivedIntent.hasExtra(USER_ID))
            return;

        android.support.v7.app.ActionBar ab = getSupportActionBar();
        String userName = Long.toString(receivedIntent.getLongExtra(USER_ID, 0));
        ab.setTitle(userName);
    }

    @Override
    protected void injectComponent(ComponentContainer componentContainer) {

    }
}
