package com.stairway.spotlight.screens.home;

import android.content.Context;
import android.content.Intent;

import android.os.Bundle;

import com.stairway.spotlight.R;
import com.stairway.spotlight.internal.di.component.ComponentContainer;
import com.stairway.spotlight.screens.BaseActivity;

public class HomeActivity extends BaseActivity implements HomeScreen{

    public static Intent callingIntent(Context context) {
        Intent intent = new Intent(context, HomeActivity.class);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

    }

    @Override
    protected void injectComponent(ComponentContainer componentContainer) {
        componentContainer.getAppComponent().inject(this);
    }
}
