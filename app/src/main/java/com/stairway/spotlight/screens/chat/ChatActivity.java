package com.stairway.spotlight.screens.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.stairway.spotlight.R;
import com.stairway.spotlight.core.di.component.ComponentContainer;
import com.stairway.spotlight.core.BaseActivity;

public class ChatActivity extends BaseActivity {

    public static Intent callingIntent(Context context) {
        Intent intent = new Intent(context, ChatActivity.class);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
    }

    @Override
    protected void injectComponent(ComponentContainer componentContainer) {

    }
}
